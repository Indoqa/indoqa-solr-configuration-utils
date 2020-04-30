/*
 *   Licensed to the Indoqa Software Design und Beratung GmbH (Indoqa) under
 *   one or more contributor license agreements. See the NOTICE file distributed
 *   with this work for additional information regarding copyright ownership.
 *   Indoqa licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.indoqa.solr.utils.validation.results;

import java.util.HashMap;
import java.util.Map;

public class AttributesValidationResult {

    private String name;
    private String className;

    private ValuesOrigin valuesOrigin;

    private Map<String, Object> attributesOnlyInSchema = new HashMap<>();
    private Map<String, DifferentValue> differentAttributeValues = new HashMap<>();
    private Map<String, Object> attributesStillInSolr = new HashMap<>();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isEmpty() {
        return ValuesOrigin.BOTH.equals(this.valuesOrigin) && this.attributesOnlyInSchema.isEmpty()
            && this.differentAttributeValues.isEmpty() && this.attributesStillInSolr.isEmpty();
    }

    public ValuesOrigin getValuesOrigin() {
        return valuesOrigin;
    }

    public void setValuesOrigin(ValuesOrigin valuesOrigin) {
        this.valuesOrigin = valuesOrigin;
    }

    public void addAttributeOnlyInSchema(String attribute, Object value) {
        this.attributesOnlyInSchema.put(attribute, value);
    }

    public void addDifferentAttribute(String attribute, Object expected, Object actual) {
        this.differentAttributeValues.put(attribute, DifferentValue.of(expected, actual));
    }

    public void addAttributeStillInSolr(String attribute, Object value) {
        this.attributesStillInSolr.put(attribute, value);
    }

    public Map<String, Object> getAttributesOnlyInSchema() {
        return this.attributesOnlyInSchema;
    }

    public Map<String, DifferentValue> getDifferentAttributeValues() {
        return this.differentAttributeValues;
    }

    public Map<String, Object> getAttributesStillInSolr() {
        return this.attributesStillInSolr;
    }
}
