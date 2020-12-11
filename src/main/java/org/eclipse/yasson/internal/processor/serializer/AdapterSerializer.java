package org.eclipse.yasson.internal.processor.serializer;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.stream.JsonGenerator;
import org.eclipse.yasson.internal.components.AdapterBinding;
import org.eclipse.yasson.internal.processor.SerializationContextImpl;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * TODO javadoc
 */
class AdapterSerializer implements ModelSerializer{

    private final JsonbAdapter<Object, Object> adapter;
    private final AdapterBinding adapterBinding;
    private final ModelSerializer delegate;


    @SuppressWarnings("unchecked")
    AdapterSerializer(AdapterBinding adapterBinding,
                      ModelSerializer delegate) {
        this.adapter = (JsonbAdapter<Object, Object>) adapterBinding.getAdapter();
        this.adapterBinding = adapterBinding;
        this.delegate = delegate;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContextImpl context) {
        try {
            delegate.serialize(adapter.adaptToJson(value), generator, context);
        } catch (Exception e) {
            throw new JsonbException(Messages.getMessage(MessageKeys.ADAPTER_EXCEPTION,
                                                         adapterBinding.getBindingType(),
                                                         adapterBinding.getToType(),
                                                         adapter.getClass()), e);
        }
    }

}
