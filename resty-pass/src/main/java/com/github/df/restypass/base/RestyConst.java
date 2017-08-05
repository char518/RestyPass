package com.github.df.restypass.base;

/**
 * 常量
 * Created by darrenfu on 17-6-30.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RestyConst {

    /**
     * 默认服务名称
     */
    public static final String SERVICE_DEFAULT = "DefaultService";
    /**
     * 默认机房名称
     */
    public static final String ROOM_DEFAULT = "room";

    /**
     * The constant HTTP_POST.
     */
    public static final String HTTP_POST = "POST";
    /**
     * The constant HTTP_GET.
     */
    public static final String HTTP_GET = "GET";

    /**
     * The constant CONTENT_TYPE.
     */
    public static final String CONTENT_TYPE = "Content-Type";
    /**
     * The constant APPLICATION_JSON.
     */
    public static final String APPLICATION_JSON = "application/json;charset=UTF-8";

    public static class Instance {
        /**
         * 权重属性
         */
        public static final String PROP_WEIGHT_KEY = "weight";

        public static final Integer PROP_WEIGHT_DEFAULT = 100;

        /**
         * 预热时间
         */
        public static final String PROP_WARMUP_KEY = "warmup";

        public static final Integer PROP_WARMUP_DEFAULT = 10 * 60 * 1000;

        /**
         * server开始服务时间
         */
        public static final String PROP_TIMESTAMP_KEY = "timestamp";
        public static final Long PROP_TIMESTAMP_DEFAULT = 0L;
    }
}
