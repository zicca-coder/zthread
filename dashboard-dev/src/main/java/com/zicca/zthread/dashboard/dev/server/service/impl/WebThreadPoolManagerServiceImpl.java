package com.zicca.zthread.dashboard.dev.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.config.DashBoardConfigProperties;
import com.zicca.zthread.dashboard.dev.server.config.ZThreadProperties;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolListReqDTO;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolStateRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolUpdateReqDTO;
import com.zicca.zthread.dashboard.dev.server.remote.client.NacosClient;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosServiceRespDTO;
import com.zicca.zthread.dashboard.dev.server.service.IWebThreadPoolManagerService;
import com.zicca.zthread.dashboard.dev.server.service.handler.ConfigFileTypeEnum;
import com.zicca.zthread.dashboard.dev.server.service.handler.ConfigParserHandler;
import com.zicca.zthread.dashboard.dev.server.service.handler.YamlConfigParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * web线程池管理服务实现
 *
 * @author zicca
 */
@Service
@RequiredArgsConstructor
public class WebThreadPoolManagerServiceImpl implements IWebThreadPoolManagerService {

    private final ZThreadProperties properties;
    private final NacosClient client;


    @Override
    public List<WebThreadPoolDetailRespDTO> listThreadPool(WebThreadPoolListReqDTO requestParam) {
        List<WebThreadPoolDetailRespDTO> threadPools = new ArrayList<>();

        List<String> namespaces = new ArrayList<>(properties.getNamespaces());
        String requestedNamespace = StrUtil.isEmpty(requestParam.getNamespace()) ? "public" : requestParam.getNamespace();
        String requestedServiceName = requestParam.getServiceName();
        if (StrUtil.isNotBlank(requestedNamespace) && namespaces.contains(requestedNamespace)) {
            namespaces = Collections.singletonList(requestedNamespace);
        }
        namespaces.forEach(namespace -> {
            List<NacosConfigRespDTO> nacosConfigResponse = client.listConfig();
            if (CollUtil.isEmpty(nacosConfigResponse)) {
                return;
            }
            nacosConfigResponse.stream()
                    .forEach(config -> {
                        NacosConfigDetailRespDTO configDetail = client.getConfig(namespace, config.getDataId(), config.getGroupName());
                        NacosServiceListRespDTO serviceDetail = client.getService(namespace, config.getAppName());

                        Map<Object, Object> configInfoMap = null;
                        try {
                            configInfoMap = ConfigParserHandler.getInstance().parseConfig(configDetail.getContent(), ConfigFileTypeEnum.of(configDetail.getType()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        MapConfigurationPropertySource sources = new MapConfigurationPropertySource(configInfoMap);
                        Binder binder = new Binder(sources);

                        DashBoardConfigProperties refresherProperties;
                        try {
                            refresherProperties = binder.bind("zthread", Bindable.of(DashBoardConfigProperties.class))
                                    .orElseThrow(() -> new RuntimeException("zthread config binding failed"));
                        } catch (Exception e) {
                            return;
                        }
                        DashBoardConfigProperties.WebThreadPoolExecutorConfig webThreadPoolConfig = refresherProperties.getWeb();
                        if (serviceDetail == null || CollUtil.isEmpty(serviceDetail.getPageItems()) || webThreadPoolConfig == null) {
                            return;
                        }

                        NacosServiceRespDTO nacosService = serviceDetail.getPageItems().get(0);
                        String networkAddress = nacosService.getIp() + ":" + nacosService.getPort();

                        Result<WebThreadPoolStateRespDTO> result;
                        try {
                            String resultStr = HttpUtil.get("http://" + networkAddress + "/web/thread-pool", 1000);
                            result = JSON.parseObject(resultStr, new TypeReference<>() {
                            });
                        } catch (Exception e) {
                            return;
                        }
                        String webContainerName = result.getData().getWebContainerName();

                        WebThreadPoolDetailRespDTO webThreadPool = WebThreadPoolDetailRespDTO.builder()
                                .webContainerName(webContainerName)
                                .namespace(namespace)
                                .serviceName(config.getAppName())
                                .dataId(config.getDataId())
                                .group(config.getGroupName())
                                .instanceCount(serviceDetail.getTotalCount())
                                .corePoolSize(webThreadPoolConfig.getCorePoolSize())
                                .maximumPoolSize(webThreadPoolConfig.getMaximumPoolSize())
                                .keepAliveTime(webThreadPoolConfig.getKeepAliveTime())
                                .notify(BeanUtil.toBean(webThreadPoolConfig.getNotify(), WebThreadPoolDetailRespDTO.NotifyConfig.class))
                                .build();
                        threadPools.add(webThreadPool);
                    });
        });
        return threadPools;
    }

    @Override
    @SneakyThrows
    public void updateGlobalThreadPool(WebThreadPoolUpdateReqDTO requestParam) {
        NacosConfigDetailRespDTO configDetail = client.getConfig(requestParam.getNamespace(), requestParam.getDataId(), requestParam.getGroup());
        String originalContent = configDetail.getContent();

        Map<Object, Object> configInfoMap = null;
        try {
            configInfoMap = ConfigParserHandler.getInstance().parseConfig(configDetail.getContent(), ConfigFileTypeEnum.of(configDetail.getType()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ConfigurationPropertySource source = new MapConfigurationPropertySource(configInfoMap);

        Binder binder = new Binder(source);
        DashBoardConfigProperties zthread = binder.bind("zthread", Bindable.of(DashBoardConfigProperties.class))
                .orElseThrow(() -> new RuntimeException("binding failed"));

        zthread.setWeb(BeanUtil.toBean(requestParam, DashBoardConfigProperties.WebThreadPoolExecutorConfig.class));

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
