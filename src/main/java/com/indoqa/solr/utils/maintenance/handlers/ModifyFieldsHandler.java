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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.indoqa.solr.utils.InitializationFailedException;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.tests.validation.helper.ExtractedValidations;
import com.indoqa.solr.utils.tests.validation.helper.Operation;
import com.indoqa.solr.utils.validation.results.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.schema.FieldTypeDefinition;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;

public class ModifyFieldsHandler extends AbstractFieldsHandler {

    public ModifyFieldsHandler() {
        super(Operation.MODIFY);
    }

    @Override
    protected void handle(SolrClient solrClient, SolrSchema solrSchema, ExtractedValidations extractedValidations)
        throws InitializationFailedException {
        SchemaUpdates schemaUpdates = createSchemaUpdates(extractedValidations, solrSchema);
        processUpdates(solrClient, solrSchema, schemaUpdates.getAllUpdatesModify());
    }

    private SchemaUpdates createSchemaUpdates(ExtractedValidations extractedValidations, SolrSchema solrSchema) {
        SchemaUpdates schemaUpdates = new SchemaUpdates();
        for (AbstractValidationResult validation : extractedValidations.getValidations()) {
            Optional<List<? extends AbstractValidationResult>> modified = validation.getModified();
            if (!modified.isPresent()) {
                continue;
            }
            for (AbstractValidationResult result : modified.get()) {
                if (result instanceof FieldTypesValidationResult) {
                    schemaUpdates.addAllFieldTypeModify(createFieldTypeUpdates((FieldTypesValidationResult) result, solrSchema));
                }
                if (result instanceof FieldsValidationResult) {
                    schemaUpdates.addAllFieldUpdatesModify(createFieldUpdates((FieldsValidationResult) result, solrSchema));
                }
                if (result instanceof DynamicFieldsValidationResult) {
                    schemaUpdates.addAllDynamicUpdatesModify(createDynamicFieldUpdates((DynamicFieldsValidationResult) result,
                        solrSchema));
                }
                if (result instanceof CopyFieldsValidationResult) {
                    handleCopyFieldModification(schemaUpdates, (CopyFieldsValidationResult) result, solrSchema);
                }
            }
        }
        return schemaUpdates;
    }

    private Function<String, Optional<Map<String, Object>>> mapNameToField(List<Map<String, Object>> fields) {
        return name -> fields.stream().filter(t -> name.equals(t.get(NAME_ATTRIBUTE))).findFirst();
    }

    private List<SchemaRequest.ReplaceField> createFieldUpdates(FieldsValidationResult result, SolrSchema solrSchema) {
        List<Map<String, Object>> fields = solrSchema.getFields();
        return result
            .getFieldsModified()
            .stream()
            .map(FieldAttributesValidationResult::getName)
            .map(mapNameToField(fields))
            .map(Optional::get)
            .map(SchemaRequest.ReplaceField::new)
            .collect(toList());
    }

    private List<SchemaRequest.ReplaceFieldType> createFieldTypeUpdates(FieldTypesValidationResult result, SolrSchema solrSchema) {
        List<FieldTypeDefinition> fieldTypes = solrSchema.getFieldTypes();
        return result
            .getFieldsModified()
            .stream()
            .map(FieldAttributesValidationResult::getName)
            .map(mapNameToFieldType(fieldTypes))
            .map(Optional::get)
            .map(SchemaRequest.ReplaceFieldType::new)
            .collect(toList());
    }

    private Function<String, Optional<FieldTypeDefinition>> mapNameToFieldType(List<FieldTypeDefinition> fieldTypes) {
        return name -> fieldTypes.stream().filter(t -> name.equals(t.getAttributes().get(NAME_ATTRIBUTE))).findFirst();
    }

    private List<SchemaRequest.ReplaceDynamicField> createDynamicFieldUpdates(DynamicFieldsValidationResult result,
        SolrSchema solrSchema) {
        List<Map<String, Object>> dynamicFields = solrSchema.getDynamicFields();
        return result
            .getFieldsModified()
            .stream()
            .map(FieldAttributesValidationResult::getName)
            .map(mapNameToField(dynamicFields))
            .map(Optional::get)
            .map(SchemaRequest.ReplaceDynamicField::new)
            .collect(toList());
    }

    private void handleCopyFieldModification(SchemaUpdates schemaUpdates, CopyFieldsValidationResult result, SolrSchema solrSchema) {
        for (FieldAttributesValidationResult field : result.getFieldsModified()) {
            Optional<Map<String, Object>> possibleAttributes = solrSchema.getCopyFields().stream().filter(filter(field)).findFirst();

            if (!possibleAttributes.isPresent()) {
                continue;
            }
            Map<String, Object> attributes = possibleAttributes.get();

            SchemaRequest.AddCopyField addCopyField = createAdd(attributes);
            SchemaRequest.DeleteCopyField deleteCopyField = createDelete(attributes);

            schemaUpdates.addAllCopyFieldUpdatesModify(deleteCopyField, addCopyField);
        }
    }

    private Predicate<Map<String, Object>> filter(FieldAttributesValidationResult field) {
        return c -> {
            if (!field.getName().equals(c.get(SOURCE_ATTRIBUTE))) {
                return false;
            }
            for (Map.Entry<String, DifferentValue> eachEntry : field.getDifferentAttributeValues().entrySet()) {
                Object object = c.get(eachEntry.getKey());
                if (object == null) {
                    return false;
                }
                if (!object.equals(eachEntry.getValue().getExpected())) {
                    return false;
                }
            }
            return true;
        };
    }

    private SchemaRequest.AddCopyField createAdd(Map<String, Object> attributes) {
        String source = (String) attributes.get(SOURCE_ATTRIBUTE);
        String dest = (String) attributes.get(DEST_ATTRIBUTE);
        Integer maxChars = (Integer) attributes.get(MAX_CHARS_ATTRIBUTES);

        return new SchemaRequest.AddCopyField(source, Arrays.asList(dest), maxChars);
    }

    private SchemaRequest.DeleteCopyField createDelete(Map<String, Object> attributes) {
        String source = (String) attributes.get(SOURCE_ATTRIBUTE);
        String dest = (String) attributes.get(DEST_ATTRIBUTE);

        return new SchemaRequest.DeleteCopyField(source, Arrays.asList(dest));
    }
}
