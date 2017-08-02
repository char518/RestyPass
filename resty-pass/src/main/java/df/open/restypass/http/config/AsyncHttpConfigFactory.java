package df.open.restypass.http.config;

import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

/**
 * Created by darrenfu on 17-6-19.
 */
public class AsyncHttpConfigFactory {


    /**
     * 生成默认的httpclient config
     *
     * @return the config
     */
    public static AsyncHttpClientConfig createConfig(int connectTimeout, int requestTimeout) {
        return new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(connectTimeout)
                .setRequestTimeout(requestTimeout)
                .setMaxConnectionsPerHost(10000)
                .setValidateResponseHeaders(false)
                .setMaxRequestRetry(0)
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
