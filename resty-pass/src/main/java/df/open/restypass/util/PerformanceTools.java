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

    /**
     * just for print performance log
     *
     * @return
     */
    public static Map<String, Long> getPerformance() {
        Map<String, Long> map = new HashMap<>();
        map.put(RestyFuture.class.getSimpleName(),
                (RestyFuture.time.longValue() / RestyFuture.count.longValue()));
        return map;
    }

}
