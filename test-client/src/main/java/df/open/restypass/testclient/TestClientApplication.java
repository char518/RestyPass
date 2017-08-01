package df.open.restypass.testclient;

import df.open.restypass.starter.EnableRestyProxy;
import df.open.restypass.testclient.service.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
@EnableRestyProxy(basePackages = {"df.open"})
@RestController
public class TestClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestClientApplication.class, args);
    }


    @Autowired
    private ProxyService proxyService;

//    @Autowired
//    private DiscoveryClient discoveryClient;

    @RequestMapping(value = "nothing")
    public String callNothing() {
        proxyService.getNothing();
//        System.out.println(discoveryClient.getServices());
        return "OK";
    }
}
