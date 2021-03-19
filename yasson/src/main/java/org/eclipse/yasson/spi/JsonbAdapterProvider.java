package org.eclipse.yasson.spi;

import java.util.Set;

import jakarta.json.bind.adapter.JsonbAdapter;

/**
 * TODO javadoc
 */
public interface JsonbAdapterProvider {

    Set<JsonbAdapter<?, ?>> createAdapters();

}
