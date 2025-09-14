package com.zicca.zthread.dashboard.dev.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.zicca.zthread.dashboard.dev.server.config.ZThreadProperties;
import com.zicca.zthread.dashboard.dev.server.dto.ProjectInfoRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.client.NacosClient;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.zicca.zthread.dashboard.dev.server.service.IProjectService;
import com.zicca.zthread.dashboard.dev.server.service.handler.ConfigFileTypeEnum;
import com.zicca.zthread.dashboard.dev.server.service.handler.ConfigParser;
import com.zicca.zthread.dashboard.dev.server.service.handler.ConfigParserHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;
import java.util.*;

/**
 * 项目服务实现类
 *
 * @author zicca
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements IProjectService {

    private final ZThreadProperties properties;
    private final NacosClient client;


    @Override
    public List<ProjectInfoRespDTO> listProject() {
        List<ProjectInfoRespDTO> projects = new ArrayList<>();
        List<String> namespaces = properties.getNamespaces();
        namespaces.forEach(namespace -> {
            List<NacosConfigRespDTO> nacosConfigResponse = client.listConfig();
            if (CollUtil.isEmpty(nacosConfigResponse)) {
                return;
            }
            nacosConfigResponse.stream().filter(each -> StrUtil.isNotBlank(each.getAppName()))
                    .forEach(config -> {
                        try {
                            NacosConfigDetailRespDTO configDetail = client.getConfig(namespace, config.getDataId(), config.getGroupName());
                            NacosServiceListRespDTO serviceDetail = client.getService(namespace, config.getAppName());
                            if (Objects.isNull(configDetail)) {
                                return;
                            }
                            Map<Object, Object> map = null;
                            try {
                                switch (ConfigFileTypeEnum.of(configDetail.getType())) {
                                    case YML:
                                    case YAML:
                                        Yaml yaml = new Yaml();
                                        map = yaml.load(configDetail.getContent());
                                        break;
                                    case PROPERTIES: {
                                        Properties properties = new Properties();
                                        try (StringReader reader = new StringReader(configDetail.getContent())) {
                                            properties.load(reader);
                                        }
                                        // 将 Properties 转换为 Map
                                        map = new HashMap<>(properties);
                                        break;
                                    }
                                    default:
                                        map = new LinkedHashMap<>();
                                        break;
                                }
                            } catch (Exception e) {
                                log.warn("Failed to parse config content for dataId: {}, groupName: {}",
                                        config.getDataId(), config.getGroupName(), e);
                                map = new LinkedHashMap<>(); // 出错时使用空Map
                            }

                            // 修复空指针异常：检查 map 是否为 null
                            Map<String, Object> zhtread = null;
                            if (map != null && map.containsKey("zthread")) {
                                Object zthreadObj = map.get("zthread");
                                if (zthreadObj instanceof Map) {
                                    zhtread = (Map<String, Object>) map.get("zthread");
                                }
                            }
                            int executorCount = 0;
                            // 只有当 zhtread 不为 null 时才尝试获取 executors
                            if (zhtread != null) {
                                Object executorsObj = zhtread.get("executors");
                                if (executorsObj instanceof List) {
                                    executorCount = ((List<?>) executorsObj).size();
                                }
                            }
                            projects.add(
                                    ProjectInfoRespDTO.builder()
                                            .namespace(namespace)
                                            .serviceName(config.getAppName())
                                            .instanceCount(serviceDetail.getTotalCount())
                                            .threadPoolCount(executorCount)
                                            .hasWebThreadPool(zhtread != null && zhtread.containsKey("web"))
                                            .build()
                            );
                        } catch (Exception e) {
                            // 添加适当的异常处理
                            log.warn("Failed to process config for dataId: {}, groupName: {}", config.getDataId(), config.getGroupName(), e);
                        }
                    });
        });
        return projects;
    }
}
