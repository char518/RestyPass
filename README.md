# RestyPass
> 高性能的Restful服务调用客户端库，完全兼容Spring MVC 注解，基于接口和注解自动代理客户端HTTP请求，支持服务发现，负载均衡，自动熔断，降级、重试。覆盖Feign + Hystrix + Ribbon + ApacheHttpClient的功能

## 对比SpringCloud技术栈：Feign+Hystrix+Ribbon+ApacheHttpClient
- Http连接池性能提升，RestyPass采用基于Netty实现的AsyncHttpClient连接池，性能测试比ApacheHttpClient高30%。
- 减少对象生成，Feign+Hystrix+Ribbon+ApacheHttpClient，多个库组合完成一个完整的http客户端调用请求，一个调用链中创建很多多余对象。
- 减少线程切换，如Hystrix，ApacheHttpClient中都有自己的线程池，一个请求的完成往往要经过多次的线程切换，损耗性能。
- 更易配置，RestyPass使用注解的方式配置各个接口请求;而使用Feign+Hystrix+Ribbon+ApacheHttpClient，则面临每个库都有自己的配置项，配置繁多而且容易发生冲突，亲身实践，想把这一套配置好是一件不容易的事情。
- 实时更新配置，RestyPass支持实时更新部分配置，比如实时关闭/启用降级服务，实时熔断/恢复服务，且粒度可以精确到接口级。
- 易开发，可自由开发大部分核心接口的自定义实现，并直接注入即可启用（Spring容器）。
## 示例（demo[调用方]+demo-serverside[服务端]）
1. 客户端 

```java
// 使用@EnableRestyPass注解启用RestyPass
@SpringBootApplication
@EnableRestyPass(basePackages = {"df.open"})
@RestController
//@EnableDiscoveryClient
//启用spring cloud 服务发现则RestyPass自动使用spring的服务发现方式，
// 否则默认读取resty-server.yaml来获取服务实例
// 可自定义其它发现服务的方式，实现ServerContext接口并注入即可
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
}


//使用接口和注解定义并配置调用客户端
//RestyService注解定义服务
@RestyService(serviceName = "server",
        fallbackClass = ProxyServiceImpl.class,
        retry = 1,
        fallbackEnabled = true
)
@RequestMapping(value = "/resty")
public interface ProxyService extends ApplicationService {
    
    // RestyMethod注解定义服务接口
    @RestyMethod(retry = 2)
    @RequestMapping(value = "/get_nothing", method = RequestMethod.GET, headers = "Client=RestyProxy", params = "Param1=val1")
    void getNothing();
}

```

```yaml
# resty-server.yaml
servers:
  - serviceName: server
    instances:
      - host: localhost
        port: 9201
      - host: localhost
        port: 9202
```
2. 服务端 
```java
@RestController
@RequestMapping("/resty")
public class TestController {
    @RequestMapping(value = "/get_nothing", method = RequestMethod.GET)
    public void nothing() {
        System.out.println("############nothing");
    }
  }
```


# License

RestyPass is Open Source software released under the Apache 2.0 license.