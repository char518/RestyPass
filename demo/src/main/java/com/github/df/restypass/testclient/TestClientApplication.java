package com.github.df.restypass.testclient;

import com.github.df.restypass.command.RestyFuture;
import com.github.df.restypass.spring.EnableRestyPass;
import com.github.df.restypass.testclient.entity.User;
import com.github.df.restypass.testclient.service.ProxyService;
import com.github.df.restypass.util.PerformanceTools;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 客户端
 * 测试应用
 * Created by darrenfu on 17-7-31.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@SpringBootApplication
@EnableDiscoveryClient
@EnableRestyPass(basePackages = {"com.github.df"})
@RestController
public class TestClientApplication {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(TestClientApplication.class, args);
    }


    @Autowired
    private ProxyService proxyService;

    /**
     * Call nothing string.
     *
     * @return the string
     */
    @RequestMapping(value = "nothing")
    public String callNothing() {
        proxyService.getNothing();
        return "OK";
    }

    /**
     * Call status string.
     *
     * @return the string
     */
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

    /**
     * Call user string.
     *
     * @return the string
     * @throws ExecutionException   the execution exception
     * @throws InterruptedException the interrupted exception
     */
    @RequestMapping(value = "user")
    public String callUser() throws ExecutionException, InterruptedException {
        Future<User> future = proxyService.getUser();

        System.out.println("get future");
        while (!future.isDone()) {
            System.out.println("##### sleep wait");
            Thread.sleep(100);
        }
        User user = future.get();
        System.out.println("user:" + user);

        return "OK";
    }


    /**
     * Call void async string.
     *
     * @return the string
     * @throws ExecutionException   the execution exception
     * @throws InterruptedException the interrupted exception
     */
    @RequestMapping(value = "void_async")
    public String callVoidAsync() throws ExecutionException, InterruptedException {
        Future<Void> future = proxyService.getVoidAsync();

        System.out.println("get future");
        Void aVoid = future.get();
        System.out.println("user:" + aVoid);
        return "OK";
    }

    /**
     * Result string.
     *
     * @return the string
     */
    @RequestMapping(value = "/result")
    public String result() {
        return PerformanceTools.getPerformance().toString();
    }
}
