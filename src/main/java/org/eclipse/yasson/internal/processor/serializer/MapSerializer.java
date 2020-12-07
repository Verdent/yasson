package org.eclipse.yasson.internal.processor.serializer;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;

/**
 * TODO javadoc
 */
abstract class MapSerializer implements ModelSerializer {

    private final ModelSerializer keySerializer;
    private final ModelSerializer valueSerializer;

    public MapSerializer(ModelSerializer keySerializer, ModelSerializer valueSerializer) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    public ModelSerializer getKeySerializer() {
        return keySerializer;
    }

    public ModelSerializer getValueSerializer() {
        return valueSerializer;
    }

    public static MapSerializer create(Class<?> keyClass, ModelSerializer keySerializer, ModelSerializer valueSerializer) {
        if (String.class.equals(keyClass) || Number.class.isAssignableFrom(keyClass) || Enum.class.isAssignableFrom(keyClass)) {
            return new StringKeyMapSerializer(keyClass, keySerializer, valueSerializer);
        } else if (Object.class.equals(keyClass)) {
            return new DynamicMapSerializer(keySerializer, valueSerializer);
        }
        return new ObjectKeyMapSerializer(keySerializer, valueSerializer);
    }

    private static final class DynamicMapSerializer extends MapSerializer {

        private final StringKeyMapSerializer stringMap;
        private final ObjectKeyMapSerializer objectMap;
        private MapSerializer serializer;

        public DynamicMapSerializer(ModelSerializer keySerializer,
                                    ModelSerializer valueSerializer) {
            super(keySerializer, valueSerializer);
            stringMap = new StringKeyMapSerializer(Object.class, keySerializer, valueSerializer);
            objectMap = new ObjectKeyMapSerializer(keySerializer, valueSerializer);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
            if (serializer == null) {
                //We have to be sure that Map with Object as a key contains only supported values for key:value format map.
                Map<Object, Object> map = (Map<Object, Object>) value;
                boolean suitable = true;
                for (Object key : map.keySet()) {
                    if (key == null) {
                        continue;
                    }
                    Class<?> keyClass = key.getClass();
                    if (String.class.equals(keyClass)
                            || Number.class.isAssignableFrom(keyClass)
                            || Enum.class.isAssignableFrom(keyClass)) {
                        continue;
                    }
                    //No other checks needed. Map is not suitable for normal key:value map. Wrapping object needs to be used.
                    suitable = false;
                    break;
                }
                serializer = suitable ? stringMap : objectMap;
            }
            serializer.serialize(value, generator, context);
        }

    }

    private static final class StringKeyMapSerializer extends MapSerializer {

        private static final BiConsumer<Object, JsonGenerator> ENUM =
                (value, generator) -> generator.writeKey(((Enum<?>) value).name());

        private static final BiConsumer<Object, JsonGenerator> OTHER =
                (value, generator) -> generator.writeKey(String.valueOf(value));

        private static final BiConsumer<Object, JsonGenerator> DYNAMIC =
                (value, generator) -> {
                    if (value != null && Enum.class.isAssignableFrom(value.getClass())) {
                        ENUM.accept(value, generator);
                    } else {
                        OTHER.accept(value, generator);
                    }
                };

        private final BiConsumer<Object, JsonGenerator> keyWriter;

        public StringKeyMapSerializer(Class<?> clazz,
                                      ModelSerializer keySerializer,
                                      ModelSerializer valueSerializer) {
            super(keySerializer, valueSerializer);
            if (Enum.class.isAssignableFrom(clazz)) {
                keyWriter = ENUM;
            } else if (Object.class.equals(clazz)) {
                keyWriter = DYNAMIC;
            } else {
                keyWriter = OTHER;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            generator.writeStartObject();
            map.forEach((key, val) -> {
                keyWriter.accept(key, generator);
                getValueSerializer().serialize(val, generator, context);
            });
            generator.writeEnd();
        }

    }

    private static final class ObjectKeyMapSerializer extends MapSerializer {

        public ObjectKeyMapSerializer(ModelSerializer keySerializer,
                                      ModelSerializer valueSerializer) {
            super(keySerializer, valueSerializer);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            generator.writeStartArray();
            map.forEach((key, val) -> {
                generator.writeStartObject();
                generator.writeKey("key");
                getKeySerializer().serialize(key, generator, context);
                generator.writeKey("value");
                getValueSerializer().serialize(val, generator, context);
                generator.writeEnd();
            });
            generator.writeEnd();
        }

    }

}
