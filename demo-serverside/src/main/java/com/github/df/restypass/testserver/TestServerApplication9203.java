package com.github.df.restypass.testserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 服务端
 * 测试应用 port9202
 * Created by darrenfu on 17-7-29.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class TestServerApplication9203 {
    public static void main(String[] args) {
        String[] arg = new String[]{"--server.port=9203"};

        SpringApplication.run(TestServerApplication9203.class, arg);
    }
}
