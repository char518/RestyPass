package df.open.restypass.http.converter;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * Http响应结果转换器
 * Created by darrenfu on 17-7-19.
 *
 * @param <T> the type parameter
 */
public interface ResponseConverter<T> {

    /**
     * Support boolean.
     *
     * @param type        the type
     * @param contentType the content type
     * @return the boolean
     */
    boolean support(Type type, String contentType);

    /**
     * Convert t.
     *
     * @param body        the body
     * @param type        the type
     * @param contentType the content type
     * @return the t
     */
    T convert(byte[] body, Type type, String contentType);

    /**
     * Gets charset.
     *
     * @param contentType the content type
     * @return the charset
     */
    default Charset getCharset(String contentType) {
        try {
            if (StringUtils.isNotEmpty(contentType) && contentType.lastIndexOf(";charset=") > 0) {

                int i = contentType.lastIndexOf(";charset=");
                if (i > 0 && contentType.length() > i + 9) {
                    return Charset.forName(contentType.substring(i + 9));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Charset.defaultCharset();
    }

}
