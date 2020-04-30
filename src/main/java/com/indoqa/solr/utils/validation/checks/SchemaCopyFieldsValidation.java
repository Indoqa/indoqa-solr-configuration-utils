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

import java.util.*;

import com.indoqa.solr.utils.SolrConstants;
import com.indoqa.solr.utils.validation.SchemaCheck;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.results.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.schema.FieldTypeDefinition;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;

public class SchemaCopyFieldsValidation extends SchemaAbstractFieldsValidation<CopyFieldsValidationResult> {

    @Override
    public SchemaCheck getSchemaCheck() {
        return SchemaCheck.COPY_FIELDS;
    }

    @Override
    public CopyFieldsValidationResult validate(SolrSchema solrSchema, SchemaResponse schemaResponse, SolrClient solrClient)
        throws SolrSchemaException {
        List<Map<String, Object>> schemaResponseFields;

        if (schemaResponse == null) {
            schemaResponseFields = process(solrSchema.getCollectionName(),
                new SchemaRequest.CopyFields(),
                solrClient).getCopyFields();
        } else {
            schemaResponseFields = schemaResponse.getSchemaRepresentation().getCopyFields();
        }

        CopyFieldsValidationResult result = new CopyFieldsValidationResult();
        extractCopyFieldsValidation(result, map(solrSchema.getCopyFields()), map(schemaResponseFields));
        return result;
    }

    protected final void extractCopyFieldsValidation(AbstractFieldsValidationResult result, Map<String, Map<String, Object>> fields,
        Map<String, Map<String, Object>> schemaResponseFields) {
        Map<String, Map<String, Object>> namedSchemaResponseFields = new HashMap<>(schemaResponseFields);

        for (Map.Entry<String, Map<String, Object>> eachFieldEntry : fields.entrySet()) {
            Map<String, Object> schemaResponseField = namedSchemaResponseFields.remove(eachFieldEntry.getKey());
            String name = (String) eachFieldEntry.getValue().get(NAME_ATTRIBUTE);

            FieldAttributesValidationResult fieldValidationResult = new FieldAttributesValidationResult();
            fieldValidationResult.setName(name);
            String className = (String) eachFieldEntry.getValue().get(CLASS_ATTRIBUTE);
            fieldValidationResult.setClassName(className);

            if (schemaResponseField == null) {
                eachFieldEntry.getValue()
                    .entrySet()
                    .stream()
                    .filter(es -> !NAME_ATTRIBUTE.equalsIgnoreCase(es.getKey()))
                    .filter(es -> !CLASS_ATTRIBUTE.equalsIgnoreCase(es.getKey()))
                    .forEach(es -> fieldValidationResult.addAttributeOnlyInSchema(es.getKey(), es.getValue()));
                result.addFieldsOnlyInSchema(fieldValidationResult);
                continue;
            }

            fieldValidationResult.addAttributeValidation(AttributesValidator.validate(eachFieldEntry.getValue(), schemaResponseField));

            result.addFieldsModified(fieldValidationResult);
        }

        namedSchemaResponseFields.entrySet().forEach(es -> {
            FieldAttributesValidationResult fieldValidationResult = new FieldAttributesValidationResult();
            fieldValidationResult.setName((String) es.getValue().get(NAME_ATTRIBUTE));
            fieldValidationResult.setClassName((String) es.getValue().get(CLASS_ATTRIBUTE));
            es.getValue()
                .entrySet()
                .stream()
                .filter(vs -> !NAME_ATTRIBUTE.equalsIgnoreCase(vs.getKey()))
                .filter(vs -> !CLASS_ATTRIBUTE.equalsIgnoreCase(vs.getKey()))
                .forEach(vs -> fieldValidationResult.addAttributeStillInSolr(vs.getKey(), vs.getValue()));
            result.addFieldStillInSolr(fieldValidationResult);
        });
    }

    private Map<String, Map<String, Object>> map(List<Map<String, Object>> list) {
        Map<String, Map<String, Object>> result = new HashMap<>();

        if (list == null) {
            return result;
        }

        for (Map<String, Object> eachCopyFieldAttributes : list) {
            String source = String.valueOf(eachCopyFieldAttributes.get("source"));
            String dest = String.valueOf(eachCopyFieldAttributes.get("dest"));
            String name = source + "|" + dest;
            eachCopyFieldAttributes.put(NAME_ATTRIBUTE, source);
            result.put(name, eachCopyFieldAttributes);
        }

        return result;
    }
}
