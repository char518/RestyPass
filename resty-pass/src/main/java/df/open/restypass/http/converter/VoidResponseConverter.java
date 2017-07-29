package df.open.restypass.http.converter;

import java.lang.reflect.Type;

/**
 * Void Response
 * Created by darrenfu on 17-7-29.
 */
public class VoidResponseConverter implements ResponseConverter<Void> {
    @Override
    public boolean support(Type type, String contentType) {
        if (contentType == null && type.getTypeName().equals("void")) {
            return true;
        }
        return false;
    }

    @Override
    public Void convert(byte[] body, Type type, String contentType) {
        return null;
    }
}
