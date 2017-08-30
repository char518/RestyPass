package com.github.df.restypass.annotation.processor;

import com.github.df.restypass.annotation.RestyMethod;
import com.github.df.restypass.command.RestyCommandConfig;
import com.github.df.restypass.lb.server.VersionCondition;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * RestyMethod 注解处理器
 * 设置并覆盖 RestyCommandConfig
 * Created by darrenfu on 17-6-24.
 */
@SuppressWarnings("WeakerAccess")
public class RestyMethodProcessor implements RestyAnnotationProcessor {


    @Override
    public RestyCommandConfig processor(Annotation annotation, RestyCommandConfig properties) {

        if (annotation != null && annotation.annotationType().equals(RestyMethod.class)) {

            RestyMethod restyMethod = (RestyMethod) annotation;

            // 设置重试次数
            setRetry(restyMethod, properties);
            // 设置是否打开降级
            setFallbackEnabled(restyMethod, properties);
            // 设置是否打开断路器
            setCircuitBreakEnabled(restyMethod, properties);
            // 设置流量
            setLimit(restyMethod, properties);
            // 设置路由版本
            serVersion(restyMethod, properties);
        }
        return properties;
    }


    /**
     * Sets retry.
     *
     * @param restyMethod the resty method
     * @param properties  the properties
     */
    protected void setRetry(RestyMethod restyMethod, RestyCommandConfig properties) {
        int retry = restyMethod.retry();
        if (retry >= 0) {
            properties.setRetry(retry);
        }
    }


    /**
     * Sets fallback enabled.
     *
     * @param restyMethod the resty method
     * @param properties  the properties
     */
    protected void setFallbackEnabled(RestyMethod restyMethod, RestyCommandConfig properties) {
        String fallbackEnabled = restyMethod.fallbackEnabled();
        if ("true".equalsIgnoreCase(fallbackEnabled)) {
            properties.setFallbackEnabled(true);
        } else if ("false".equalsIgnoreCase(fallbackEnabled)) {
            properties.setFallbackEnabled(false);
        }
    }

    /**
     * Sets circuit break enabled.
     *
     * @param restyMethod the resty method
     * @param properties  the properties
     */
    protected void setCircuitBreakEnabled(RestyMethod restyMethod, RestyCommandConfig properties) {
        String circuitBreakEnabled = restyMethod.circuitBreakEnabled();
        if ("true".equalsIgnoreCase(circuitBreakEnabled)) {
            properties.setCircuitBreakEnabled(true);
        } else if ("false".equalsIgnoreCase(circuitBreakEnabled)) {
            properties.setCircuitBreakEnabled(false);
        }

        String forceBreakEnabled = restyMethod.forceBreakEnabled();
        if ("true".equalsIgnoreCase(forceBreakEnabled)) {
            properties.setCircuitBreakEnabled(true);
            properties.setForceBreakEnabled(true);
        } else if ("false".equalsIgnoreCase(forceBreakEnabled)) {
            properties.setForceBreakEnabled(false);
        }

    }

    /**
     * Sets limit.
     *
     * @param restyMethod the resty method
     * @param properties  the properties
     */
    protected void setLimit(RestyMethod restyMethod, RestyCommandConfig properties) {
        if (restyMethod.limit() > 0) {
            properties.setLimit(restyMethod.limit());
        }
    }

    /**
     * Ser version.
     *
     * @param restyMethod the resty method
     * @param properties  the properties
     */
    protected void serVersion(RestyMethod restyMethod, RestyCommandConfig properties) {
        if (restyMethod.version() != null) {
            List<VersionCondition> versionConditionList = new ArrayList<>();

            for (String version : restyMethod.version()) {
                if (StringUtils.isNotEmpty(version)) {
                    versionConditionList.add(VersionCondition.create(version));
                }
            }
            if (versionConditionList.size() > 0) {
                properties.setVersion(versionConditionList);
            }
        }
    }


}
