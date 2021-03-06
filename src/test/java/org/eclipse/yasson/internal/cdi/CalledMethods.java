/*******************************************************************************
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Roman Grigoriadi
 ******************************************************************************/

package org.eclipse.yasson.internal.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.HashMap;
import java.util.Map;

/**
 * Mockito like method call counter for CDI beans.
 *
 * @author Roman Grigoriadi
 */
@ApplicationScoped
public class CalledMethods {

    /**
     * Maps method name to called count.
     */
    private Map<String, Integer> results = new HashMap<>();

    public void registerCall(@Observes MethodCalledEvent methodCalledEvent) {
        results.compute(methodCalledEvent.getMethodName(), (s, c) -> c == null ? 1 : c + 1);
    }

    public Map<String, Integer> getResults() {
        return results;
    }
}
