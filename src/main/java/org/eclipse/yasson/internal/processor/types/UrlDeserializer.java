package org.eclipse.yasson.internal.processor.types;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
class UrlDeserializer extends TypeDeserializer {

    UrlDeserializer(TypeDeserializerBuilder builder) {
        super(builder);
    }

    @Override
    Object deserializeValue(String value, DeserializationContextImpl context, Type rType) {
        URL url = null;
        try {
            url = new URL(value);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}
