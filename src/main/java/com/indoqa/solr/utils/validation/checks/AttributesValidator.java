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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.indoqa.solr.utils.validation.results.AttributesValidationResult;
import com.indoqa.solr.utils.validation.results.ValuesOrigin;

public class AttributesValidator {

    private AttributesValidator() {
        // hide constructor
    }

    public static AttributesValidationResult validate(Map<String, Object> schema, Map<String, Object> solr) {
        if (schema == null && solr == null) {
            AttributesValidationResult attributesValidationResult = new AttributesValidationResult();
            attributesValidationResult.setValuesOrigin(ValuesOrigin.BOTH);
            return attributesValidationResult;
        }
        Map<String, Object> solrAttributes = getNullsafeAttributes(solr);
        Map<String, Object> schemaAttributes = getNullsafeAttributes(schema);

        AttributesValidationResult result = new AttributesValidationResult();
        result.setName((String) schemaAttributes.get(NAME_ATTRIBUTE));
        result.setClassName((String) schemaAttributes.get(CLASS_ATTRIBUTE));

        boolean name = false;
        boolean clazz = false;
        for (Map.Entry<String, Object> eachAttribute : schemaAttributes.entrySet()) {
            String attributeName = eachAttribute.getKey();
            Object valueInSolr = solrAttributes.remove(attributeName);
            if (NAME_ATTRIBUTE.equalsIgnoreCase(attributeName)) {
                name = valueInSolr != null;
                continue;
            }
            if (CLASS_ATTRIBUTE.equalsIgnoreCase(attributeName)) {
                clazz = valueInSolr != null;
                continue;
            }
            if (valueInSolr == null) {
                result.addAttributeOnlyInSchema(attributeName, eachAttribute.getValue());
                continue;
            }

            if (!checkValuesAreEqual(eachAttribute.getValue(), valueInSolr)) {
                result.addDifferentAttribute(attributeName, eachAttribute.getValue(), valueInSolr);
            }
        }

        if ((schemaAttributes.isEmpty() && solrAttributes.isEmpty()) || name || clazz) {
            result.setValuesOrigin(ValuesOrigin.BOTH);
        } else {
            result.setValuesOrigin(ValuesOrigin.ONLY_IN_SCHEMA);
        }

        for (Map.Entry<String, Object> attributeStillInSolr : solrAttributes.entrySet()) {
            String attributeName = attributeStillInSolr.getKey();
            if (NAME_ATTRIBUTE.equalsIgnoreCase(attributeName)) {
                result.setName((String) attributeStillInSolr.getValue());
                result.setValuesOrigin(ValuesOrigin.STILL_IN_SOLR);
                continue;
            }
            if (CLASS_ATTRIBUTE.equalsIgnoreCase(attributeName)) {
                result.setClassName((String) attributeStillInSolr.getValue());
                result.setValuesOrigin(ValuesOrigin.STILL_IN_SOLR);
                continue;
            }
            result.addAttributeStillInSolr(attributeName, attributeStillInSolr.getValue());
        }

        return result;
    }

    private static boolean checkValuesAreEqual(Object value, Object valueInSolr) {
        // check for boolean values, custom field attributes may return as string literal and not as primitive boolean.
        if (value instanceof Boolean || valueInSolr instanceof  Boolean) {
            return value.toString().equalsIgnoreCase(valueInSolr.toString());
        }

        return Objects.equals(value, valueInSolr);
    }

    private static Map<String, Object> getNullsafeAttributes(Map<String, Object> attributes) {
        if (attributes == null) {
            return new HashMap<>();
        }
        return new HashMap<>(attributes);
    }

}
