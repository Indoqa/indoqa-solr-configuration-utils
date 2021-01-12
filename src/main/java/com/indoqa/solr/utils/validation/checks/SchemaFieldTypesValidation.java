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
package com.indoqa.solr.utils.validation.checks;

import static com.indoqa.solr.utils.SolrConstants.*;
import static java.util.Collections.emptyMap;
import static java.util.Optional.*;

import java.util.*;
import java.util.function.Consumer;

import com.indoqa.solr.utils.validation.SchemaCheck;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.results.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.schema.AnalyzerDefinition;
import org.apache.solr.client.solrj.request.schema.FieldTypeDefinition;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;

public class SchemaFieldTypesValidation extends AbstractSchemaAbstractFieldsValidation<FieldTypesValidationResult> {

    @Override
    public SchemaCheck getSchemaCheck() {
        return SchemaCheck.FIELD_TYPES;
    }

    @Override
    public FieldTypesValidationResult validate(SolrSchema solrSchema, SchemaResponse schemaResponse, SolrClient solrClient)
        throws SolrSchemaException {
        List<? extends FieldTypeDefinition> schemaResponseFields;

        if (schemaResponse == null) {
            schemaResponseFields = process(solrSchema.getCollectionName(), new SchemaRequest.FieldTypes(), solrClient).getFieldTypes();
        } else {
            schemaResponseFields = schemaResponse.getSchemaRepresentation().getFieldTypes();
        }

        List<FieldTypeDefinition> fieldTypes = solrSchema.getFieldTypes();

        FieldTypesValidationResult result = new FieldTypesValidationResult();
        extractFieldTypeValidations(result, fieldTypes, schemaResponseFields);
        return result;
    }

    private void extractFieldTypeValidations(FieldTypesValidationResult result, List<FieldTypeDefinition> schemaFieldTypes,
        List<? extends FieldTypeDefinition> solr) {

        Map<String, FieldTypeDefinition> solrFieldTypes = map(solr);

        for (FieldTypeDefinition fieldType : schemaFieldTypes) {
            String name = (String) fieldType.getAttributes().get(NAME_ATTRIBUTE);
            FieldTypeDefinition remove = solrFieldTypes.remove(name);

            FieldTypeValidationResult fieldValidationResult = new FieldTypeValidationResult();
            fieldValidationResult.setName(name);
            fieldValidationResult.setValuesOrigin(ValuesOrigin.BOTH);

            if (remove == null) {
                fieldValidationResult.addAttributeValidation(AttributesValidator.validate(fieldType.getAttributes(),
                    emptyMap()));
                fieldValidationResult.setValuesOrigin(ValuesOrigin.ONLY_IN_SCHEMA);
                result.addFieldsOnlyInSchema(fieldValidationResult);
                onlyInSchemaAnalyzers(fieldValidationResult, fieldType);
                fieldValidationResult.addSimilarityValidation(AttributesValidator.validate(fieldType.getSimilarity(), emptyMap()));
                continue;
            }

            fieldValidationResult.addAttributeValidation(AttributesValidator.validate(fieldType.getAttributes(),
                remove.getAttributes()));

            result.addFieldsModified(fieldValidationResult);
            modifiedAnalyzers(fieldValidationResult, fieldType, remove);
            fieldValidationResult.addSimilarityValidation(AttributesValidator.validate(fieldType.getSimilarity(), remove.getSimilarity()));
        }

        for (Map.Entry<String, FieldTypeDefinition> entry : solrFieldTypes.entrySet()) {
            FieldTypeValidationResult fieldValidationResult = new FieldTypeValidationResult();
            fieldValidationResult.setValuesOrigin(ValuesOrigin.STILL_IN_SOLR);
            fieldValidationResult.setName(entry.getKey());
            FieldTypeDefinition value = entry.getValue();
            fieldValidationResult.addAttributeValidation(AttributesValidator.validate(emptyMap(), value.getAttributes()));
            result.addFieldStillInSolr(fieldValidationResult);
            stillInSolrAnalyzers(fieldValidationResult, entry.getValue());
            fieldValidationResult.addSimilarityValidation(AttributesValidator.validate(emptyMap(), value.getSimilarity()));
        }

    }

