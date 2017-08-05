package com.github.df.restypass.util;

/**
 * Class工具类
 * Created by darrenfu on 17-7-23.
 */
@SuppressWarnings("unused")
public class ClassTools {

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
            e.printStackTrace();
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
