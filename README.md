# RestyPass
>High performance Restful services call client library, support service discovery, load balance, circuit breaker, service fallback, retry. 
automatically proxy client HTTP requests based on interfaces and annotations and compatible with Spring MVC annotations。

## Purpose 

Project can be used with spring cloud / spring boot, solve the interface call between services in the micro service architecture.
Welcome to contribute ideas and code. 

## compare with SpringCloud：Feign + Hystrix + Ribbon + ApacheHttpClient
- Http connection pool performance upgrade, RestyPass based on Netty implementation of the AsyncHttpClient connection pool, performance testing than ApacheHttpClient 30% higher.
- Reduce object generation, Feign+Hystrix+Ribbon+ApacheHttpClient, multiple library combinations to complete a complete http client request, a request chain to create a lot of redundant objects。
- Reduce thread switching, Such as Hystrix, ApacheHttpClient have their own thread pool, a request is often completed through a number of thread switching, loss of performance.
- Easier configuration, RestyPass uses annotations to configure individual interface requests.
- Real-time update configuration, RestyPass support real-time update part of the configuration, such as  disable / enable fallback services, disable / enable circuit breaker, and granularity can be accurate to the interface level。
- Easy to develop, free to implement most of the core interface of the custom implementation, and direct injection can be enabled（base on Spring context）。 
- Support filter, and feel free to define a new one. 
- Support traffic limit configuration.
- Support service discovery automatically.
## Demo（demo[client]+demo-serverside[server]） 

### client side

#### enable resty service

RestyPass will use DiscoveryClient in spring cloud automatically while you enable server discovery,otherwise it will use resty-server.yaml to get server list.
Implement ServerContext interface, you can define your own way to find server, just inject it as a normal bean will be fine. 

```java
// use @EnableRestyPass to enable RestyPass
@SpringBootApplication
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
}
``` 

#### define client service
```java 

//RestyService define service
@RestyService(serviceName = "server",
        fallbackEnabled = true,
        fallbackClass = ProxyServiceImpl.class,
        forceBreakEnabled = false,
        circuitBreakEnabled = false,
        loadBalancer = RandomLoadBalancer.NAME,
        retry = 1,
        requestTimeout = 10000,
        limit = 1000 //traffic limit
)
@RequestMapping(value = "/resty")
public interface ProxyService extends ApplicationService {
    
    // RestyMethod define interface
    // 同步调用
    @RestyMethod(retry = 2,
            fallbackEnabled = "false",
            circuitBreakEnabled = "false",
            forceBreakEnabled = "false",
            limit = 10)
    @RequestMapping(value = "/get_nothing", method = RequestMethod.GET, headers = "Client=RestyProxy", params = "Param1=val1")
    void getNothing();
    
    //use spring mvc annotations
    @RestyMethod()
    @RequestMapping(value = "/get_age", method = RequestMethod.GET)
    Response<String> getAge(@RequestParam("id") Long id, String code, @PathVariable(value = "name") String name, @RequestHeader(value="TOKEN") String token);

    // Async call： (outcome)parameter type is Future<?>
    @RestyMethod
    @RequestMapping(value = "/get_status", method = RequestMethod.GET)
    String getStatus(RestyFuture<String> future);

    // Async call ： return type is Future<?> 
    @RestyMethod
    @RequestMapping(value = "/get_user", method = RequestMethod.GET)
    Future<User> getUser();
}

```
#### define server instances
we use yaml to define instances in demo, usually, we use CloudDiscoveryServerContext to discover instances automatically. 

```yaml
# resty-server.yaml
# define server list
servers:
  - serviceName: server
    instances:
      - host: localhost
        port: 9201
      - host: localhost
        port: 9202
```
### server side

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
## core conception

- RestyCommand: contain all information in one resty request.
- RestyCommandExecutor: execute resty command and return the result.
- ServerContext: server instances container, find and refresh the server instances.
- LoadBalancer: load balancer, you can define one technical LB for every resty service.
- CommandFilter: filter the command as your want.
- FallbackExecutor: execute the fallback Impl.

## config and inject
You can use your own Impl, just inject it will be fine. 
 
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

# import jar 

```xml 

<dependency>
    <groupId>com.github.darren-fu</groupId>
    <artifactId>resty-pass</artifactId>
    <version>1.3.0</version>
</dependency>
``` 


# License

RestyPass is Open Source software released under the Apache 2.0 license.