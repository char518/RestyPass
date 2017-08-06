package com.github.df.restypass.filter;

import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.enums.CommandFilterType;
import com.github.df.restypass.exception.filter.FilterException;
import com.github.df.restypass.exception.filter.RejectException;
import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 流量控制过滤器
 * Created by darrenfu on 17-8-6.
 */
public class TrafficLimitFilter implements CommandFilter {

    private ConcurrentHashMap<String, RateLimiter> limiterMap = new ConcurrentHashMap<>();

    @Override
    public int order() {
        return 0;
    }

    @Override
    public boolean shouldFilter(RestyCommand restyCommand) {
        return restyCommand != null && restyCommand.getRestyCommandConfig().getLimit() > 0;
    }

    @Override
    public CommandFilterType getFilterType() {
        return CommandFilterType.BEFOR_EXECUTE;
    }

    @Override
    public void before(RestyCommand restyCommand) throws FilterException {
        RateLimiter rateLimiter = getRateLimiter(restyCommand);
        if (!rateLimiter.tryAcquire()) {
            throw new RejectException("rejected, exceeded the traffic limit: " + restyCommand.getRestyCommandConfig().getLimit());
        }
    }

    /**
     * 获取限速器
     *
     * @param restyCommand
     * @return limiter
     */
    private RateLimiter getRateLimiter(RestyCommand restyCommand) {
        String key = restyCommand.getServiceMethod() + "@" + restyCommand.getPath();
        RateLimiter rateLimiter = limiterMap.get(key);
        if (rateLimiter == null) {
            limiterMap.putIfAbsent(key, RateLimiter.create(restyCommand.getRestyCommandConfig().getLimit()));
            rateLimiter = limiterMap.get(key);
        } else if (rateLimiter.getRate() != restyCommand.getRestyCommandConfig().getLimit()) {
            // 更新rate
            rateLimiter.setRate(restyCommand.getRestyCommandConfig().getLimit());
        }
        return rateLimiter;
    }
}

