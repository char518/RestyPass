package com.github.df.restypass.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class工具类
 * Created by darrenfu on 17-7-23.
 */
@SuppressWarnings("unused")
public class ClassTools {
    private static final Logger log = LoggerFactory.getLogger(ClassTools.class);

    /**
     * Cast to t.
     *
     * @param <T> the type parameter
     * @param obj the obj
     * @param clz the clz
     * @return the t
     */
    public static <T> T castTo(Object obj, Class<T> clz) {
        return clz.cast(obj);
    }

    /**
     * Instance t.
     *
     * @param <T>       the type parameter
     * @param targetClz the target clz
     * @return the t
     */
    public static <T> T instance(Class<T> targetClz) {
        try {
            return targetClz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.warn("Instantiation failed:{}", e.getMessage(), e);
        }
        return null;
    }


    /**
     * Has class boolean.
     *
     * @param clz the clz
     * @return the boolean
     */
    public static boolean hasClass(String clz) {
        try {
            Class.forName(clz);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }


    /**
     * Gets class.
     *
     * @param clz the clz
     * @return the class
     */
    public static Class<?> getClass(String clz) {
        try {
            return Class.forName(clz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
