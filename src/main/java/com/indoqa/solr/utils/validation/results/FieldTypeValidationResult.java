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

import static java.util.Comparator.comparing;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.lang.model.element.VariableElement;

public class FieldTypeValidationResult extends FieldAttributesValidationResult {

    private List<AnalyzerValidationResult> analyzersModified = new ArrayList<>();
    private List<AnalyzerValidationResult> analyzersOnlyInSchema = new ArrayList<>();
    private List<AnalyzerValidationResult> analyzersStillInSolr = new ArrayList<>();

    private FieldAttributesValidationResult similarityValidationResult = new FieldAttributesValidationResult();

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && this.analyzersModified.isEmpty() && this.analyzersOnlyInSchema.isEmpty() && this.analyzersStillInSolr
            .isEmpty() && this.similarityValidationResult.isEmpty();
    }

    private static void appendAnalyzer(StringBuilder result, int levelOfIndentation, Stream<AnalyzerValidationResult> stream) {
        stream.sorted(comparing(AnalyzerValidationResult::getType))
            .forEach(eachResult -> result.append(eachResult.getErrorMessage(levelOfIndentation)));
    }

    private static void appendAnalyzers(StringBuilder result, int levelOfIndentation, String text, List<AnalyzerValidationResult> analyzers) {
        if (analyzers == null || analyzers.isEmpty()) {
            return;
        }
        appendNewlineIndentation(result, levelOfIndentation + 1);
        result.append(text);
        appendNewlineIndentation(result, levelOfIndentation + 2);
        appendAnalyzer(result, levelOfIndentation + 2, analyzers.stream());

    }

    @Override
    protected void appendToErrorMessage(int levelOfIndentation, StringBuilder result) {
        appendAnalyzers(result, levelOfIndentation, "Analyzers only in Schema:", analyzersOnlyInSchema);
        appendAnalyzers(result, levelOfIndentation, "Analyzers modified:", analyzersModified);
        appendAnalyzers(result, levelOfIndentation, "Analyzers still in Solr:", analyzersStillInSolr);
        if (!this.similarityValidationResult.isEmpty()) {
            appendNewlineIndentation(result, levelOfIndentation + 1);
            result.append("Similarity: ");
            result.append(similarityValidationResult.getErrorMessage(levelOfIndentation + 1));
        }
    }

    public void addSimilarityValidation(AttributesValidationResult attributesValidation) {
        similarityValidationResult.addAttributeValidation(attributesValidation);
    }

    public void addAnalyzerModified(AnalyzerValidationResult analyzerValidation) {
        if (analyzerValidation.isEmpty()) {
            return;
        }
        this.analyzersModified.add(analyzerValidation);
    }

    public void addAnalyzerOnlyInSchema(AnalyzerValidationResult analyzerValidation) {
        if (analyzerValidation.isEmpty()) {
            return;
        }
        this.analyzersOnlyInSchema.add(analyzerValidation);
    }

    public void addAnalyzerStillInSolr(AnalyzerValidationResult analyzerValidation) {
        if (analyzerValidation.isEmpty()) {
            return;
        }
        this.analyzersStillInSolr.add(analyzerValidation);
    }

    public List<AnalyzerValidationResult> getAnalyzersModified() {
        return analyzersModified;
    }

    public List<AnalyzerValidationResult> getAnalyzersOnlyInSchema() {
        return analyzersOnlyInSchema;
    }

    public List<AnalyzerValidationResult> getAnalyzersStillInSolr() {
        return analyzersStillInSolr;
    }

    public FieldAttributesValidationResult getSimilarityValidationResult() {
        return similarityValidationResult;
    }
}
