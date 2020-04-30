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
package com.indoqa.solr.utils.tests.validation.helper;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.indoqa.solr.utils.validation.results.SchemaValidationResult;
import com.indoqa.solr.utils.validation.results.ValidationResult;

public class ValidationExtractor {

    public static ExtractedValidations extractValidations(Operation operation, SchemaValidationResult validationResult) {
        switch (operation) {
            case ADD:
                return extractOnlyInSchemaValidations(validationResult);
            case REMOVE:
                return extractStillInSolrValidations(validationResult);
            case MODIFY:
                return extractModifiedValidations(validationResult);
        }
        throw new IllegalArgumentException("No extraction method found for operation " + operation);
    }

    public static boolean hasValidations(Operation operation, SchemaValidationResult validationResult) {
        switch (operation) {
            case ADD:
                return hasValidations(validationResult, ValidationResult::getOnlyInSchema);
            case REMOVE:
                return hasValidations(validationResult, ValidationResult::getStillInSolr);
            case MODIFY:
                return hasValidations(validationResult, ValidationResult::getModified);
        }
        throw new IllegalArgumentException("No extraction method found for operation " + operation);
    }

    private static ExtractedValidations extractModifiedValidations(SchemaValidationResult validationResult) {
        ExtractedValidations extractedValidations = new ExtractedValidations();
        for (ValidationResult result : validationResult.getResults()) {
            result.getModified().ifPresent(v -> v.forEach(extractedValidations::add));
        }
        return extractedValidations;
    }

    private static ExtractedValidations extractStillInSolrValidations(SchemaValidationResult validationResult) {
        ExtractedValidations extractedValidations = new ExtractedValidations();
        for (ValidationResult result : validationResult.getResults()) {
            result.getStillInSolr().ifPresent(v -> v.forEach(extractedValidations::add));
        }
        return extractedValidations;
    }

    private static ExtractedValidations extractOnlyInSchemaValidations(SchemaValidationResult validationResult) {
        ExtractedValidations extractedValidations = new ExtractedValidations();
        for (ValidationResult result : validationResult.getResults()) {
            result.getOnlyInSchema().ifPresent(v -> v.forEach(extractedValidations::add));
        }
        return extractedValidations;
    }
    private static boolean hasValidations(SchemaValidationResult validationResult,
        Function<ValidationResult, Optional<List<? extends ValidationResult>>> getter) {
//        Optional<Optional<List<? extends ValidationResult>>> foundGetter = validationResult.getResults().stream().map(getter).findAny();
        List<Optional<List<? extends ValidationResult>>> list = validationResult
            .getResults()
            .stream()
            .map(getter)
            .collect(Collectors.toList());
        for (Optional<List<? extends ValidationResult>> validationResults : list) {
            if (validationResults.isPresent()) {
                if (!validationResults.get().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
}
