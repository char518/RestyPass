package df.open.restypass.cb;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 断路器 工厂类
 * Created by darrenfu on 17-7-25.
 */
public class CircuitBreakerFactory {

    private static volatile ConcurrentHashMap<String, CircuitBreaker> breakerMap = new ConcurrentHashMap<>();


    /**
     * 获取 默认 断路器，划分维度为 service
     *
     * @param serviceName the service name
     * @return the circuit breaker
     */
    public static CircuitBreaker defaultCircuitBreaker(String serviceName) {
        CircuitBreaker circuitBreaker = breakerMap.get(serviceName);
        if (circuitBreaker == null) {
            CircuitBreaker newCircuitBreaker = new DefaultCircuitBreaker();
            breakerMap.putIfAbsent(serviceName, newCircuitBreaker);
            circuitBreaker = breakerMap.get(serviceName);
        }
        circuitBreaker.start();
        return circuitBreaker;
    }

}
