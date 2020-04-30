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

import java.util.*;

public abstract class AbstractFieldsValidationResult<T extends FieldAttributesValidationResult> extends ValidationResult {

    private List<T> fieldsModified = new ArrayList<>();
    private List<T> fieldsOnlyInSchema = new ArrayList<>();
    private List<T> fieldsStillInSolr = new ArrayList<>();

    @Override
    public Optional<List<? extends ValidationResult>> getModified() {
        if (this.fieldsModified.isEmpty()) {
            return super.getModified();
        }
        return Optional.of(Collections.singletonList(this));
    }

    @Override
    public Optional<List<? extends ValidationResult>> getStillInSolr() {
        if (this.fieldsStillInSolr.isEmpty()) {
            return super.getStillInSolr();
        }
        return Optional.of(Collections.singletonList(this));
    }

    @Override
    public Optional<List<? extends ValidationResult>> getOnlyInSchema() {
        if (this.fieldsOnlyInSchema.isEmpty()) {
            return super.getOnlyInSchema();
        }
        return Optional.of(Collections.singletonList(this));
    }

    public void addFieldsModified(T fieldValidation) {
        if (fieldValidation.isEmpty()) {
            return;
        }
        this.fieldsModified.add(fieldValidation);
    }

    public void addFieldsOnlyInSchema(T fieldValidation) {
        if (fieldValidation.isEmpty()) {
            return;
        }
        this.fieldsOnlyInSchema.add(fieldValidation);
    }

    public void addFieldStillInSolr(T fieldValidation) {
        if (fieldValidation.isEmpty()) {
            return;
        }
        this.fieldsStillInSolr.add(fieldValidation);
    }

    @Override
    public boolean isEmpty() {
        return this.fieldsModified.isEmpty() && this.fieldsOnlyInSchema.isEmpty() && this.fieldsStillInSolr.isEmpty();
    }

    public List<T> getFieldsModified() {
        return fieldsModified;
    }

    public Optional<T> getFieldModified(String name) {
        return this.fieldsModified.stream().filter(fm -> fm.getName().equalsIgnoreCase(name)).findFirst();
    }

    public List<T> getFieldsOnlyInSchema() {
        return fieldsOnlyInSchema;
    }

    public Optional<T> getFieldOnlyInSchema(String name) {
        return this.fieldsOnlyInSchema.stream().filter(fm -> fm.getName().equalsIgnoreCase(name)).findFirst();
    }

    public List<T> getFieldsStillInSolr() {
        return fieldsStillInSolr;
    }

    public Optional<T> getFieldStillInSolr(String name) {
        return this.fieldsStillInSolr.stream().filter(fm -> fm.getName().equalsIgnoreCase(name)).findFirst();
    }

    protected abstract String getType();

    @Override
    public String getErrorMessage(int levelOfIndentation) {
        StringBuilder result = new StringBuilder();

        appendFields(result," only in Schema:", this.fieldsOnlyInSchema, levelOfIndentation);
        appendFields(result," still in Solr:", this.fieldsStillInSolr, levelOfIndentation);
        appendFields(result," with modified attributes:", this.fieldsModified, levelOfIndentation);

        return result.toString();
    }

    private void appendFields(StringBuilder result, String text, List<T> fields, int levelOfIndentation) {
        if (fields.isEmpty()) {
            return;
        }
        result.append(getType());
        result.append(text);
        appendNewlineIndentation(result, 2);
        fields.stream()
            .sorted()
            .forEach(eachResult -> result.append(eachResult.getErrorMessage(levelOfIndentation + 1)));
        appendNewlineIndentation(result, 1);
    }
}
