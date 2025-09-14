package com.zicca.zthread.dashboard.dev.server.remote.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosConfigListRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosConfigRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Nacos 代理客户端
 *
 * @author zicca
 */
@Slf4j
@Component
public class NacosClient {

    @Value("${zthread.nacos.server-addr}")
    private String serverAddr;

    /**
     * 获取配置列表
     */
    public List<NacosConfigRespDTO> listConfig() {
        String url = serverAddr + "/v3/console/cs/config/list";
        try {
            HttpResponse response = HttpRequest.get(url)
                    .timeout(5000)
                    .execute();
            String result = response.body();
            if (!response.isOk()) {
                log.error("Failed to list config from Nacos. Status: {}, Response: {}", response.getStatus(), result);
                throw new RuntimeException("Nacos server returned error. Status: " + response.getStatus());
            }
            // 先解析为 Result<NacosConfigListRespDTO>，然后获取 data
            Result<NacosConfigListRespDTO> resultWrapper = JSON.parseObject(result, new TypeReference<Result<NacosConfigListRespDTO>>() {});
            NacosConfigListRespDTO nacosConfigList = resultWrapper.getData();
            log.info("List configs from Nacos.");
            return nacosConfigList != null ? nacosConfigList.getPageItems() : List.of();
        } catch (Exception e) {
            log.error("Exception occurred while listing config from Nacos.", e);
            throw new RuntimeException("Failed to list config from Nacos", e);
        }
    }


    /**
     * 获取配置详情
     */
    public NacosConfigDetailRespDTO getConfig(String namespaceId, String dataId, String groupName) {
        if (StrUtil.isEmpty(dataId)) {
            throw new RuntimeException("dataId cannot be null or empty");
        }
        String url = serverAddr + "/v3/console/cs/config";
        try {
            HttpResponse response = HttpRequest.get(url)
                    .form("dataId", StrUtil.isEmpty(dataId) ? "" : dataId)
                    .form("groupName", StrUtil.isEmpty(groupName) ? "DEFAULT_GROUP" : groupName)
                    .form("namespaceId", StrUtil.isEmpty(namespaceId) ? "public" : namespaceId)
                    .execute();
            String result = response.body();
            if (!response.isOk()) {
                log.error("Failed to get config detail from Nacos. Status: {}, Response: {}", response.getStatus(), result);
                throw new RuntimeException("Nacos server returned error. Status: " + response.getStatus());
            }
            Result<NacosConfigDetailRespDTO> resultWrapper = JSON.parseObject(result, new TypeReference<Result<NacosConfigDetailRespDTO>>() {});
            NacosConfigDetailRespDTO config = resultWrapper.getData();
            log.info("Get config detail from Nacos.");
            return config;
        } catch (Exception e) {
            log.error("Exception occurred while getting config detail from Nacos. dataId: {}, group: {}", dataId, groupName, e);
            throw new RuntimeException("Failed to get config detail from Nacos", e);
        }
    }

    /**
     * 发布配置
     */
    public void publishConfig(String dataId, String groupName, String namespaceId, String type, String content) {
        if (StrUtil.isEmpty(dataId)) {
            throw new RuntimeException("dataId cannot be null or empty");
        }
        if (StrUtil.isEmpty(content)) {
            throw new RuntimeException("content cannot be null or empty");
        }
        String url = serverAddr + "/v3/console/cs/config";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("dataId", dataId)
                    .form("groupName", StrUtil.isEmpty(groupName) ? "DEFAULT_GROUP" : groupName)
                    .form("namespaceId", StrUtil.isEmpty(namespaceId) ? "public" : namespaceId)
                    .form("type", StrUtil.isEmpty( type) ? "yaml" : type)
                    .form("content", content)
                    .execute();
            String result = response.body();
            if (!response.isOk()) {
                log.error("Failed to publish config to Nacos. Status: {}, Response: {}", response.getStatus(), result);
                throw new RuntimeException("Nacos server returned error. Status: " + response.getStatus());
            }
            log.info("Successfully published config to Nacos. dataId: {}, group: {}", dataId, groupName);
        } catch (Exception e) {
            log.error("Exception occurred while publishing config to Nacos. dataId: {}, group: {}", dataId, groupName, e);
            throw new RuntimeException("Failed to publish config to Nacos", e);
        }
    }

    /**
     * 获取服务列表
     */
    public NacosServiceListRespDTO getService(String namespaceId, String serviceName) {
        String url = serverAddr + "/v3/console/ns/instance/list";
        try {
            HttpResponse response = HttpRequest.get(url)
                    .form("pageNo", "1") // 页码
                    .form("pageSize", "10") // 每页数量
                    .form("serviceName", serviceName) // 服务名的pattern，为空时查询所有服务
                    .form("groupName", "DEFAULT_GROUP") // 服务所属的groupName的pattern，为空时查询所有服务
                    .form("namespaceId", namespaceId) // 服务所属的命名空间ID
                    .form("healthyOnly", "false") // 是否只返回健康实例
                    .form("enableOnly", "false") // 是否只返回未下线实例
                    .form("clusterName", "default") // 默认集群
                    .execute();
            String result = response.body();
            if (!response.isOk()) {
                log.error("Failed to list project form Nacos. Status: {}, Response: {}", response.getStatus(), result);
                return new NacosServiceListRespDTO();
            }
            Result<NacosServiceListRespDTO> resultWrapper = JSON.parseObject(result, new TypeReference<Result<NacosServiceListRespDTO>>() {
            });
            NacosServiceListRespDTO serviceList = resultWrapper.getData();
            log.info("List projects from Nacos.");
            return serviceList;
        } catch (Exception e) {
            log.error("Exception occurred while listing project from Nacos.", e);
            throw new RuntimeException("Failed to list project from Nacos", e);
        }
    }


}
