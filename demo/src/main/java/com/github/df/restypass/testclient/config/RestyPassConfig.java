package com.github.df.restypass.testclient.config;

import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.command.RestyCommandContext;
import com.github.df.restypass.enums.CommandFilterType;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.exception.filter.FilterException;
import com.github.df.restypass.executor.CommandExecutor;
import com.github.df.restypass.executor.FallbackExecutor;
import com.github.df.restypass.executor.RestyCommandExecutor;
import com.github.df.restypass.executor.RestyFallbackExecutor;
import com.github.df.restypass.filter.CommandFilter;
import com.github.df.restypass.lb.server.CloudConsulServerContext;
import com.github.df.restypass.lb.server.ConfigurableServerContext;
import com.github.df.restypass.lb.server.ServerContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by darrenfu on 17-7-31.
 */
@Configuration
public class RestyPassConfig {

    /**
     * Fallback executor fallback executor.
     *
     * @return the fallback executor
     */
    @Bean
    public FallbackExecutor fallbackExecutor() {
        return new RestyFallbackExecutor();
    }

    /**
     * Server context server context.
     *
     * @return the server context
     */
    @Bean
    public ServerContext serverContext() {
        return new ConfigurableServerContext();
    }

    /**
     * Command executor command executor.
     *
     * @param commandContext the command context
     * @return the command executor
     */
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    public CommandExecutor commandExecutor(RestyCommandContext commandContext) {
        return new RestyCommandExecutor(commandContext);
    }

    /**
     * Custom command filter command filter.
     *
     * @return the command filter
     */
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
