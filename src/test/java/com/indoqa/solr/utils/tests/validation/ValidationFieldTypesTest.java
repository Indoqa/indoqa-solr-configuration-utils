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
package com.indoqa.solr.utils.tests.validation;

import static com.indoqa.solr.utils.validation.SchemaCheck.FIELD_TYPES;
import static com.indoqa.solr.utils.validation.results.ValuesOrigin.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Optional;

import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.results.*;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class ValidationFieldTypesTest extends AbstractSolrSchemaCheckerTest {

    @Test
    public void testTest1StillInSolr() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), CHANGED_SCHEMA, FIELD_TYPES);
        assertEquals(1, schemaValidationResult.getResults().size());

        Optional<FieldTypesValidationResult> result = schemaValidationResult.getResult(FieldTypesValidationResult.class);
        FieldTypesValidationResult fieldTypesValidationResult = result.get();

        assertEquals(1, fieldTypesValidationResult.getFieldsOnlyInSchema().size());
        assertEquals(2, fieldTypesValidationResult.getFieldsStillInSolr().size());
        assertEquals(2, fieldTypesValidationResult.getFieldsModified().size());

        FieldTypeValidationResult test1 = new FieldTypeValidationResult();
        test1.setName("test1");
        test1.setClassName("solr.StrField");
        test1.addAttributeStillInSolr("sortMissingLast", false);
        test1.addAttributeStillInSolr("multiValued", false);

        Optional<FieldTypeValidationResult> fieldStillInSolr = fieldTypesValidationResult.getFieldStillInSolr("test1");
        assertTrue(fieldStillInSolr.isPresent());
        FieldTypeValidationResult resultTest1 = fieldStillInSolr.get();

        assertSameProperties(resultTest1, test1);

        String errorMessage = schemaValidationResult.getErrorMessage();
        MatcherAssert.assertThat(errorMessage, allOf(
            containsString("Field: 'test1'"),
            containsString("Class: 'solr.StrField'"),
            containsString("Attributes still in Solr"),
            containsString("multiValued=false"),
            containsString("sortMissingLast=false")));
    }

    @Test
    public void testOldTextNgramStillInSolr() throws SolrSchemaException {
        AnalyzerValidationResult analyzerValidation = new AnalyzerValidationResult();
        analyzerValidation.setType("default");
        analyzerValidation.addTokenizerValidation(tokenizerOf("solr.StandardTokenizerFactory", STILL_IN_SOLR));
        analyzerValidation.addFiltersStillInSolr(filterOf("solr.StopFilterFactory", STILL_IN_SOLR));

        FieldTypeValidationResult oldTextNgram = new FieldTypeValidationResult();
        oldTextNgram.setName("old_text_ngram");
        oldTextNgram.setClassName("solr.TextField");
        oldTextNgram.addAttributeStillInSolr("positionIncrementGap", "100");
        oldTextNgram.addAttributeStillInSolr("storeOffsetsWithPositions", true);
        oldTextNgram.addAttributeStillInSolr("termVectors", true);
        oldTextNgram.addAttributeStillInSolr("termPositions", false);
        oldTextNgram.addAttributeStillInSolr("termOffsets", false);
        oldTextNgram.addAnalyzerStillInSolr(analyzerValidation);

        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), CHANGED_SCHEMA, FIELD_TYPES);
        assertEquals(1, schemaValidationResult.getResults().size());

        Optional<FieldTypesValidationResult> result = schemaValidationResult.getResult(FieldTypesValidationResult.class);
        FieldTypesValidationResult fieldTypesValidationResult = result.get();

        assertEquals(1, fieldTypesValidationResult.getFieldsOnlyInSchema().size());
        assertEquals(2, fieldTypesValidationResult.getFieldsStillInSolr().size());
        assertEquals(2, fieldTypesValidationResult.getFieldsModified().size());

        Optional<FieldTypeValidationResult> fieldStillInSolr = fieldTypesValidationResult.getFieldStillInSolr("old_text_ngram");
        assertTrue(fieldStillInSolr.isPresent());
        FieldTypeValidationResult resultOldTextNgram = fieldStillInSolr.get();

        assertSameProperties(resultOldTextNgram, oldTextNgram);

        String errorMessage = schemaValidationResult.getErrorMessage();
        MatcherAssert.assertThat(errorMessage, allOf(
            containsString("Field: 'old_text_ngram'"),
            containsString("Class: 'solr.TextField'"),
            containsString("Attributes still in Solr"),
            containsString("positionIncrementGap=100"),
            containsString("storeOffsetsWithPositions=true"),
            containsString("termOffsets=false"),
            containsString("termPositions=false"),
            containsString("termVectors=true"),
            containsString("Analyzer: 'default'"),
            containsString("Tokenizer: Class: 'solr.StandardTokenizerFactory'"),
            containsString("Filters still in Solr:"),
            containsString("Filter: Class: 'solr.StopFilterFactory'")
        ));
    }

    @Test
    public void testNewTextNgramOnlyInSchema() throws SolrSchemaException {
        AnalyzerValidationResult indexAnalzyer = new AnalyzerValidationResult();
        indexAnalzyer.setType("index");

        indexAnalzyer.addTokenizerValidation(tokenizerOf("solr.WhitespaceTokenizerFactory", ONLY_IN_SCHEMA));
        indexAnalzyer.addFiltersOnlyInSchema(filterOf("solr.TrimFilterFactory", ONLY_IN_SCHEMA));
        indexAnalzyer.addFiltersOnlyInSchema(filterOf("solr.LowerCaseFilterFactory", ONLY_IN_SCHEMA));
        indexAnalzyer.addFiltersOnlyInSchema(filterOf("solr.RemoveDuplicatesTokenFilterFactory", ONLY_IN_SCHEMA));
        FilterValidationResult filterValidation = filterOf("solr.NGramFilterFactory", ONLY_IN_SCHEMA);
        filterValidation.addAttributeOnlyInSchema("minGramSize", "1");
        filterValidation.addAttributeOnlyInSchema("maxGramSize", "30");
        indexAnalzyer.addFiltersOnlyInSchema(filterValidation);

        CharFilterValidationResult charFilterValidationResult = charFilterOf("solr.PatternReplaceCharFilterFactory", ONLY_IN_SCHEMA);
        charFilterValidationResult.addAttributeOnlyInSchema("pattern", "[^\\p{L}\\d\\§]");
        charFilterValidationResult.addAttributeOnlyInSchema("replacement", " ");
        indexAnalzyer.addCharFiltersOnlyInSchema(charFilterValidationResult);

        AnalyzerValidationResult multitermAnalyzer = new AnalyzerValidationResult();
        multitermAnalyzer.setType("multiterm");
        multitermAnalyzer.addTokenizerValidation(tokenizerOf("solr.KeywordTokenizerFactory", ONLY_IN_SCHEMA));

        FieldTypeValidationResult newTextNgram = new FieldTypeValidationResult();
        newTextNgram.setName("new_text_ngram");
        newTextNgram.setClassName("solr.TextField");
        newTextNgram.addAttributeOnlyInSchema("positionIncrementGap", "100");
        newTextNgram.addAttributeOnlyInSchema("storeOffsetsWithPositions", true);
        newTextNgram.addAttributeOnlyInSchema("termVectors", true);
        newTextNgram.addAttributeOnlyInSchema("termPositions", false);
        newTextNgram.addAttributeOnlyInSchema("termOffsets", false);
        newTextNgram.addAnalyzerOnlyInSchema(indexAnalzyer);
        newTextNgram.addAnalyzerOnlyInSchema(multitermAnalyzer);

        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), CHANGED_SCHEMA, FIELD_TYPES);
        assertEquals(1, schemaValidationResult.getResults().size());

        Optional<FieldTypesValidationResult> result = schemaValidationResult.getResult(FieldTypesValidationResult.class);
        FieldTypesValidationResult fieldTypesValidationResult = result.get();

        assertEquals(1, fieldTypesValidationResult.getFieldsOnlyInSchema().size());
        assertEquals(2, fieldTypesValidationResult.getFieldsStillInSolr().size());
        assertEquals(2, fieldTypesValidationResult.getFieldsModified().size());
        Optional<FieldTypeValidationResult> fieldOnlyInSchema = fieldTypesValidationResult.getFieldOnlyInSchema("new_text_ngram");
        assertTrue(fieldOnlyInSchema.isPresent());
        FieldTypeValidationResult resultNewTextNgram = fieldOnlyInSchema.get();

        assertSameProperties(resultNewTextNgram, newTextNgram);

        String errorMessage = schemaValidationResult.getErrorMessage();
        MatcherAssert.assertThat(errorMessage, allOf(
            containsString("Field: 'new_text_ngram'"),
            containsString("Class: 'solr.TextField'"),
            containsString("Attributes only in Schema"),
            containsString("positionIncrementGap=100"),
            containsString("storeOffsetsWithPositions=true"),
            containsString("termOffsets=false"),
            containsString("termPositions=false"),
            containsString("termVectors=true"),
            containsString("Analyzer: 'index'"),
            containsString("Tokenizer: Class: 'solr.WhitespaceTokenizerFactory'"),
            containsString("Filters only in Schema"),
            containsString("Filter: Class: 'solr.LowerCaseFilterFactory'"),
            containsString("Filter: Class: 'solr.NGramFilterFactory':"),
            containsString("maxGramSize=30"),
            containsString("minGramSize=1"),
            containsString("Filter: Class: 'solr.RemoveDuplicatesTokenFilterFactory'"),
            containsString("Filter: Class: 'solr.TrimFilterFactory'"),
            containsString("Charfilters only in Schema"),
            containsString("CharFilter: Class: 'solr.PatternReplaceCharFilterFactory'"),
            containsString("Attributes only in Schema"),
            containsString("pattern=[^\\p{L}\\d\\§]"),
            containsString("replacement= "),
            containsString("Analyzer: 'multiterm'"),
            containsString("Tokenizer: Class: 'solr.KeywordTokenizerFactory'")
            ));
    }

    @Test
    public void testTextNgramModified() throws SolrSchemaException {
        CharFilterValidationResult charFilterValidationResult = charFilterOf("solr.PatternReplaceCharFilterFactory", STILL_IN_SOLR);
        charFilterValidationResult.addAttributeStillInSolr("pattern", "[^\\p{L}\\d\\§]");
        charFilterValidationResult.addAttributeStillInSolr("replacement", " ");

        AnalyzerValidationResult queryAnalyzer = new AnalyzerValidationResult();
        queryAnalyzer.setType("query");
        queryAnalyzer.addFiltersStillInSolr(filterOf("solr.RemoveDuplicatesTokenFilterFactory", STILL_IN_SOLR));
        queryAnalyzer.addCharFiltersStillInSolr(charFilterValidationResult);

        AnalyzerValidationResult indexAnalyzer = new AnalyzerValidationResult();
        indexAnalyzer.setType("index");
        indexAnalyzer.addFiltersStillInSolr(filterOf("solr.RemoveDuplicatesTokenFilterFactory", STILL_IN_SOLR));

        FilterValidationResult filterValidationResult = filterOf("solr.NGramFilterFactory", BOTH);
        filterValidationResult.addDifferentAttribute("minGramSize", "5", "1");
        filterValidationResult.addDifferentAttribute("maxGramSize", "10", "30");
        indexAnalyzer.addFiltersModified(filterValidationResult);

        AttributesValidationResult similarityValidation = new AttributesValidationResult();
        similarityValidation.setClassName("solr.DFRSimilarityFactory");
        similarityValidation.addDifferentAttribute("basicModel", "D", "P");
        similarityValidation.addDifferentAttribute("afterEffect", "B", "L");
        similarityValidation.addDifferentAttribute("c", "9", "7.0");

        FieldTypeValidationResult textNgram = new FieldTypeValidationResult();
        textNgram.setName("text_ngram");
        textNgram.setClassName("solr.TextField");
        textNgram.addDifferentAttribute("positionIncrementGap", "95", "100");
        textNgram.addDifferentAttribute("termVectors", false, true);
        textNgram.addDifferentAttribute("termOffsets", true, false);
        textNgram.addAnalyzerModified(queryAnalyzer);
        textNgram.addAnalyzerModified(indexAnalyzer);
        textNgram.addSimilarityValidation(similarityValidation);

        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), CHANGED_SCHEMA, FIELD_TYPES);
        assertEquals(1, schemaValidationResult.getResults().size());

        Optional<FieldTypesValidationResult> result = schemaValidationResult.getResult(FieldTypesValidationResult.class);
        FieldTypesValidationResult fieldTypesValidationResult = result.get();

        assertEquals(1, fieldTypesValidationResult.getFieldsOnlyInSchema().size());
        assertEquals(2, fieldTypesValidationResult.getFieldsStillInSolr().size());
        assertEquals(2, fieldTypesValidationResult.getFieldsModified().size());
        Optional<FieldTypeValidationResult> fieldModified = fieldTypesValidationResult.getFieldModified("text_ngram");
        assertTrue(fieldModified.isPresent());
        FieldTypeValidationResult resulTextNgram = fieldModified.get();

        assertSameProperties(resulTextNgram, textNgram);

        String errorMessage = schemaValidationResult.getErrorMessage();
        MatcherAssert.assertThat(errorMessage, allOf(
            containsString("Field: 'text_ngram'"),
            containsString("Class: 'solr.TextField'"),
            containsString("Attributes with different values"),
            containsString("positionIncrementGap={expected=95, actual=100}"),
            containsString("termOffsets={expected=true, actual=false}"),
            containsString("termVectors={expected=false, actual=true}"),
            containsString("Analyzers modified"),
            containsString("Analyzer: 'index'"),
            containsString("Filters modified"),
            containsString("Filter: Class: 'solr.NGramFilterFactory'"),
            containsString("Attributes with different values"),
            containsString("maxGramSize={expected=10, actual=30}"),
            containsString("minGramSize={expected=5, actual=1}"),
            containsString("Filters still in Solr"),
            containsString("Filter: Class: 'solr.RemoveDuplicatesTokenFilterFactory'"),
            containsString("Analyzer: 'query'"),
            containsString("Filter: Class: 'solr.RemoveDuplicatesTokenFilterFactory'"),
            containsString("Charfilters still in Solr"),
            containsString("CharFilter: Class: 'solr.PatternReplaceCharFilterFactory'"),
            containsString("Attributes still in Solr"),
            containsString("pattern=[^\\p{L}\\d\\§]"),
            containsString("replacement= "),
            containsString("Similarity"),
            containsString("Field: Class: 'solr.DFRSimilarityFactory'"),
            containsString("afterEffect={expected=B, actual=L}"),
            containsString("basicModel={expected=D, actual=P}"),
            containsString("c={expected=9, actual=7.0}")
        ));
    }

    private static AttributesValidationResult tokenizerOf(String className, ValuesOrigin valuesOrigin) {
        AttributesValidationResult tokenizerValidation = new AttributesValidationResult();
        tokenizerValidation.setClassName(className);
        tokenizerValidation.setValuesOrigin(valuesOrigin);
        return tokenizerValidation;
    }

    private static FilterValidationResult filterOf(String clazz, ValuesOrigin valuesOrigin) {
        FilterValidationResult filterValidationResult = new FilterValidationResult();
        filterValidationResult.setClassName(clazz);
        filterValidationResult.setValuesOrigin(valuesOrigin);
        return filterValidationResult;
    }

    private static CharFilterValidationResult charFilterOf(String clazz, ValuesOrigin valuesOrigin) {
        CharFilterValidationResult filterValidationResult = new CharFilterValidationResult();
        filterValidationResult.setClassName(clazz);
        filterValidationResult.setValuesOrigin(valuesOrigin);
        return filterValidationResult;
    }
}
