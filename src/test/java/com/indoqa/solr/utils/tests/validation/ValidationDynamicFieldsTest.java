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

import static com.indoqa.solr.utils.validation.SchemaCheck.DYNAMIC_FIELDS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import java.util.Optional;

import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.results.DynamicFieldsValidationResult;
import com.indoqa.solr.utils.validation.results.FieldAttributesValidationResult;
import com.indoqa.solr.utils.validation.results.SchemaValidationResult;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class ValidationDynamicFieldsTest extends AbstractSolrSchemaCheckerTest {

    @Test
    public void testDynamicFieldsValidation() throws SolrSchemaException {
        FieldAttributesValidationResult modifiedField = new FieldAttributesValidationResult();
        modifiedField.setName("open_to_*");
        modifiedField.addDifferentAttribute("type", "long", "int");

        FieldAttributesValidationResult fieldOnlyInSchema = new FieldAttributesValidationResult();
        fieldOnlyInSchema.setName("registered_*");
        fieldOnlyInSchema.addAttributeOnlyInSchema("type", "date");
        fieldOnlyInSchema.addAttributeOnlyInSchema("indexed", true);
        fieldOnlyInSchema.addAttributeOnlyInSchema("stored", true);

        FieldAttributesValidationResult fieldStillInSolr = new FieldAttributesValidationResult();
        fieldStillInSolr.setName("open_from_*");
        fieldStillInSolr.addAttributeStillInSolr("type", "long");
        fieldStillInSolr.addAttributeStillInSolr("indexed", true);
        fieldStillInSolr.addAttributeStillInSolr("stored", true);

        DynamicFieldsValidationResult dynamicFields = new DynamicFieldsValidationResult();
        dynamicFields.addFieldsModified(modifiedField);
        dynamicFields.addFieldsOnlyInSchema(fieldOnlyInSchema);
        dynamicFields.addFieldStillInSolr(fieldStillInSolr);

        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), CHANGED_SCHEMA, DYNAMIC_FIELDS);
        assertEquals(1, schemaValidationResult.getResults().size());

        Optional<DynamicFieldsValidationResult> result = schemaValidationResult.getResult(DynamicFieldsValidationResult.class);
        DynamicFieldsValidationResult dynamicFieldsValidationResult = result.get();
        assertSameProperties(dynamicFieldsValidationResult, dynamicFields);

        String errorMessage = schemaValidationResult.getErrorMessage();
        MatcherAssert.assertThat(errorMessage, allOf(containsString("Fields only in Schema"),
            containsString("Field: 'registered_*'"),
            containsString("type=date"),
            containsString("stored=true"),
            containsString("indexed=true")));
        MatcherAssert.assertThat(errorMessage, allOf(containsString("Fields still in Solr"),
            containsString("Field: 'open_from_*'"),
            containsString("indexed=true"),
            containsString("stored=true"),
            containsString("type=long")));
        MatcherAssert.assertThat(errorMessage, allOf(containsString("Fields with modified attributes"),
            containsString("Field: 'open_to_*'"),
            containsString("type={expected=long, actual=int}")));
    }
}
