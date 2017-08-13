package com.github.df.restypass.testclient;

import com.github.df.restypass.command.RestyFuture;
import com.github.df.restypass.spring.EnableRestyPass;
import com.github.df.restypass.testclient.service.ProxyService;
import com.github.df.restypass.util.PerformanceTools;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端
 * 测试应用
 * Created by darrenfu on 17-7-31.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@SpringBootApplication
//@EnableDiscoveryClient
@EnableRestyPass(basePackages = {"com.github.df"})
@RestController
public class TestClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestClientApplication.class, args);
    }


    @Autowired
    private ProxyService proxyService;

    @RequestMapping(value = "nothing")
    public String callNothing() {
        proxyService.getNothing();
        return "OK";
    }

    @RequestMapping(value = "status")
    public String callStatus() {
        // future为出参，将会被填充future数据
        RestyFuture<String> future = new RestyFuture<>();
        String status = proxyService.getStatus(future);
        Assert.assertTrue("原始返回为null", status == null);
        String futureResult = future.get();

        Assert.assertTrue("future获取正确结果", "Status is OK".equals(futureResult));
        return "OK";
    }

    @RequestMapping(value = "/result")
    public String result() {
        return PerformanceTools.getPerformance().toString();
    }
}
