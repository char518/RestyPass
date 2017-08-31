# RestyPass
> 高性能的Restful服务调用客户端库，完全兼容Spring MVC 注解，基于接口和注解自动代理客户端HTTP请求，支持服务发现，负载均衡，自动熔断，降级，重试，限流。覆盖Feign + Hystrix + Ribbon + ApacheHttpClient的功能

# 欢迎贡献代码 

新生项目，可与spring cloud/spring boot配套使用,帮助微服务架构更容易落地，解决服务间最后一公里的调用问题。
欢迎贡献想法和code~ 

github: https://github.com/darren-fu/RestyPass

## 对比SpringCloud技术栈：Feign+Hystrix+Ribbon+ApacheHttpClient
- Http连接池性能提升，RestyPass采用基于Netty实现的AsyncHttpClient连接池，性能测试比ApacheHttpClient高30%。
- 减少对象生成，Feign+Hystrix+Ribbon+ApacheHttpClient，多个库组合完成一个完整的http客户端调用请求，一个调用链中创建很多多余对象。
- 减少线程切换，如Hystrix，ApacheHttpClient中都有自己的线程池，一个请求的完成往往要经过多次的线程切换，损耗性能。
- 更易配置，RestyPass使用注解的方式配置各个接口请求;而使用Feign+Hystrix+Ribbon+ApacheHttpClient，则面临每个库都有自己的配置项，配置繁多而且容易发生冲突，亲身实践，想把这一套配置好是一件不容易的事情。
- 实时更新配置，RestyPass支持实时更新部分配置，比如实时关闭/启用降级服务，实时熔断/恢复服务，且粒度可以精确到接口级。
- 易开发，可自由开发大部分核心接口的自定义实现，并直接注入即可启用（Spring容器）。 
- 支持过滤器，并可以自定义过滤器并注入
- 支持限流

## 示例（demo[调用方]+demo-serverside[服务端]） 

### 客户端代码 

- 启用spring cloud 服务发现则RestyPass自动使用spring的服务发现方式，
- 否则默认读取resty-server.yaml来获取服务实例
- 可自定义其它发现服务的方式，实现ServerContext接口并注入即可
```java
// 使用@EnableRestyPass注解启用RestyPass
@SpringBootApplication
@EnableRestyPass(basePackages = {"com.github.df"})
@RestController
//@EnableDiscoveryClient
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
``` 

**客户端服务**
 
```java 

//使用接口和注解定义并配置调用客户端
//RestyService注解定义服务
@RestyService(serviceName = "server",
        fallbackEnabled = true,
        fallbackClass = ProxyServiceImpl.class,
        forceBreakEnabled = false,
        circuitBreakEnabled = false,
        loadBalancer = RandomLoadBalancer.NAME,
        retry = 1,
        requestTimeout = 10000,
        limit = 1000, //限流
        version = {"<=1.3.1-RELEASE","<2.0-RC"} // version route
)
@RequestMapping(value = "/resty")
public interface ProxyService extends ApplicationService {
    
    // RestyMethod注解定义服务接口
    // 同步调用
    @RestyMethod(retry = 2,
            fallbackEnabled = "false",
            circuitBreakEnabled = "false",
            forceBreakEnabled = "false",
            limit = 10)
    @RequestMapping(value = "/get_nothing", method = RequestMethod.GET, headers = "Client=RestyProxy", params = "Param1=val1")
    void getNothing();
       
    //支持spring mvc注解
    @RestyMethod()
    @RequestMapping(value = "/get_age/{name}", method = RequestMethod.GET)
    Response<String> getAge(@RequestParam("id") Long id, @PathVariable(value = "name") String name, @RequestHeader(value="TOKEN") String token);


    // 异步调用形式： Future<?> 参数类型，出参
    @RestyMethod
    @RequestMapping(value = "/get_status", method = RequestMethod.GET)
    String getStatus(RestyFuture<String> future);

    // 异步调用形式： Future<?> 返回类型
    @RestyMethod
    @RequestMapping(value = "/get_user", method = RequestMethod.GET)
    Future<User> getUser();
    
    
}

```

**服务实例定义**
- 支持SC自动服务发现，yaml配置等多种方式；
- 可实现接口ServerContext自定义服务发现机制（如多注册中心）

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

### 服务端代码

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

### 版本路由机制
1. 要点
版本信息由版本号和版本阶段组成
如： 1.2.2-RELEASE, 版本号为：1.22, 版本阶段：RELEASE. 
3.4SNAPSHOT, 版本号为：3.4, 版本阶段： SNPSHOT.
1.1.0,版本阶段：1.10,没有版本阶段

2. 配置
版本路由的配置在配置版本号和版本阶段的同时，支持多种操作符来作为版本的路由约束条件。
版本号： >, >=, <, <=, =,!,!= (!等同!=)
版本阶段!, !=, =(!等同!=)

例如： version = {"<=1.3.1-RELEASE","<2.0-RC"} 

1.2.5-RELEASE 或者 1.8.1-RC 匹配此配置；  1.3.2-RELEASE或1.2.5-SNAPSHOT 不匹配。


## 核心接口

- RestyCommand: 包含单次所有信息。
- RestyCommandExecutor: command执行器，返回执行结果。
- ServerContext: 服务实例容器，负责服务发现和更新。
- LoadBalancer: 负载均衡器，实现接口或者继承抽象类可以轻易的实现想要的LB，并且很容易在RestyService中配置并使用
- CommandFilter: command过滤器
- FallbackExecutor: 降级服务类执行器。

## 配置与注入
注入自己的实现类，实现特殊需求。
 
```java
@Configuration
public class RestyPassConfig {

    @Bean
    public FallbackExecutor fallbackExecutor() {
        return new RestyFallbackExecutor();
    }

    @Bean
    public ServerContext serverContext() {
        return new ConfigurableServerContext();
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    public CommandExecutor commandExecutor(RestyCommandContext commandContext) {
        return new RestyCommandExecutor(commandContext);
    }

    @Bean
    public CommandFilter CustomCommandFilter() {
        return new CustomCommandFilter();
    }


    private static class CustomCommandFilter implements CommandFilter {
        @Override
        public int order() {
            return 0;
        }

        @Override
        public boolean shouldFilter(RestyCommand restyCommand) {
            return true;
        }

        @Override
        public CommandFilterType getFilterType() {
            return CommandFilterType.BEFOR_EXECUTE;
        }

        @Override
        public void before(RestyCommand restyCommand) throws FilterException {

            System.out.println("custom command filter");
        }

        @Override
        public void after(RestyCommand restyCommand, Object result) {

        }

        @Override
        public void error(RestyCommand restyCommand, RestyException ex) {

        }
    }

}


``` 
# 引入jar 

```xml 

<dependency>
    <groupId>com.github.darren-fu</groupId>
    <artifactId>resty-pass</artifactId>
    <version>1.3.0</version>
</dependency>
``` 

# License

RestyPass is Open Source software released under the Apache 2.0 license.