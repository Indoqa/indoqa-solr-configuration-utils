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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.indoqa.solr.utils.validation.results.AbstractFieldsValidationResult;
import com.indoqa.solr.utils.validation.results.FieldAttributesValidationResult;
import com.indoqa.solr.utils.validation.results.AbstractValidationResult;

public abstract class AbstractSchemaAbstractFieldsValidation<V extends AbstractValidationResult> implements SchemaValidation<V> {

    protected final void extractSchemaValidations(AbstractFieldsValidationResult result, Collection<Map<String, Object>> fields,
        Collection<Map<String, Object>> schemaResponseFields) {
        Map<String, Map<String, Object>> namedSchemaResponseFields = map(schemaResponseFields);

        for (Map<String, Object> eachField : fields) {
            String name = (String) eachField.get(NAME_ATTRIBUTE);
            Map<String, Object> schemaResponseField = namedSchemaResponseFields.remove(name);

            FieldAttributesValidationResult fieldValidationResult = new FieldAttributesValidationResult();
            fieldValidationResult.setName(name);
            String className = (String) eachField.get(CLASS_ATTRIBUTE);
            fieldValidationResult.setClassName(className);

            if (schemaResponseField == null) {
                eachField
                    .entrySet()
                    .stream()
                    .filter(es -> !NAME_ATTRIBUTE.equalsIgnoreCase(es.getKey()))
                    .filter(es -> !CLASS_ATTRIBUTE.equalsIgnoreCase(es.getKey()))
                    .forEach(es -> fieldValidationResult.addAttributeOnlyInSchema(es.getKey(), es.getValue()));
                result.addFieldsOnlyInSchema(fieldValidationResult);
                continue;
            }

            fieldValidationResult.addAttributeValidation(AttributesValidator.validate(eachField, schemaResponseField));

            result.addFieldsModified(fieldValidationResult);
        }

        namedSchemaResponseFields.entrySet().stream().forEach(es -> {
            FieldAttributesValidationResult fieldValidationResult = new FieldAttributesValidationResult();
            fieldValidationResult.setName(es.getKey());
            fieldValidationResult.setClassName((String) es.getValue().get(CLASS_ATTRIBUTE));
            es
                .getValue()
                .entrySet()
                .stream()
                .filter(vs -> !NAME_ATTRIBUTE.equalsIgnoreCase(vs.getKey()))
                .filter(vs -> !CLASS_ATTRIBUTE.equalsIgnoreCase(vs.getKey()))
                .forEach(vs -> fieldValidationResult.addAttributeStillInSolr(vs.getKey(), vs.getValue()));
            result.addFieldStillInSolr(fieldValidationResult);
        });
    }

    private Map<String, Map<String, Object>> map(Collection<Map<String, Object>> schemaResponseFields) {
        Map<String, Map<String, Object>> resultFields = new HashMap<>();
        for (Map<String, Object> schemaResponseField : schemaResponseFields) {
            HashMap<String, Object> value = new HashMap<>();
            for (Map.Entry<String, Object> entry : schemaResponseField.entrySet()) {
                value.put(entry.getKey(), entry.getValue());
            }
            resultFields.put((String) schemaResponseField.get(NAME_ATTRIBUTE), value);
        }
        return resultFields;
    }
}
