package com.github.df.restypass.testclient.entity;

/**
 * Created by darrenfu on 17-4-1.
 *
 * @param <T> the type parameter
 */
public class Response<T> {
    /**
     * The Info.
     */
    T info;

    /**
     * Gets info.
     *
     * @return the info
     */
    public T getInfo() {
        return info;
    }

    /**
     * Sets info.
     *
     * @param info the info
     */
    public void setInfo(T info) {
        this.info = info;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets code.
     *
     * @param code the code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * The Code.
     */
    String code;
}
