package org.eclipse.yasson.internal.serializer;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.config.BinaryDataStrategy;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.JsonbContext;
import org.eclipse.yasson.internal.SerializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
abstract class ArraySerializer implements ModelSerializer {

    private static final Map<Class<?>, Function<ModelSerializer, ArraySerializer>> ARRAY_SERIALIZERS;

    static {
        Map<Class<?>, Function<ModelSerializer, ArraySerializer>> cache = new HashMap<>();
        cache.put(boolean[].class, ArraySerializer.BooleanArraySerializer::new);
        cache.put(byte[].class, ArraySerializer.ByteArraySerializer::new);
        cache.put(char[].class, ArraySerializer.CharacterArraySerializer::new);
        cache.put(double[].class, ArraySerializer.DoubleArraySerializer::new);
        cache.put(float[].class, ArraySerializer.FloatArraySerializer::new);
        cache.put(int[].class, ArraySerializer.IntegerArraySerializer::new);
        cache.put(long[].class, ArraySerializer.LongArraySerializer::new);
        cache.put(short[].class, ArraySerializer.ShortArraySerializer::new);
        ARRAY_SERIALIZERS = Collections.unmodifiableMap(cache);
    }

    private final ModelSerializer valueSerializer;

    protected ArraySerializer(ModelSerializer valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public static ModelSerializer create(Class<?> arrayType,
                                         JsonbContext jsonbContext,
                                         ModelSerializer modelSerializer) {
        String binaryDataStrategy = jsonbContext.getConfigProperties().getBinaryDataStrategy();
        if (byte[].class.equals(arrayType) && !binaryDataStrategy.equals(BinaryDataStrategy.BYTE)) {
            return new Base64ByteArraySerializer(binaryDataStrategy);
        }
        if (ARRAY_SERIALIZERS.containsKey(arrayType)) {
            return ARRAY_SERIALIZERS.get(arrayType).apply(modelSerializer);
        }
        return new ObjectArraySerializer(modelSerializer);
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        generator.writeStartArray();
        serializeArray(value, generator, context);
        generator.writeEnd();
    }

    abstract void serializeArray(Object value, JsonGenerator generator, SerializationContextImpl context);

    protected ModelSerializer getValueSerializer() {
        return valueSerializer;
    }

    private static final class ByteArraySerializer extends ArraySerializer {

        ByteArraySerializer(ModelSerializer valueSerializer) {
            super(valueSerializer);
        }

        @Override
        public void serializeArray(Object value, JsonGenerator generator, SerializationContextImpl context) {
            byte[] array = (byte[]) value;
            for (byte b : array) {
                getValueSerializer().serialize(b, generator, context);
            }
        }

    }

    private static final class Base64ByteArraySerializer implements ModelSerializer {

        private final Base64.Encoder encoder;

        Base64ByteArraySerializer(String strategy) {
            this.encoder = getEncoder(strategy);
        }

        @Override
        public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
            byte[] array = (byte[]) value;
            generator.write(encoder.encodeToString(array));
        }

        private Base64.Encoder getEncoder(String strategy) {
            switch (strategy) {
            case BinaryDataStrategy.BASE_64:
                return Base64.getEncoder();
            case BinaryDataStrategy.BASE_64_URL:
                return Base64.getUrlEncoder();
            default:
                throw new JsonbException(Messages.getMessage(MessageKeys.INTERNAL_ERROR, "Invalid strategy: " + strategy));
            }
        }
    }

    private static final class ShortArraySerializer extends ArraySerializer {

        ShortArraySerializer(ModelSerializer valueSerializer) {
            super(valueSerializer);
        }

        @Override
        public void serializeArray(Object value, JsonGenerator generator, SerializationContextImpl context) {
            short[] array = (short[]) value;
            for (short s : array) {
                getValueSerializer().serialize(s, generator, context);
            }
        }

    }

    private static final class IntegerArraySerializer extends ArraySerializer {

        IntegerArraySerializer(ModelSerializer valueSerializer) {
            super(valueSerializer);
        }

        @Override
        public void serializeArray(Object value, JsonGenerator generator, SerializationContextImpl context) {
            int[] array = (int[]) value;
            for (int i : array) {
                getValueSerializer().serialize(i, generator, context);
            }
        }

    }

    private static final class LongArraySerializer extends ArraySerializer {

        LongArraySerializer(ModelSerializer valueSerializer) {
            super(valueSerializer);
        }

        @Override
        public void serializeArray(Object value, JsonGenerator generator, SerializationContextImpl context) {
            long[] array = (long[]) value;
            for (long l : array) {
                getValueSerializer().serialize(l, generator, context);
            }
        }

    }

    private static final class FloatArraySerializer extends ArraySerializer {

        FloatArraySerializer(ModelSerializer valueSerializer) {
            super(valueSerializer);
        }

        @Override
        public void serializeArray(Object value, JsonGenerator generator, SerializationContextImpl context) {
            float[] array = (float[]) value;
            for (float f : array) {
                getValueSerializer().serialize(f, generator, context);
            }
        }

    }

    private static final class DoubleArraySerializer extends ArraySerializer {

        DoubleArraySerializer(ModelSerializer valueSerializer) {
            super(valueSerializer);
        }

        @Override
        public void serializeArray(Object value, JsonGenerator generator, SerializationContextImpl context) {
            double[] array = (double[]) value;
            for (double d : array) {
                getValueSerializer().serialize(d, generator, context);
            }
        }

    }

    private static final class BooleanArraySerializer extends ArraySerializer {

        BooleanArraySerializer(ModelSerializer valueSerializer) {
            super(valueSerializer);
        }

        @Override
        public void serializeArray(Object value, JsonGenerator generator, SerializationContextImpl context) {
            boolean[] array = (boolean[]) value;
            for (boolean b : array) {
                generator.write(b);
            }
        }

    }

    private static final class CharacterArraySerializer extends ArraySerializer {

        CharacterArraySerializer(ModelSerializer valueSerializer) {
            super(valueSerializer);
        }

        @Override
        public void serializeArray(Object value, JsonGenerator generator, SerializationContextImpl context) {
            char[] array = (char[]) value;
            for (char c : array) {
                generator.write(Character.valueOf(c).toString());
            }
        }

    }

    private static final class ObjectArraySerializer extends ArraySerializer {

        ObjectArraySerializer(ModelSerializer valueSerializer) {
            super(valueSerializer);
        }

        @Override
        public void serializeArray(Object value, JsonGenerator generator, SerializationContextImpl context) {
            Object[] array = (Object[]) value;
            for (Object o : array) {
                getValueSerializer().serialize(o, generator, context);
            }
        }

    }

}
