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

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class FieldAttributesValidationResult extends ValidationResult implements Comparable<FieldAttributesValidationResult> {

    private String name;
    private String className;

    private ValuesOrigin valuesOrigin;

    private Map<String, Object> attributesOnlyInSchema = new TreeMap<>();
    private Map<String, DifferentValue> differentAttributeValues = new TreeMap<>();
    private Map<String, Object> attributesStillInSolr = new TreeMap<>();

    public String getId() {
        if (name != null) {
            return name;
        }
        return className;
    }

    @Override
    public Optional<List<? extends ValidationResult>> getModified() {
        if (this.differentAttributeValues.isEmpty()) {
            return super.getModified();
        }
        return Optional.of(singletonList(this));
    }

    @Override
    public Optional<List<? extends ValidationResult>> getStillInSolr() {
        if (this.attributesStillInSolr.isEmpty()) {
            return super.getStillInSolr();
        }
        return Optional.of(singletonList(this));
    }

    @Override
    public Optional<List<? extends ValidationResult>> getOnlyInSchema() {
        if (this.attributesOnlyInSchema.isEmpty()) {
            return super.getOnlyInSchema();
        }
        return Optional.of(singletonList(this));
    }

    public boolean isEmpty() {
        return this.attributesOnlyInSchema.isEmpty() && this.differentAttributeValues.isEmpty() && this.attributesStillInSolr.isEmpty()
            && this.emptyValueOrigin();
    }

    @Override
    public int compareTo(FieldAttributesValidationResult o) {
        if (this.getClassName() != null && o != null && o.getClassName() != null) {
            return this.getClassName().compareTo(o.getClassName());
        }
        return 0;
    }

    private boolean emptyValueOrigin() {
        return this.valuesOrigin == null || ValuesOrigin.BOTH.equals(this.valuesOrigin);
    }

    @Override
    public String getErrorMessage(int levelOfIndentation) {
        StringBuilder result = new StringBuilder();

        appendIndentation(result, levelOfIndentation);
        result.append(getType());
        result.append(":");

        if (name != null) {
            result.append(" '");
            result.append(name);
            result.append("':");
        }

        if (className != null) {
            result.append(" Class: '");
            result.append(className);
            result.append("':");
        }

        if (!this.attributesOnlyInSchema.isEmpty()) {
            appendNewlineIndentation(result, levelOfIndentation + 1);
            result.append("Attributes only in Schema:");
            appendNewlineIndentation(result, levelOfIndentation + 2);
            result.append(this.attributesOnlyInSchema);
        }

        if (!this.differentAttributeValues.isEmpty()) {
            appendNewlineIndentation(result, levelOfIndentation + 1);
            result.append("Attributes with different values:");
            appendNewlineIndentation(result, levelOfIndentation + 2);
            result.append(this.differentAttributeValues);
        }

        if (!this.attributesStillInSolr.isEmpty()) {
            appendNewlineIndentation(result, levelOfIndentation + 1);
            result.append("Attributes still in Solr:");
            appendNewlineIndentation(result, levelOfIndentation + 2);
            result.append(this.attributesStillInSolr);
        }

        appendToErrorMessage(levelOfIndentation + 1, result);

        return result.toString();
    }

    protected void appendToErrorMessage(int levelOfIndentation, StringBuilder builder) {
        // default do nothing
    }

    protected String getType() {
        return "Field";
    }

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

    public void addAttributeValidation(AttributesValidationResult attributesValidation) {
        this.setClassName(attributesValidation.getClassName());
        this.setValuesOrigin(attributesValidation.getValuesOrigin());
        this.attributesOnlyInSchema.putAll(attributesValidation.getAttributesOnlyInSchema());
        this.attributesStillInSolr.putAll(attributesValidation.getAttributesStillInSolr());
        this.differentAttributeValues.putAll(attributesValidation.getDifferentAttributeValues());
    }
}
