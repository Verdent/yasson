package org.eclipse.yasson.internal.processor.deserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import jakarta.json.stream.JsonParser;
import org.eclipse.yasson.internal.InstanceCreator;
import org.eclipse.yasson.internal.JsonbConfigProperties;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;

/**
 * TODO javadoc
 */
public class MapInstanceCreator implements ModelDeserializer<JsonParser> {

    private final MapDeserializer delegate;
    private final InstanceCreator instanceCreator;
    private final JsonbConfigProperties configProperties;
    private final Class<?> clazz;

    public MapInstanceCreator(MapDeserializer delegate,
                              InstanceCreator instanceCreator,
                              JsonbConfigProperties configProperties, Class<?> clazz) {
        this.delegate = delegate;
        this.instanceCreator = instanceCreator;
        this.configProperties = configProperties;
        this.clazz = clazz;
    }

    @Override
    public Object deserialize(JsonParser value, DeserializationContextImpl context) {
        Map<?, ?> map = createInstance(clazz);
        context.setInstance(map);
        return delegate.deserialize(value, context);
    }

    private Map<?, ?> createInstance(Class<?> clazz) {
        return clazz.isInterface()
                ? getMapImpl(clazz)
                : (Map<?, ?>) InstanceCreator.createInstance(clazz);
    }

    private Map<?, ?> getMapImpl(Class<?> ifcType) {
        if (ConcurrentMap.class.isAssignableFrom(ifcType)) {
            if (SortedMap.class.isAssignableFrom(ifcType) || NavigableMap.class.isAssignableFrom(ifcType)) {
                return new ConcurrentSkipListMap<>();
            } else {
                return new ConcurrentHashMap<>();
            }
        }
        // SortedMap, NavigableMap
        if (SortedMap.class.isAssignableFrom(ifcType)) {
            Class<?> defaultMapImplType = configProperties.getDefaultMapImplType();
            return SortedMap.class.isAssignableFrom(defaultMapImplType)
                    ? (Map<?, ?>) InstanceCreator.createInstance(defaultMapImplType)
                    : new TreeMap<>();
        }
        return new HashMap<>();
    }

}
