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
package com.indoqa.solr.utils.maintenance.handlers;

import static com.indoqa.solr.utils.SolrConstants.*;
import static java.util.stream.Collectors.toList;

import java.util.*;

import com.indoqa.solr.utils.InitializationFailedException;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.tests.validation.helper.ExtractedValidations;
import com.indoqa.solr.utils.tests.validation.helper.Operation;
import com.indoqa.solr.utils.validation.results.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.schema.AnalyzerDefinition;
import org.apache.solr.client.solrj.request.schema.FieldTypeDefinition;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;

public class AddFieldsHandler extends AbstractFieldsHandler {

    public AddFieldsHandler() {
        super(Operation.ADD);
    }

    @Override
    protected void handle(SolrClient solrClient, SolrSchema solrSchema, ExtractedValidations extractedValidations)
        throws InitializationFailedException {
        SchemaUpdates schemaUpdates = createSchemaUpdates(extractedValidations);
        processUpdates(solrClient, solrSchema, schemaUpdates.getAllUpdatesAdd());
    }

    private SchemaUpdates createSchemaUpdates(ExtractedValidations extractedValidations) {
        SchemaUpdates schemaUpdates = new SchemaUpdates();
        for (AbstractValidationResult validation : extractedValidations.getValidations()) {
            Optional<List<? extends AbstractValidationResult>> onlyInSchema = validation.getOnlyInSchema();
            if (!onlyInSchema.isPresent()) {
                continue;
            }
            for (AbstractValidationResult result : onlyInSchema.get()) {
                if (result instanceof FieldTypesValidationResult) {
                    schemaUpdates.addAllFieldTypeAdd(createFieldTypeUpdates((FieldTypesValidationResult) result));
                }
                if (result instanceof FieldsValidationResult) {
                    schemaUpdates.addAllFieldUpdatesAdd(createFieldUpdates((FieldsValidationResult) result));
                }
                if (result instanceof DynamicFieldsValidationResult) {
                    schemaUpdates.addAllDynamicUpdatesAdd(createDynamicFieldUpdates((DynamicFieldsValidationResult) result));
                }
                if (result instanceof CopyFieldsValidationResult) {
                    schemaUpdates.addAllCopyFieldUpdatesAdd(createAddCopyFieldUpdates((CopyFieldsValidationResult) result));
                }
            }
        }
        return schemaUpdates;
    }

    private List<SchemaRequest.AddCopyField> createAddCopyFieldUpdates(CopyFieldsValidationResult result) {
        return result.getFieldsOnlyInSchema().stream().map(this::create).collect(toList());
    }

    private List<SchemaRequest.AddDynamicField> createDynamicFieldUpdates(DynamicFieldsValidationResult result) {
        return result.getFieldsOnlyInSchema().stream().map(this::convert).map(SchemaRequest.AddDynamicField::new).collect(toList());
    }

    private List<SchemaRequest.AddField> createFieldUpdates(FieldsValidationResult result) {
        return result.getFieldsOnlyInSchema().stream().map(this::convert).map(SchemaRequest.AddField::new).collect(toList());
    }

    private SchemaRequest.AddCopyField create(FieldAttributesValidationResult fieldAttributesValidationResult) {
        String source = (String) fieldAttributesValidationResult.getAttributesOnlyInSchema().get(SOURCE_ATTRIBUTE);
        String dest = (String) fieldAttributesValidationResult.getAttributesOnlyInSchema().get(DEST_ATTRIBUTE);
        Integer maxChars = (Integer) fieldAttributesValidationResult.getAttributesOnlyInSchema().get(MAX_CHARS_ATTRIBUTES);

        return new SchemaRequest.AddCopyField(source, Arrays.asList(dest), maxChars);
    }

    private List<SchemaRequest.AddFieldType> createFieldTypeUpdates(FieldTypesValidationResult result) {
        return result.getFieldsOnlyInSchema().stream().map(this::convert).map(SchemaRequest.AddFieldType::new).collect(toList());
    }

    private FieldTypeDefinition convert(FieldTypeValidationResult validationResult) {
        FieldTypeDefinition result = new FieldTypeDefinition();

        for (AnalyzerValidationResult analyzerValidationResult : validationResult.getAnalyzersOnlyInSchema()) {
            if (INDEX_ANALYZER.equalsIgnoreCase(analyzerValidationResult.getType())) {
                result.setIndexAnalyzer(convert(analyzerValidationResult));
            }
            if (QUERY_ANALYZER.equalsIgnoreCase(analyzerValidationResult.getType())) {
                result.setQueryAnalyzer(convert(analyzerValidationResult));
            }
            if (MULTITERM_ANALYZER.equalsIgnoreCase(analyzerValidationResult.getType())) {
                result.setQueryAnalyzer(convert(analyzerValidationResult));
            }
            if (DEFAULT_ANALYZER.equalsIgnoreCase(analyzerValidationResult.getType())) {
                result.setAnalyzer(convert(analyzerValidationResult));
            }
        }

        result.setAttributes(convert((FieldAttributesValidationResult) validationResult));
        result.setSimilarity(convert(validationResult.getSimilarityValidationResult()));
        return result;
    }

    private AnalyzerDefinition convert(AnalyzerValidationResult validation) {
        List<Map<String, Object>> filters = validation.getFiltersOnlyInSchema().stream().map(this::convert).collect(toList());
        List<Map<String, Object>> charFilters = validation.getCharFiltersOnlyInSchema().stream().map(this::convert).collect(toList());

        AnalyzerDefinition result = new AnalyzerDefinition();
        result.setAttributes(convert(validation.getAttributesValidationResult()));
        result.setFilters(filters);
        result.setCharFilters(charFilters);
        result.setTokenizer(convert(validation.getTokenizerValidationResult()));
        return result;
    }

    private Map<String, Object> convert(FieldAttributesValidationResult validationResult) {
        HashMap<String, Object> result = new HashMap<>(validationResult.getAttributesOnlyInSchema());
        if (validationResult.getClassName() != null) {
            result.put(CLASS_ATTRIBUTE, validationResult.getClassName());
        }
        if (validationResult.getName() != null) {
            result.put(NAME_ATTRIBUTE, validationResult.getName());
        }
        return result;
    }
}
