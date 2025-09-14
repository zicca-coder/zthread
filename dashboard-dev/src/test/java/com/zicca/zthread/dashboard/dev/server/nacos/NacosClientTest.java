package com.zicca.zthread.dashboard.dev.server.nacos;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosConfigDetailRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosConfigListRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Nacos client test
 *
 * @author zicca
 */
@Slf4j
@SpringBootTest
public class NacosClientTest {

    private final String baseConfigUrl = "http://192.168.17.128:8081/v3/console/cs/config";

    private final String baseServiceUrl = "http://192.168.17.128:8081/v3/console/ns/instance";

    /**
     * 获取配置详情
     */
    @Test
    public void testGetConfig() {
        String dataId = "nacos-cloud-example.yaml";
        String groupName = "DEFAULT_GROUP";
        String namespaceId = "public";
        try {
            HttpResponse response = HttpRequest.get(baseConfigUrl)
                    .form("dataId", dataId)
                    .form("groupName", groupName)
                    .form("namespaceId", namespaceId)
                    .execute();
            String result = response.body();
            if (!response.isOk()) {
                log.error("Failed to get config detail from Nacos. Status: {}, Response: {}", response.getStatus(), result);
                throw new RuntimeException("Nacos server returned error. Status: " + response.getStatus());
            }
            Result<NacosConfigDetailRespDTO> resultWrapper = JSON.parseObject(result, new TypeReference<Result<NacosConfigDetailRespDTO>>() {});
            NacosConfigDetailRespDTO config = resultWrapper.getData();
            log.info("Get config detail from Nacos. Result: {}", config);
        } catch (Exception e) {
            log.error("Exception occurred while getting config detail from Nacos. dataId: {}, group: {}", dataId, groupName, e);
            throw new RuntimeException("Failed to get config detail from Nacos", e);
        }
    }

    /**
     * 获取配置列表
     */
    @Test
    public void testListConfig() {
        String url = baseConfigUrl + "/list";
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
            log.info("List configs from Nacos. Result: {}", nacosConfigList);
        } catch (Exception e) {
            log.error("Exception occurred while listing config from Nacos.", e);
            throw new RuntimeException("Failed to list config from Nacos", e);
        }
    }

    /**
     * 发布/更新配置
     */
    @Test
    public void testPublishConfig() {
        HttpResponse response = HttpRequest.post(baseConfigUrl)
                .form("dataId", "test.properties")
                .form("groupName", "DEFAULT_GROUP")
                .form("namespaceId", "public")
                .form("type", "Properties")
                .form("content", "name=AAAAAAAAA")
                .execute();
        String result = response.body();
        System.out.println(result);
    }

    /**
     * 获取服务列表
     */
    @Test
    public void testListService() {
        String pageNo = "1";
        String pageSize = "100";
        String serviceName = "nacos-cloud-example";
        String groupName = "DEFAULT_GROUP";
        String namespaceId = "public";
        String healthyOnly = "false";
        String enableOnly = "false";

        String url = baseServiceUrl + "/list";
        try {
            HttpResponse response = HttpRequest.get(url)
                    .form("pageNo", pageNo) // 页码
                    .form("pageSize", pageSize) // 每页数量
                    .form("serviceName", serviceName) // 服务名的pattern，为空时查询所有服务
                    .form("groupName", groupName) // 服务所属的groupName的pattern，为空时查询所有服务
                    .form("namespaceId", namespaceId) // 服务所属的命名空间ID
                    .form("healthyOnly", healthyOnly) // 是否只返回健康实例
                    .form("enableOnly", enableOnly) // 是否只返回未下线实例
                    .form("clusterName", "default")
                    .execute();
            String result = response.body();
            if (!response.isOk()) {
                log.error("Failed to list project form Nacos. Status: {}, Response: {}", response.getStatus(), result);
                throw new RuntimeException("Nacos server returned error. Status: " + response.getStatus());
            }
            Result<NacosServiceListRespDTO> resultWrapper = JSON.parseObject(result, new TypeReference<Result<NacosServiceListRespDTO>>() {
            });
            NacosServiceListRespDTO serviceList = resultWrapper.getData();
            log.info("List projects from Nacos. Result: {}", serviceList);
        } catch (Exception e) {
            log.error("Exception occurred while listing project from Nacos.", e);
            throw new RuntimeException("Failed to list project from Nacos", e);
        }
    }








}
