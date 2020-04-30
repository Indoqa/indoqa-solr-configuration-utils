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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SchemaValidationResult extends ValidationResult {

    private String collectionName;
    private List<ValidationResult> validationResults = new ArrayList<>();

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void addValidationResult(ValidationResult validationResult) {
        if (validationResult.isEmpty()) {
            return;
        }
        this.validationResults.add(validationResult);
    }

    public List<ValidationResult> getResults() {
        return this.validationResults;
    }

    public <T extends ValidationResult> Optional<T> getResult(Class<T> clazz) {
        if (clazz == null) {
            return Optional.empty();
        }

        return (Optional<T>) this.validationResults.stream()
            .filter(validationResult -> validationResult.getClass().isAssignableFrom(clazz))
            .findFirst();
    }

    @Override
    public boolean isEmpty() {
        return this.validationResults.isEmpty();
    }

    @Override
    public String getErrorMessage(int levelOfIndentation) {
        StringBuilder result = new StringBuilder();

        result.append("Schema for collection: '");
        result.append(collectionName);
        result.append("':\n");
        for (ValidationResult validationResult : validationResults) {
            if (validationResult.isEmpty()) {
                continue;
            }
            result.append('\t');
            result.append(validationResult.getErrorMessage(levelOfIndentation + 1));
        }

        return result.toString();
    }
}
