package df.open.restypass.spring.proxy;

import df.open.restypass.base.DefaultRestyPassFactory;
import df.open.restypass.base.RestyPassFactory;
import df.open.restypass.command.RestyCommandContext;
import df.open.restypass.executor.CommandExecutor;
import df.open.restypass.executor.FallbackExecutor;
import df.open.restypass.lb.server.ServerContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.lang.reflect.Proxy;
import java.util.concurrent.locks.ReentrantLock;

/**
 * RestyService代理工厂类
 *
 * @author darren-fu
 */
@Data
@Slf4j
public class RestyProxyBeanFactory implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {

    private Class<?> type;

    private RestyCommandContext restyCommandContext;

    private ApplicationContext applicationContext;

    private ServerContext serverContext;

    private CommandExecutor commandExecutor;

    private FallbackExecutor fallbackExecutor;

    private RestyPassFactory factory;

    /**
     * 是否完成初始化
     */
    private boolean inited = false;

    private ReentrantLock initLock = new ReentrantLock();

    @Override
    public Object getObject() throws Exception {
        return createProxy(type, restyCommandContext);
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    protected Object createProxy(Class type, RestyCommandContext restyCommandContext) {

        if (!inited) {
            initLock.lock();
            try {
                if (!inited) {
                    this.serverContext = getBean(ServerContext.class);
                    this.commandExecutor = getBean(CommandExecutor.class);
                    this.fallbackExecutor = getBean(FallbackExecutor.class);
                    if (serverContext instanceof ApplicationContextAware) {
                        ApplicationContextAware contextAware = (ApplicationContextAware) serverContext;
                        contextAware.setApplicationContext(this.applicationContext);
                    }
                    inited = true;
                }
            } finally {
                initLock.unlock();
            }
        }

        Object proxy = null;
        try {
            RestyProxyInvokeHandler interfaceIvkHandler =
                    new RestyProxyInvokeHandler(restyCommandContext, commandExecutor, fallbackExecutor, serverContext);
            proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, interfaceIvkHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return proxy;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(type, "type不能为空");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private <T> T getBean(Class<T> clz) {
        T t = null;
        try {
            if (this.applicationContext != null) {
                t = this.applicationContext.getBean(clz);
            }
            if (t == null) {
                log.info("{}使用默认配置", clz);
                t = DefaultRestyPassFactory.getDefaultBean(clz);
            } else {
                log.info("{}使用Spring注入", clz);
            }
            if (t == null) {
                throw new RuntimeException("无法获取Bean:" + clz);
            }
        } catch (BeansException ex) {
            log.info("{}使用默认配置", clz);
            t = DefaultRestyPassFactory.getDefaultBean(clz);
        }
        return t;
    }
}
