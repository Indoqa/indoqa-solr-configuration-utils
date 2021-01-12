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
import java.util.Optional;

import com.indoqa.solr.utils.InitializationFailedException;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.tests.validation.helper.ExtractedValidations;
import com.indoqa.solr.utils.tests.validation.helper.Operation;
import com.indoqa.solr.utils.validation.results.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;

public class RemoveFieldsHandler extends AbstractFieldsHandler {

    public RemoveFieldsHandler() {
        super(Operation.REMOVE);
    }

    @Override
    protected void handle(SolrClient solrClient, SolrSchema solrSchema, ExtractedValidations extractedValidations)
        throws InitializationFailedException {
        SchemaUpdates schemaUpdates = createSchemaUpdates(extractedValidations);
        processUpdates(solrClient, solrSchema, schemaUpdates.getAllUpdatesRemove());
    }

    private SchemaUpdates createSchemaUpdates(ExtractedValidations extractedValidations) {
        SchemaUpdates schemaUpdates = new SchemaUpdates();
        for (AbstractValidationResult validation : extractedValidations.getValidations()) {
            Optional<List<? extends AbstractValidationResult>> stillInSolr = validation.getStillInSolr();
            if (!stillInSolr.isPresent()) {
                continue;
            }
            for (AbstractValidationResult result : stillInSolr.get()) {
                if (result instanceof FieldTypesValidationResult) {
                    schemaUpdates.addAllFieldTypeUpdatesRemove(createFieldTypeUpdates((FieldTypesValidationResult) result));
                }
                if (result instanceof FieldsValidationResult) {
                    schemaUpdates.addAllFieldUpdatesRemove(createFieldUpdates((FieldsValidationResult) result));
                }
                if (result instanceof DynamicFieldsValidationResult) {
                    schemaUpdates.addAllDynamicUpdatesRemove(createDynamicFieldUpdates((DynamicFieldsValidationResult) result));
                }
                if (result instanceof CopyFieldsValidationResult) {
                    schemaUpdates.addAllCopyFieldUpdatesRemove(createAddCopyFieldUpdates((CopyFieldsValidationResult) result));
                }
            }
        }
        return schemaUpdates;
    }

    private List<SchemaRequest.DeleteFieldType> createFieldTypeUpdates(FieldTypesValidationResult result) {
        return result
            .getFieldsStillInSolr()
            .stream()
            .map(FieldTypeValidationResult::getName)
            .map(SchemaRequest.DeleteFieldType::new)
            .collect(toList());
    }

    private List<SchemaRequest.DeleteField> createFieldUpdates(FieldsValidationResult result) {
        return result
            .getFieldsStillInSolr()
            .stream()
            .map(FieldAttributesValidationResult::getName)
            .map(SchemaRequest.DeleteField::new)
            .collect(toList());
    }

    private List<SchemaRequest.DeleteDynamicField> createDynamicFieldUpdates(DynamicFieldsValidationResult result) {
        return result
            .getFieldsStillInSolr()
            .stream()
            .map(FieldAttributesValidationResult::getName)
            .map(SchemaRequest.DeleteDynamicField::new)
            .collect(toList());
    }

    private List<SchemaRequest.DeleteCopyField> createAddCopyFieldUpdates(CopyFieldsValidationResult result) {
        return result.getFieldsStillInSolr().stream().map(this::convert).collect(toList());
    }

    private SchemaRequest.DeleteCopyField convert(FieldAttributesValidationResult result) {
        String source = (String) result.getAttributesStillInSolr().get(SOURCE_ATTRIBUTE);
        String dest = (String) result.getAttributesStillInSolr().get(DEST_ATTRIBUTE);

        return new SchemaRequest.DeleteCopyField(source, Arrays.asList(dest));
    }
}
