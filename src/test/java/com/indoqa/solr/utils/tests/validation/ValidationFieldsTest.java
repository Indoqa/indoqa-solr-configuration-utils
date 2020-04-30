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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import java.util.Optional;

import com.indoqa.solr.utils.validation.SchemaCheck;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.results.FieldAttributesValidationResult;
import com.indoqa.solr.utils.validation.results.FieldsValidationResult;
import com.indoqa.solr.utils.validation.results.SchemaValidationResult;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class ValidationFieldsTest extends AbstractSolrSchemaCheckerTest {

    @Test
    public void testFieldsValidation() throws SolrSchemaException {
        FieldAttributesValidationResult modifiedField = new FieldAttributesValidationResult();
        modifiedField.setName("name");
        modifiedField.addAttributeStillInSolr("indexed", true);
        modifiedField.addAttributeOnlyInSchema("required", true);
        modifiedField.addDifferentAttribute("multiValued", true, false);
        modifiedField.addDifferentAttribute("type", "strings", "string");

        FieldAttributesValidationResult fieldOnlyInSchema = new FieldAttributesValidationResult();
        fieldOnlyInSchema.setName("language");
        fieldOnlyInSchema.addAttributeOnlyInSchema("type", "string");
        fieldOnlyInSchema.addAttributeOnlyInSchema("indexed", true);
        fieldOnlyInSchema.addAttributeOnlyInSchema("stored", true);
        fieldOnlyInSchema.addAttributeOnlyInSchema("multiValued", false);

        FieldAttributesValidationResult fieldStillInSolr = new FieldAttributesValidationResult();
        fieldStillInSolr.setName("core0");
        fieldStillInSolr.addAttributeStillInSolr("type", "string");
        fieldStillInSolr.addAttributeStillInSolr("indexed", true);
        fieldStillInSolr.addAttributeStillInSolr("stored", true);
        fieldStillInSolr.addAttributeStillInSolr("multiValued", false);

        FieldsValidationResult fields = new FieldsValidationResult();
        fields.addFieldStillInSolr(fieldStillInSolr);
        fields.addFieldsOnlyInSchema(fieldOnlyInSchema);
        fields.addFieldsModified(modifiedField);

        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), CHANGED_SCHEMA, SchemaCheck.FIELDS);
        assertEquals(1, schemaValidationResult.getResults().size());

        Optional<FieldsValidationResult> result = schemaValidationResult.getResult(FieldsValidationResult.class);
        FieldsValidationResult fieldsValidationResult = result.get();

        assertSameProperties(fieldsValidationResult, fields);
        String errorMessage = schemaValidationResult.getErrorMessage();
        MatcherAssert.assertThat(errorMessage, allOf(
            containsString("Fields only in Schema"),
            containsString("Field: 'language'"),
            containsString("indexed=true"),
            containsString("stored=true"),
            containsString("multiValued=false"),
            containsString("type=string")));
        MatcherAssert.assertThat(errorMessage, allOf(
            containsString("Fields still in Solr"),
            containsString("Field: 'core0'"),
            containsString("indexed=true"),
            containsString("stored=true"),
            containsString("multiValued=false"),
            containsString("type=string")));
        MatcherAssert.assertThat(errorMessage, allOf(
            containsString("Fields "),
            containsString("Field: 'name'"),
            containsString("required=true"),
            containsString("multiValued={expected=true, actual=false}"),
            containsString("type={expected=strings, actual=string}"),
            containsString("indexed=true")));
    }
}
