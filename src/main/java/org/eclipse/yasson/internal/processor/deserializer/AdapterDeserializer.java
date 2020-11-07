package org.eclipse.yasson.internal.processor.deserializer;

import java.lang.reflect.Type;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.adapter.JsonbAdapter;
import org.eclipse.yasson.internal.components.AdapterBinding;
import org.eclipse.yasson.internal.processor.DeserializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
public class AdapterDeserializer implements ModelDeserializer<Object> {

    private final JsonbAdapter<Object, Object> adapter;
    private final AdapterBinding adapterBinding;
    private final ModelDeserializer<Object> delegate;

    public AdapterDeserializer(AdapterBinding adapterBinding,
                               ModelDeserializer<Object> delegate) {
        this.adapterBinding = adapterBinding;
        this.adapter = (JsonbAdapter<Object, Object>) adapterBinding.getAdapter();
        this.delegate = delegate;
    }

    @Override
    public Object deserialize(Object value, DeserializationContextImpl context, Type rType) {
        try {
            return delegate.deserialize(adapter.adaptFromJson(value), context, rType);
        } catch (Exception e) {
            throw new JsonbException(Messages.getMessage(MessageKeys.ADAPTER_EXCEPTION,
                                                         adapterBinding.getBindingType(),
                                                         adapterBinding.getToType(),
                                                         adapterBinding.getAdapter().getClass()), e);
        }
    }

}