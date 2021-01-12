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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class AnalyzerValidationResult extends AbstractValidationResult {

    private String type;

    private FieldAttributesValidationResult attributesValidationResult = new FieldAttributesValidationResult();
    private TokenizerAttributesValidationResult tokenizerValidationResult = new TokenizerAttributesValidationResult();

    private List<FilterValidationResult> filtersModified = new ArrayList<>();
    private List<FilterValidationResult> filtersOnlyInSchema = new ArrayList<>();
    private List<FilterValidationResult> filtersStillInSolr = new ArrayList<>();

    private List<CharFilterValidationResult> charFiltersModified = new ArrayList<>();
    private List<CharFilterValidationResult> charFiltersOnlyInSchema = new ArrayList<>();
    private List<CharFilterValidationResult> charFiltersStillInSolr = new ArrayList<>();

    public void addAttributeValidation(AttributesValidationResult attributesValidation) {
        if (attributesValidation.isEmpty()) {
            return;
        }
        attributesValidationResult.addAttributeValidation(attributesValidation);
    }

    public void addTokenizerValidation(AttributesValidationResult tokenizerValidation) {
        if (tokenizerValidation.isEmpty()) {
            return;
        }
        tokenizerValidationResult.addAttributeValidation(tokenizerValidation);
    }

    public void addFiltersModified(FilterValidationResult filterValidation) {
        if (filterValidation.isEmpty()) {
            return;
        }
        this.filtersModified.add(filterValidation);
    }

    public void addFiltersOnlyInSchema(FilterValidationResult filterValidation) {
        if (filterValidation.isEmpty()) {
            return;
        }
        this.filtersOnlyInSchema.add(filterValidation);
    }

    public void addFiltersStillInSolr(FilterValidationResult filterValidation) {
        if (filterValidation.isEmpty()) {
            return;
        }
        this.filtersStillInSolr.add(filterValidation);
    }

    public void addCharFiltersModified(CharFilterValidationResult charFilterValidation) {
        if (charFilterValidation.isEmpty()) {
            return;
        }
        this.charFiltersModified.add(charFilterValidation);
    }

    public void addCharFiltersOnlyInSchema(CharFilterValidationResult charFilterValidation) {
        if (charFilterValidation.isEmpty()) {
            return;
        }
        this.charFiltersOnlyInSchema.add(charFilterValidation);
    }

    public void addCharFiltersStillInSolr(CharFilterValidationResult charFilterValidation) {
        if (charFilterValidation.isEmpty()) {
            return;
        }
        this.charFiltersStillInSolr.add(charFilterValidation);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean isEmpty() {
        return this.attributesValidationResult.isEmpty() && this.tokenizerValidationResult.isEmpty() && this.filtersAreEmpty()
            && this.charFiltersAreEmpty();
    }

    private boolean charFiltersAreEmpty() {
        return this.charFiltersModified.isEmpty() && this.charFiltersOnlyInSchema.isEmpty() && this.charFiltersStillInSolr.isEmpty();
    }

    private boolean filtersAreEmpty() {
        return this.filtersModified.isEmpty() && this.filtersOnlyInSchema.isEmpty() && this.filtersStillInSolr.isEmpty();
    }

    private static void appendFilter(StringBuilder result, int levelOfIndentation,
        Stream<? extends FieldAttributesValidationResult> stream) {
        stream
            .sorted(Comparator.comparing(FieldAttributesValidationResult::getClassName))
            .forEach(eachResult -> result.append(eachResult.getErrorMessage(levelOfIndentation)));
    }

    private static void appendFilters(StringBuilder result, int levelOfIndentation, String text,
        List<? extends FieldAttributesValidationResult> filters) {
        if (filters == null || filters.isEmpty()) {
            return;
        }
        appendNewlineIndentation(result, levelOfIndentation + 1);
        result.append(text);
        appendNewlineIndentation(result, levelOfIndentation + 2);
        appendFilter(result, levelOfIndentation + 1, filters.stream());
    }

    @Override
    public String getErrorMessage(int levelOfIndentation) {
        StringBuilder result = new StringBuilder();

        appendNewlineIndentation(result, levelOfIndentation);
        result.append("Analyzer: '");
        result.append(getType());
        result.append("':");

        if (!this.attributesValidationResult.isEmpty()) {
            result.append(attributesValidationResult.getErrorMessage(levelOfIndentation + 1));
        }

        if (!this.tokenizerValidationResult.isEmpty()) {
            result.append(tokenizerValidationResult.getErrorMessage(levelOfIndentation + 1));
        }

        appendFilters(result, levelOfIndentation, "Filters only in Schema:", filtersOnlyInSchema);
        appendFilters(result, levelOfIndentation, "Filters modified:", filtersModified);
        appendFilters(result, levelOfIndentation, "Filters still in Solr:", filtersStillInSolr);
        appendFilters(result, levelOfIndentation, "Charfilters only in Schema:", charFiltersOnlyInSchema);
        appendFilters(result, levelOfIndentation, "Charfilters modified:", charFiltersModified);
        appendFilters(result, levelOfIndentation, "Charfilters still in Solr:", charFiltersStillInSolr);

        return result.toString();
    }

    public FieldAttributesValidationResult getAttributesValidationResult() {
        return attributesValidationResult;
    }

    public TokenizerAttributesValidationResult getTokenizerValidationResult() {
        return tokenizerValidationResult;
    }

    public List<FilterValidationResult> getFiltersModified() {
        return filtersModified;
    }

    public List<FilterValidationResult> getFiltersOnlyInSchema() {
        return filtersOnlyInSchema;
    }

    public List<FilterValidationResult> getFiltersStillInSolr() {
        return filtersStillInSolr;
    }

    public List<CharFilterValidationResult> getCharFiltersModified() {
        return charFiltersModified;
    }

    public List<CharFilterValidationResult> getCharFiltersOnlyInSchema() {
        return charFiltersOnlyInSchema;
    }

    public List<CharFilterValidationResult> getCharFiltersStillInSolr() {
        return charFiltersStillInSolr;
    }
}
