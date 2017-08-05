package com.github.df.restypass.http.config;

import io.netty.util.HashedWheelTimer;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.netty.channel.DefaultChannelPool;

/**
 * HttpClient config 工厂类
 * Created by darrenfu on 17-6-19.
 */
public class AsyncHttpConfigFactory {


    /**
     * 生成默认的httpclient config
     *
     * @return the config
     */
    public static AsyncHttpClientConfig createConfig(int connectTimeout, int requestTimeout) {
        HashedWheelTimer timer = new HashedWheelTimer();
        timer.start();
        DefaultChannelPool channelPool = new DefaultChannelPool(60000,
                -1,
                DefaultChannelPool.PoolLeaseStrategy.LIFO,
                timer,
                1000);

        return new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(connectTimeout)
                .setRequestTimeout(requestTimeout)
                .setMaxConnectionsPerHost(10000)
                .setValidateResponseHeaders(false)
                .setMaxRequestRetry(0)
                .setChannelPool(channelPool)
                .build();
    }

    /**
     * Refresh config async http client config.
     *
     * @return the async http client config
     */
    public static AsyncHttpClientConfig refreshConfig() {
        return null;
    }


}
