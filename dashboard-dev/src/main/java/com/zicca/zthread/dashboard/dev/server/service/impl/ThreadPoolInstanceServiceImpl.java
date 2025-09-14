package com.zicca.zthread.dashboard.dev.server.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.zicca.zthread.dashboard.dev.server.common.Result;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolBaseMetricsRespDTO;
import com.zicca.zthread.dashboard.dev.server.dto.ThreadPoolStateRespDTO;
import com.zicca.zthread.dashboard.dev.server.remote.client.NacosClient;
import com.zicca.zthread.dashboard.dev.server.remote.dto.NacosServiceListRespDTO;
import com.zicca.zthread.dashboard.dev.server.service.IThreadPoolInstanceService;
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
 * 线程池实例服务实现
 *
 * @author zicca
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadPoolInstanceServiceImpl implements IThreadPoolInstanceService {

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
    public List<ThreadPoolBaseMetricsRespDTO> listBasicMetrics(String namespace, String serviceName, String threadPoolId) {
        NacosServiceListRespDTO serviceList = client.getService(namespace, serviceName);
        if (serviceList == null || serviceList.getTotalCount() == 0) {
            return List.of();
        }

        List<CompletableFuture<ThreadPoolBaseMetricsRespDTO>> futures = serviceList.getPageItems().stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String networkAddress = item.getIp() + ":" + item.getPort();
                        String resultStr = HttpUtil.get("http://" + networkAddress + "/dynamic/thread-pool/" + threadPoolId + "/basic-metrics");
                        Result<ThreadPoolBaseMetricsRespDTO> result = JSON.parseObject(resultStr, new TypeReference<>() {
                        });
                        return result.getData();
                    } catch (Exception e) {
                        log.error("Error fetching metrics from {}", item.getIp(), e);
                        return null;
                    }
                }, threadPoolExecutor)).toList();
        return futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();
    }

    @Override
    public ThreadPoolStateRespDTO getRuntimeState(String threadPoolId, String networkAddress) {
        String resultStr = HttpUtil.get("http://" + networkAddress + "/dynamic/thread-pool/" + threadPoolId);
        Result<ThreadPoolStateRespDTO> result = JSON.parseObject(resultStr, new TypeReference<>() {
        });
        return result.getData();
    }
}
