package com.github.df.restypass.filter;

import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.enums.CommandFilterType;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.exception.filter.FilterException;

/**
 * 流量控制过滤器
 * Created by darrenfu on 17-8-6.
 */
public class TrafficLimitFilter implements CommandFilter {
    @Override
    public int order() {
        return 0;
    }

    @Override
    public boolean shouldFilter(RestyCommand restyCommand) {
        return false;
    }

    @Override
    public CommandFilterType getFilterType() {
        return null;
    }

    @Override
    public void before(RestyCommand restyCommand) throws FilterException {

    }

    @Override
    public void after(RestyCommand restyCommand, Object result) {

    }

    @Override
    public void error(RestyCommand restyCommand, RestyException ex) {

    }
}
