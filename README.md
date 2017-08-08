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
## Demo（demo[client]+demo-serverside[server]） 

1. client 

```java
// use @EnableRestyPass to enable RestyPass
@SpringBootApplication
@EnableRestyPass(basePackages = {"com.github.df"})
@RestController
//@EnableDiscoveryClient
//RestyPass use DiscoveryClient in spring cloud automatically,
// otherwise it will use resty-server.yaml to get server list
// use ServerContext interface, you can define your own way to find server, just inject it as a normal bean will be fine. 
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
    @RestyMethod(retry = 2, forceBreakEnabled = "true", limit = 10)
    // use spring mvc annotation to define interface's details
    @RequestMapping(value = "/get_nothing", method = RequestMethod.GET, headers = "Client=RestyProxy", params = "Param1=val1")
    void getNothing();
}

```

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
2. server 

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

# import jar 

```xml 

<dependency>
    <groupId>com.github.darren-fu</groupId>
    <artifactId>resty-pass</artifactId>
    <version>LATEST_VERSION</version>
</dependency>
``` 


# License

RestyPass is Open Source software released under the Apache 2.0 license.