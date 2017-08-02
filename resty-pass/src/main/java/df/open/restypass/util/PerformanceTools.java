package df.open.restypass.util;

import df.open.restypass.command.RestyFuture;
import df.open.restypass.executor.RestyCommandExecutor;
import df.open.restypass.spring.proxy.RestyProxyInvokeHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试工具
 * Created by darrenfu on 17-8-2.
 */
public class PerformanceTools {


    public static Map<String, Long> getPerformance() {

        Map<String, Long> map = new HashMap<>();

        map.put(RestyProxyInvokeHandler.class.getSimpleName(),
                (RestyProxyInvokeHandler.time.longValue() / RestyProxyInvokeHandler.count.longValue()));

        map.put(RestyCommandExecutor.class.getSimpleName(),
                (RestyCommandExecutor.time.longValue() / RestyCommandExecutor.count.longValue()));

        map.put(RestyFuture.class.getSimpleName(),
                (RestyFuture.time.longValue() / RestyFuture.count.longValue()));

        return map;
    }

}