    private void analyzers(Consumer<AnalyzerValidationResult> consumer, Optional<FieldTypeDefinition> schema, Optional<FieldTypeDefinition> solr) {
        //Solr ignores index if default is defined
        consumer.accept(validateAnalyzer(DEFAULT_ANALYZER, getAnalyzer(schema), getAnalyzer(solr)));
        consumer.accept(validateAnalyzer(INDEX_ANALYZER, getIndexAnalyzer(schema), getIndexAnalyzer(solr)));
        consumer.accept(validateAnalyzer(QUERY_ANALYZER, getQueryAnalyzer(schema), getQueryAnalyzer(solr)));
        consumer.accept(validateAnalyzer(MULTITERM_ANALYZER, getMultiTermAnalyzer(schema), getMultiTermAnalyzer(solr)));
    }

    private Optional<AnalyzerDefinition> getAnalyzer(Optional<FieldTypeDefinition> typeDefinition) {
        return ofNullable(typeDefinition.map(FieldTypeDefinition::getAnalyzer).orElse(null));
    }
    private Optional<AnalyzerDefinition> getIndexAnalyzer(Optional<FieldTypeDefinition> typeDefinition) {
        return ofNullable(typeDefinition.map(FieldTypeDefinition::getIndexAnalyzer).orElse(null));
    }
    private Optional<AnalyzerDefinition> getQueryAnalyzer(Optional<FieldTypeDefinition> typeDefinition) {
        return ofNullable(typeDefinition.map(FieldTypeDefinition::getQueryAnalyzer).orElse(null));
    }
    private Optional<AnalyzerDefinition> getMultiTermAnalyzer(Optional<FieldTypeDefinition> typeDefinition) {
        return ofNullable(typeDefinition.map(FieldTypeDefinition::getMultiTermAnalyzer).orElse(null));
    }

    private void modifiedAnalyzers(FieldTypeValidationResult result, FieldTypeDefinition schema, FieldTypeDefinition solr) {
        analyzers(result::addAnalyzerModified, ofNullable(schema),ofNullable(solr));
    }

    private void stillInSolrAnalyzers(FieldTypeValidationResult result, FieldTypeDefinition solr) {
        analyzers(result::addAnalyzerStillInSolr, empty(),ofNullable(solr));
    }

    private void onlyInSchemaAnalyzers(FieldTypeValidationResult result, FieldTypeDefinition schema) {
        analyzers(result::addAnalyzerOnlyInSchema,ofNullable(schema), empty());
    }

