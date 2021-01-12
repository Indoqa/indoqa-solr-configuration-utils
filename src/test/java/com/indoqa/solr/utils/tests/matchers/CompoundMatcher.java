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
package com.indoqa.solr.utils.tests.matchers;

import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.indoqa.solr.utils.validation.results.*;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class CompoundMatcher {

    public static Matcher<AbstractFieldsValidationResult> isSameAbstractFieldsValidationResult(AbstractFieldsValidationResult result) {
        return allOf(
            hasProperty("fieldsModified", containsFieldAttributesValidation(result.getFieldsModified())),
            hasProperty("fieldsOnlyInSchema", containsFieldAttributesValidation(result.getFieldsOnlyInSchema())),
            hasProperty("fieldsStillInSolr", containsFieldAttributesValidation(result.getFieldsStillInSolr())));
    }

    private static Matcher<?> containsFieldAttributesValidation(List<? extends FieldAttributesValidationResult> list) {
        List<Matcher<?>> result = new ArrayList<>();
        for (FieldAttributesValidationResult eachEntry : list) {
            result.add(hasSameFieldAttributesValidtion(eachEntry));
        }
        return hasItems(result.toArray(new Matcher[result.size()]));
    }

    public static Matcher<FieldTypeValidationResult> isSameFieldTypeValidationAs(FieldTypeValidationResult result) {
        return allOf(hasSameFieldAttributesValidtion(result),
            hasSameFieldValidationResult(result),
            hasProperty("analyzersStillInSolr", getAnalyzersMatcher(result.getAnalyzersStillInSolr())),
            hasProperty("analyzersOnlyInSchema", getAnalyzersMatcher(result.getAnalyzersOnlyInSchema())),
            hasProperty("analyzersModified", getAnalyzersMatcher(result.getAnalyzersModified())),
            hasProperty("similarityValidationResult", hasSameFieldAttributesValidtion(result.getSimilarityValidationResult())));
    }

    public static Matcher<FieldAttributesValidationResult> hasSameFieldValidationResult(AbstractValidationResult validation) {
        return allOf(hasProperty("empty", equalTo(validation.isEmpty())),
            hasProperty("errorMessage", equalTo(validation.getErrorMessage(0))));
    }

    public static Matcher hasSameDifferentValueEntries(Map<String, DifferentValue> map) {
        List<Matcher<?>> result = new ArrayList<>();
        for (Map.Entry<String, DifferentValue> eachEntry : map.entrySet()) {
            result.add(hasEntry(eachEntry.getKey(), eachEntry.getValue()));
        }
        return allOf(result.toArray(new Matcher[result.size()]));
    }

    public static Matcher<FieldAttributesValidationResult> hasSameFieldAttributesValidtion(
        FieldAttributesValidationResult validation) {
        return allOf(hasProperty("name", equalTo(validation.getName())),
            hasProperty("className", equalTo(validation.getClassName())),
            hasProperty("attributesStillInSolr", equalTo(validation.getAttributesStillInSolr())),
            hasProperty("attributesOnlyInSchema", equalTo(validation.getAttributesOnlyInSchema())),
            hasProperty("differentAttributeValues", IsMapWithSize.isMapWithSize(validation.getDifferentAttributeValues().size())),
            hasProperty("differentAttributeValues", hasSameDifferentValueEntries(validation.getDifferentAttributeValues())));
    }

    public static Matcher<AnalyzerValidationResult> isSameAnalzyerValidationAs(AnalyzerValidationResult validation) {
        return allOf(
            hasProperty("type", equalTo(validation.getType())),

            hasProperty("attributesValidationResult", hasSameFieldAttributesValidtion(validation.getAttributesValidationResult())),
            hasProperty("tokenizerValidationResult", hasSameFieldAttributesValidtion(validation.getTokenizerValidationResult())),

            hasProperty("filtersStillInSolr", getFiltersMatcher(validation.getFiltersStillInSolr())),
            hasProperty("filtersOnlyInSchema", getFiltersMatcher(validation.getFiltersOnlyInSchema())),
            hasProperty("filtersModified", getFiltersMatcher(validation.getFiltersModified())),

            hasProperty("charFiltersModified", getFiltersMatcher(validation.getCharFiltersModified())),
            hasProperty("charFiltersOnlyInSchema", getFiltersMatcher(validation.getCharFiltersOnlyInSchema())),
            hasProperty("charFiltersStillInSolr", getFiltersMatcher(validation.getCharFiltersStillInSolr())));
    }

    private static Matcher<?> getAnalyzersMatcher(List<AnalyzerValidationResult> analyzers) {
        if (analyzers.isEmpty()) {
            return Matchers.emptyCollectionOf(AnalyzerValidationResult.class);
        }
        return hasItems(getAnalyzerMatchers(analyzers).toArray(new Matcher[analyzers.size()]));
    }

    private static List<Matcher<?>> getAnalyzerMatchers(List<AnalyzerValidationResult> validations) {
        List<Matcher<?>> result = new ArrayList<>();
        for (AnalyzerValidationResult validation : validations) {
            result.add(isSameAnalzyerValidationAs(validation));
        }
        return result;
    }

    private static Matcher<?> getFiltersMatcher(List<? extends FieldAttributesValidationResult> validations) {
        if (validations.isEmpty()) {
            return Matchers.emptyCollectionOf(FieldAttributesValidationResult.class);
        }
        return hasItems(getFilterMatchers(validations).toArray(new Matcher[validations.size()]));
    }

    private static List<Matcher<?>> getFilterMatchers(List<? extends FieldAttributesValidationResult> validations) {
        List<Matcher<?>> result = new ArrayList<>();
        for (FieldAttributesValidationResult validation : validations) {
            result.add(hasSameFieldAttributesValidtion(validation));
        }
        return result;
    }
}
