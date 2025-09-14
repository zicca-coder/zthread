package com.zicca.zthread.dashboard.dev.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.zicca.zthread.dashboard.dev.server.config.DashBoardConfigProperties;
import com.zicca.zthread.dashboard.dev.server.config.ZThreadProperties;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolListReqDTO;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolUpdateReqDTO;
import com.zicca.zthread.dashboard.dev.server.remote.client.NacosClient;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.zicca.zthread.dashboard.dev.server.service.IThreadPoolManagerService;
import com.zicca.zthread.dashboard.dev.server.service.handler.ConfigFileTypeEnum;
import com.zicca.zthread.dashboard.dev.server.service.handler.ConfigParserHandler;
import com.zicca.zthread.dashboard.dev.server.service.handler.YamlConfigParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * 线程池管理服务实现类
 *
 * @author zicca
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadPoolManagerServiceImpl implements IThreadPoolManagerService {

    private final ZThreadProperties properties;
    private final NacosClient client;


    @Override
    public List<ThreadPoolDetailRespDTO> listThreadPool(ThreadPoolListReqDTO requestParam) {
        // 获取配置中的命名空间列表，并根据请求参数进行过滤
        List<String> namespaces = new ArrayList<>(properties.getNamespaces());
        String requestedNamespace = StrUtil.isEmpty(requestParam.getNamespace()) ? "public" : requestParam.getNamespace();
        String requestedServiceName = requestParam.getServiceName();

        // 如果请求参数中指定了命名空间，并且该命名空间在配置中存在，则只查询该命名空间
        if (StrUtil.isNotBlank(requestedNamespace) && namespaces.contains(requestedNamespace)) {
            namespaces = Collections.singletonList(requestedNamespace);
        }
        List<Map.Entry<String, NacosConfigRespDTO>> tasks = namespaces
                .parallelStream()
                .flatMap(namespace -> {
                    List<NacosConfigRespDTO> cfgs = client.listConfig();
                    if (CollUtil.isEmpty(cfgs)) {
                        return Stream.<Map.Entry<String, NacosConfigRespDTO>>empty();
                    }
                    return cfgs.stream().filter(cfg -> StrUtil.isNotBlank(cfg.getAppName()))
                            .filter(cfg -> StrUtil.isBlank(requestedServiceName) || Objects.equals(cfg.getAppName(), requestedServiceName))
                            .map(cfg -> new AbstractMap.SimpleEntry<>(namespace, cfg));
                }).toList();

        return tasks.parallelStream()
                .map(entry -> {
                    String namespace = entry.getKey();
                    NacosConfigRespDTO cfg = entry.getValue();
                    NacosConfigDetailRespDTO configDetail;
                    NacosServiceListRespDTO serviceDetail;
                    try {
                        configDetail = client.getConfig(namespace, cfg.getDataId(), cfg.getGroupName());
                        serviceDetail = client.getService(namespace, cfg.getAppName());
                    } catch (Exception e) {
                        log.warn("Failed to fetch config or service detail for namespace: {}, dataId: {}, appName: {}", 
                                namespace, cfg.getDataId(), cfg.getAppName(), e);
                        return Collections.<ThreadPoolDetailRespDTO>emptyList();
                    }
                    Map<Object, Object> configInfoMap = null;
                    try {
                        configInfoMap = ConfigParserHandler.getInstance().parseConfig(configDetail.getContent(), ConfigFileTypeEnum.of(configDetail.getType()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    MapConfigurationPropertySource sources = new MapConfigurationPropertySource(configInfoMap);
                    Binder binder = new Binder(sources);

                    BindResult<DashBoardConfigProperties> bound = binder.bind("zthread", Bindable.of(DashBoardConfigProperties.class));
                    if (!bound.isBound()) {
                        return Collections.<ThreadPoolDetailRespDTO>emptyList();
                    }

                    DashBoardConfigProperties refresherProperties = bound.get();
                    refresherProperties.getExecutors().forEach(each -> {
                        each.setNamespace(namespace);
                        each.setServiceName(cfg.getAppName());
                        each.setDataId(cfg.getDataId());
                        each.setGroup(cfg.getGroupName());
                        each.setInstanceCount(serviceDetail.getTotalCount());
                    });
                    return refresherProperties.getExecutors();
                })
                .flatMap(List::stream)
                .toList();
    }

    @Override
    @SneakyThrows
    public void updateGlobalThreadPool(ThreadPoolUpdateReqDTO requestParam) {
        NacosConfigDetailRespDTO configDetail = client.getConfig(requestParam.getNamespace(), requestParam.getDataId(), requestParam.getGroup());
        String originalContent = configDetail.getContent();

        Map<Object, Object> configInfoMap = null;
        try {
            configInfoMap = ConfigParserHandler.getInstance().parseConfig(configDetail.getContent(), ConfigFileTypeEnum.of(configDetail.getType()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(configInfoMap);

        Binder binder = new Binder(source);
        DashBoardConfigProperties zthread = binder.bind("zthread", Bindable.of(DashBoardConfigProperties.class))
                .orElseThrow(() -> new RuntimeException("binding failed"));

        zthread.getExecutors().stream()
                .filter(e -> e.getThreadPoolId().equals(requestParam.getThreadPoolId()))
                .findFirst()
                .ifPresent(e -> {
                    e.setCorePoolSize(requestParam.getCorePoolSize());
                    e.setMaximumPoolSize(requestParam.getMaximumPoolSize());
                    e.setKeepAliveTime(requestParam.getKeepAliveTime());
                    e.setQueueCapacity(requestParam.getQueueCapacity());
                    e.setWorkQueue(requestParam.getWorkQueue());
                    e.setRejectedHandler(requestParam.getRejectedHandler());
                    e.setAllowCoreThreadTimeOut(requestParam.getAllowCoreThreadTimeOut());
                    e.setNotify(BeanUtil.toBean(requestParam.getNotify(), ThreadPoolDetailRespDTO.NotifyConfig.class));
                    e.setAlarm(BeanUtil.toBean(requestParam.getAlarm(), ThreadPoolDetailRespDTO.AlarmConfig.class));
                });

        Map<String, Object> updatedMap = new LinkedHashMap<>();
        updatedMap.put("zthread", zthread);

        YAMLFactory factory = new YAMLFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);

        ObjectMapper objectMapper = new ObjectMapper(factory);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        String yamlStr = objectMapper.writeValueAsString(Collections.singletonMap("zthread", zthread));
        client.publishConfig(requestParam.getDataId(), requestParam.getGroup(), requestParam.getNamespace(), "yaml", yamlStr);
    }
}
