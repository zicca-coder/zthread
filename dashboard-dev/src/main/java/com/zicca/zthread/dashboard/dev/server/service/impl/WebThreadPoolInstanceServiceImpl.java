package com.zicca.zthread.dashboard.dev.server.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolBaseMetricsRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.WebThreadPoolStateRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.client.NacosClient;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.zicca.zthread.dashboard.dev.server.service.IWebThreadPoolInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**

 * web线程池实例服务实现类
 * @author zicca
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebThreadPoolInstanceServiceImpl implements IWebThreadPoolInstanceService {

    private final NacosClient client;

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 10,
            Runtime.getRuntime().availableProcessors() * 10,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    @Override
    public List<WebThreadPoolBaseMetricsRespDTO> listBasicMetrics(String namespace, String serviceName) {
        NacosServiceListRespDTO serviceListResponse = client.getService(namespace, serviceName);
        if (serviceListResponse == null || serviceListResponse.getTotalCount() == 0) {
            return List.of();
        }
        List<CompletableFuture<WebThreadPoolBaseMetricsRespDTO>> futures = serviceListResponse.getPageItems().stream()
                .map(service -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String networkAddress = service.getIp() + ":" + service.getPort();
                        String resultStr = HttpUtil.get("http://" + networkAddress + "/web/thread-pool/basic-metrics");
                        log.info("resultStr: {}", resultStr);
                        Result<WebThreadPoolBaseMetricsRespDTO> result = JSON.parseObject(resultStr, new TypeReference<>() {
                        });
                        return result.getData();
                    } catch (Exception e) {
                        log.error("Error fetching metrics from {}", service.getIp(), e);
                        return null;
                    }
                }, threadPoolExecutor)).toList();
        List<WebThreadPoolBaseMetricsRespDTO> results = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();
        return results;
    }

    @Override
    public WebThreadPoolStateRespDTO getRuntimeState(String networkAddress) {
        String resultStr = HttpUtil.get("http://" + networkAddress + "/web/thread-pool");
        Result<WebThreadPoolStateRespDTO> result = JSON.parseObject(resultStr, new TypeReference<>() {
        });
        return result.getData();
    }
}
