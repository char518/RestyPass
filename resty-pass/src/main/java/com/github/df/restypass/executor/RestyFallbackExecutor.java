package com.github.df.restypass.executor;

import com.github.df.restypass.annotation.RestyService;
import com.github.df.restypass.command.RestyCommand;
import com.github.df.restypass.command.RestyCommandConfig;
import com.github.df.restypass.enums.RestyCommandStatus;
import com.github.df.restypass.exception.execute.FallbackException;
import com.github.df.restypass.exception.execute.RequestException;
import com.github.df.restypass.exception.execute.RestyException;
import com.github.df.restypass.util.ClassTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认服务降级处理类
 * Created by darrenfu on 17-7-27.
 */
public class RestyFallbackExecutor implements FallbackExecutor, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(RestyFallbackExecutor.class);

    private ApplicationContext applicationContext;

    /**
     * 降级服务实例缓存
     */
    private static ConcurrentHashMap<String, Object> fallbackObjMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean executable(RestyCommand restyCommand) {
        if (restyCommand == null) {
            return false;
        }
        RestyCommandConfig commandConfig = restyCommand.getRestyCommandConfig();
        return restyCommand.getStatus() == RestyCommandStatus.FAILED
                && !(restyCommand.getFailException() instanceof RequestException) //客户端请求异常不进行降级
                && commandConfig.isFallbackEnabled()
                && (StringUtils.isNotEmpty(commandConfig.getFallbackBean())
                || (commandConfig.getFallbackClass() != null && commandConfig.getFallbackClass() != RestyService.Noop.class));
    }


    @Override
    public Object execute(RestyCommand restyCommand) throws FallbackException {

        RestyCommandConfig config = restyCommand.getRestyCommandConfig();
        String serviceName = restyCommand.getServiceName();
        Class fallbackClass = config.getFallbackClass();
        //获取降级服务实例
        Object fallbackObj = fallbackObjMap.get(serviceName);
        if (fallbackObj == null) {
            Object fo = getFallbackObject(restyCommand);
            fallbackObjMap.putIfAbsent(serviceName, fo);
            fallbackObj = fallbackObjMap.get(serviceName);
        }
        if (fallbackObj == null || fallbackObj == RestyService.Noop.noop) {
            throw new FallbackException("无法获取指定的降级服务实例:" + fallbackClass);
        }
        try {
            return findAndInvokeMethodInFallbackClass(restyCommand, fallbackObj);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new FallbackException(e);
        }
    }

    private Object getFallbackObject(RestyCommand restyCommand) {

        Object t = null;

        RestyCommandConfig config = restyCommand.getRestyCommandConfig();
        Class fallbackClz = config.getFallbackClass();
        String fallbackBean = config.getFallbackBean();
        // 优先按照bean name从spring注入指定bean
        if (StringUtils.isNotEmpty(fallbackBean)) {
            if (this.applicationContext != null) {
                t = this.applicationContext.getBean(fallbackBean);
                if (t != null && log.isDebugEnabled()) {
                    log.debug("注入降级服务:{}", t.getClass());
                }
            }
        } else if (fallbackClz != null && fallbackClz != RestyService.Noop.class) {
            // 否则初始化指定class
            t = ClassTools.instance(fallbackClz);
            if (t != null && log.isDebugEnabled()) {
                log.debug("使用降级服务:{}", fallbackClz);
            }
        }
        return t == null ? RestyService.Noop.noop : t;
    }

    private Object findAndInvokeMethodInFallbackClass(RestyCommand restyCommand,
                                                      Object fallbackObj) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method serviceMethod = restyCommand.getServiceMethod();
        String methodName = serviceMethod.getName();

        Class fallbackClass = fallbackObj.getClass();
        Method method = getMethod(fallbackClass, methodName, copyParamsWithException(restyCommand));
        if (method != null) {
            return invokeMethod(method, fallbackObj, copyArgsWithException(restyCommand));
        }
        method = getMethod(fallbackClass, methodName, serviceMethod.getParameterTypes());
        if (method == null) {
            throw new FallbackException(fallbackClass.getSimpleName() + "中没有发现没有合适的降级方法:" + methodName);
        }
        return invokeMethod(method, fallbackObj, restyCommand.getArgs());
    }


    private Method getMethod(Class clz, String methodName, Class<?>[] paramTypes) {
        try {
            return clz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Object invokeMethod(Method method, Object obj, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(obj, args);
    }

    private Class<?>[] copyParamsWithException(RestyCommand restyCommand) {
        Method serviceMethod = restyCommand.getServiceMethod();
        if (serviceMethod.getParameterTypes().length == 0) {
            return new Class[]{RestyException.class};
        }

        Class<?>[] paramTypes = new Class[serviceMethod.getParameterTypes().length + 1];
        paramTypes[0] = RestyException.class;
        System.arraycopy(serviceMethod.getParameterTypes(), 0, paramTypes, 1, serviceMethod.getParameterTypes().length);
        return paramTypes;
    }

    private Object[] copyArgsWithException(RestyCommand restyCommand) {
        if (restyCommand.getArgs() == null || restyCommand.getArgs().length == 0) {
            return new Object[]{restyCommand.getFailException()};
        }

        Object[] args = new Object[restyCommand.getArgs().length + 1];
        args[0] = restyCommand.getFailException();
        System.arraycopy(restyCommand.getArgs(), 0, args, 1, restyCommand.getArgs().length);
        return args;
    }


}
