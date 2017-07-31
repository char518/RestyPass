package df.open.restypass.testclient.config;

import df.open.restypass.command.RestyCommandContext;
import df.open.restypass.executor.CommandExecutor;
import df.open.restypass.executor.FallbackExecutor;
import df.open.restypass.executor.RestyCommandExecutor;
import df.open.restypass.executor.RestyFallbackExecutor;
import df.open.restypass.lb.server.ConfigurableServerContext;
import df.open.restypass.lb.server.ServerContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by darrenfu on 17-7-31.
 */
@Configuration
public class RestyPassConfig {

    @Bean
    public FallbackExecutor fallbackExecutor() {
        System.out.println("init fallback..");
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
