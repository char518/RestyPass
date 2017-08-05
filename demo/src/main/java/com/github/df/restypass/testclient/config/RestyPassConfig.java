package com.github.df.restypass.testclient.config;

import com.github.df.restypass.command.RestyCommandContext;
import com.github.df.restypass.executor.CommandExecutor;
import com.github.df.restypass.executor.FallbackExecutor;
import com.github.df.restypass.executor.RestyCommandExecutor;
import com.github.df.restypass.executor.RestyFallbackExecutor;
import com.github.df.restypass.lb.server.ConfigurableServerContext;
import com.github.df.restypass.lb.server.ServerContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by darrenfu on 17-7-31.
 */
@Configuration
public class RestyPassConfig {

    @Bean
    public FallbackExecutor fallbackExecutor() {
        System.out.println("initProps fallback..");
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


}
