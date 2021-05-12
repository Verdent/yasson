package org.eclipse.yasson.mpconfig;

import jakarta.json.bind.adapter.JsonbAdapter;

/**
 * TODO javadoc
 */
public class TestAdapter implements JsonbAdapter<String, String> {
    @Override
    public String adaptToJson(String obj) throws Exception {
        return null;
    }

    @Override
    public String adaptFromJson(String obj) throws Exception {
        return null;
    }
}
