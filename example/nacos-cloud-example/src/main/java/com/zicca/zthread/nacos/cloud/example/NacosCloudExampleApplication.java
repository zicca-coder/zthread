package com.zicca.zthread.nacos.cloud.example;

import com.zicca.zthread.spring.base.enable.EnableZThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableZThread
@SpringBootApplication
public class NacosCloudExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosCloudExampleApplication.class, args);
    }

}
