package org.eclipse.yasson.mpconfig;

import jakarta.json.bind.adapter.JsonbAdapter;

/**
 * TODO javadoc
 */
public class TestAdapter2 implements JsonbAdapter<Object, String> {

    @Override
    public String adaptToJson(Object obj) throws Exception {
        return null;
    }

    @Override
    public String adaptFromJson(String obj) throws Exception {
        return null;
    }
}