    private Map<String, Map<String, Object>> mapClassesToAttributes(List<Map<String, Object>> list) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Map<String, Object> eachMap : list) {
            result.put((String) eachMap.get(CLASS_ATTRIBUTE), eachMap);
        }
        return result;
    }

    private void validateCharFilterOnlyInSchema(AnalyzerValidationResult result, String className, Map<String, Object> attributes) {
        CharFilterValidationResult validationResult = new CharFilterValidationResult();
        validationResult.setClassName(className);
        validationResult.addAttributeValidation(AttributesValidator.validate(attributes, emptyMap()));
        result.addCharFiltersOnlyInSchema(validationResult);
    }

    private void validateCharFilterStillInSolr(AnalyzerValidationResult result, String className, Map<String, Object> attributes) {
        CharFilterValidationResult validationResult = new CharFilterValidationResult();
        validationResult.setClassName(className);
        validationResult.addAttributeValidation(AttributesValidator.validate(emptyMap(), attributes));
        result.addCharFiltersStillInSolr(validationResult);
    }

    private void validateCharFilters(AnalyzerValidationResult result, Optional<List<Map<String, Object>>> schema, Optional<List<Map<String, Object>>> solr) {
        if (schema.isPresent() && solr.isPresent()) {
            Map<String, Map<String, Object>> schemaCharFilters = mapClassesToAttributes(schema.get());
            Map<String, Map<String, Object>> solrCharFilters = mapClassesToAttributes(solr.get());

            for (Map.Entry<String, Map<String, Object>> eachSchemaCharFilter : schemaCharFilters.entrySet()) {
                String className = eachSchemaCharFilter.getKey();
                Map<String, Object> solrCharFilter = solrCharFilters.remove(className);
                if(solrCharFilter == null) {
                    validateCharFilterOnlyInSchema(result, className, eachSchemaCharFilter.getValue());
                    continue;
                }

                CharFilterValidationResult validationResult = new CharFilterValidationResult();
                validationResult.setClassName(className);
                validationResult.addAttributeValidation(AttributesValidator.validate(eachSchemaCharFilter.getValue(), solrCharFilter));
                result.addCharFiltersModified(validationResult);
            }

            for (Map.Entry<String, Map<String, Object>> eachSolrCharFilter : solrCharFilters.entrySet()) {
                validateCharFilterStillInSolr(result, eachSolrCharFilter.getKey(), eachSolrCharFilter.getValue());
            }
            return;
        }

        if (schema.isPresent()) {
            Map<String, Map<String, Object>> schemaCharFilters = mapClassesToAttributes(schema.get());
            for (Map.Entry<String, Map<String, Object>> eachSchemaCharFilter : schemaCharFilters.entrySet()) {
                validateCharFilterOnlyInSchema(result, eachSchemaCharFilter.getKey(), eachSchemaCharFilter.getValue());
            }
            return;
        }

        if (solr.isPresent()) {
            Map<String, Map<String, Object>> solrCharFilters = mapClassesToAttributes(solr.get());
            for (Map.Entry<String, Map<String, Object>> eachSolrCharFilter : solrCharFilters.entrySet()) {
                validateCharFilterStillInSolr(result, eachSolrCharFilter.getKey(), eachSolrCharFilter.getValue());
            }
            return;
        }
    }

    private void validateFilterOnlyInSchema(AnalyzerValidationResult result, String className, Map<String, Object> attributes) {
        FilterValidationResult validationResult = new FilterValidationResult();
        validationResult.setClassName(className);
        validationResult.addAttributeValidation(AttributesValidator.validate(attributes, emptyMap()));
        result.addFiltersOnlyInSchema(validationResult);
    }

    private void validateFilterStillInSolr(AnalyzerValidationResult result, String className, Map<String, Object> attributes) {
        FilterValidationResult validationResult = new FilterValidationResult();
        validationResult.setClassName(className);
        validationResult.addAttributeValidation(AttributesValidator.validate(emptyMap(), attributes));
        result.addFiltersStillInSolr(validationResult);
    }

    private void validateFilters(AnalyzerValidationResult result, Optional<List<Map<String, Object>>> schema, Optional<List<Map<String, Object>>> solr) {
        if (schema.isPresent() && solr.isPresent()) {
            Map<String, Map<String, Object>> schemaFilters = mapClassesToAttributes(schema.get());
            Map<String, Map<String, Object>> solrFilters = mapClassesToAttributes(solr.get());

            for (Map.Entry<String, Map<String, Object>> eachSchemaCharFilter : schemaFilters.entrySet()) {
                String className = eachSchemaCharFilter.getKey();
                Map<String, Object> solrFilter = solrFilters.remove(className);
                if(solrFilter == null) {
                    validateFilterOnlyInSchema(result, eachSchemaCharFilter.getKey(), eachSchemaCharFilter.getValue());
                    continue;
                }

                FilterValidationResult validationResult = new FilterValidationResult();
                validationResult.setClassName(className);
                validationResult.addAttributeValidation(AttributesValidator.validate(eachSchemaCharFilter.getValue(), solrFilter));
                result.addFiltersModified(validationResult);
            }

            for (Map.Entry<String, Map<String, Object>> eachSolrCharFilter : solrFilters.entrySet()) {
                validateFilterStillInSolr(result,eachSolrCharFilter.getKey(),eachSolrCharFilter.getValue());
            }
            return;
        }

        if (schema.isPresent()) {
            Map<String, Map<String, Object>> stringMapMap = mapClassesToAttributes(schema.get());
            for (Map.Entry<String, Map<String, Object>> eachSchemaFilter : stringMapMap.entrySet()) {
                validateFilterOnlyInSchema(result,eachSchemaFilter.getKey(), eachSchemaFilter.getValue());
            }
            return;
        }

        if (solr.isPresent()) {
            Map<String, Map<String, Object>> stringMapMap = mapClassesToAttributes(solr.get());
            for (Map.Entry<String, Map<String, Object>> eachSchemaFilter : stringMapMap.entrySet()) {
                validateFilterStillInSolr(result, eachSchemaFilter.getKey(), eachSchemaFilter.getValue());
            }
            return;
        }
    }

    private AnalyzerValidationResult validateAnalyzer(String type, Optional<AnalyzerDefinition> schema, Optional<AnalyzerDefinition> solr) {
        AnalyzerValidationResult result = new AnalyzerValidationResult();
        if (schema.isPresent() && solr.isPresent()) {
            AnalyzerDefinition schemaAnalyzer = schema.get();
            AnalyzerDefinition solrAnalyzer = solr.get();
            result.setType(type);
            result.addAttributeValidation(AttributesValidator.validate(
                schemaAnalyzer.getAttributes(),
                solrAnalyzer.getAttributes()));

            result.addTokenizerValidation(AttributesValidator.validate(
                schemaAnalyzer.getTokenizer(),
                solrAnalyzer.getTokenizer()));

            validateCharFilters(result, ofNullable(schemaAnalyzer.getCharFilters()), ofNullable(solrAnalyzer.getCharFilters()));
            validateFilters(result, ofNullable(schemaAnalyzer.getFilters()), ofNullable(solrAnalyzer.getFilters()));
            return result;
        }

        if (schema.isPresent()) {
            AnalyzerDefinition analyzerDefinition = schema.get();
            result.setType(type);
            result.addAttributeValidation(AttributesValidator.validate(
                analyzerDefinition.getAttributes(),
                emptyMap()));

            result.addTokenizerValidation(AttributesValidator.validate(
                analyzerDefinition.getTokenizer(),
                emptyMap()));

            validateCharFilters(result, ofNullable(analyzerDefinition.getCharFilters()), empty());
            validateFilters(result, ofNullable(analyzerDefinition.getFilters()), empty());

            return result;
        }

        if (solr.isPresent()) {
            AnalyzerDefinition analyzerDefinition = solr.get();
            result.setType(type);
            result.addAttributeValidation(AttributesValidator.validate(emptyMap(), analyzerDefinition.getAttributes()));

            result.addTokenizerValidation(AttributesValidator.validate(emptyMap(), analyzerDefinition.getTokenizer()));

            validateCharFilters(result, empty(), ofNullable(analyzerDefinition.getCharFilters()));
            validateFilters(result, empty(), ofNullable(analyzerDefinition.getFilters()));
        }

        return result;
    }

    private Map<String, FieldTypeDefinition> map(List<? extends FieldTypeDefinition> schemaResponseFields) {
        Map<String, FieldTypeDefinition> result = new HashMap<>();

        for (FieldTypeDefinition eachFieldType : schemaResponseFields) {
            String name = String.valueOf(eachFieldType.getAttributes().get(NAME_ATTRIBUTE));
            result.put(name, eachFieldType);
        }

        return result;
    }
}
