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

import static com.indoqa.solr.utils.validation.SchemaCheck.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import java.util.Optional;

import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.results.CopyFieldsValidationResult;
import com.indoqa.solr.utils.validation.results.FieldAttributesValidationResult;
import com.indoqa.solr.utils.validation.results.SchemaValidationResult;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class ValidationCopyFieldsTest extends AbstractSolrSchemaCheckerTest {

    @Test
    public void testCopyFieldsValidation() throws SolrSchemaException {
        FieldAttributesValidationResult modifiedField = new FieldAttributesValidationResult();
        modifiedField.setName("*");
        modifiedField.addDifferentAttribute("maxChars", 250, 300);

        FieldAttributesValidationResult fieldOnlyInSchema = new FieldAttributesValidationResult();
        fieldOnlyInSchema.setName("language");
        fieldOnlyInSchema.addAttributeOnlyInSchema("source", "language");
        fieldOnlyInSchema.addAttributeOnlyInSchema("dest", "name");
        FieldAttributesValidationResult fieldOnlyInSchema2 = new FieldAttributesValidationResult();
        fieldOnlyInSchema2.setName("name");
        fieldOnlyInSchema2.addAttributeOnlyInSchema("source", "name");
        fieldOnlyInSchema2.addAttributeOnlyInSchema("dest", "language");

        FieldAttributesValidationResult fieldStillInSolr = new FieldAttributesValidationResult();
        fieldStillInSolr.setName("id");
        fieldStillInSolr.addAttributeStillInSolr("source", "id");
        fieldStillInSolr.addAttributeStillInSolr("dest", "type");
        FieldAttributesValidationResult fieldStillInSolr2 = new FieldAttributesValidationResult();
        fieldStillInSolr2.setName("name");
        fieldStillInSolr2.addAttributeStillInSolr("source", "name");
        fieldStillInSolr2.addAttributeStillInSolr("dest", "core0");

        CopyFieldsValidationResult copyFields = new CopyFieldsValidationResult();
        copyFields.addFieldsModified(modifiedField);
        copyFields.addFieldsOnlyInSchema(fieldOnlyInSchema);
        copyFields.addFieldsOnlyInSchema(fieldOnlyInSchema2);
        copyFields.addFieldStillInSolr(fieldStillInSolr);
        copyFields.addFieldStillInSolr(fieldStillInSolr2);

        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), CHANGED_SCHEMA, COPY_FIELDS);
        assertEquals(1, schemaValidationResult.getResults().size());

        Optional<CopyFieldsValidationResult> result = schemaValidationResult.getResult(CopyFieldsValidationResult.class);
        CopyFieldsValidationResult copyFieldsValidationResult = result.get();
        assertSameProperties(copyFieldsValidationResult, copyFields);

        String errorMessage = schemaValidationResult.getErrorMessage();
        MatcherAssert.assertThat(errorMessage, allOf(containsString("CopyFields only in Schema"),
            containsString("Field: 'language'"),
            containsString("dest=name"),
            containsString("source=language")));
        MatcherAssert.assertThat(errorMessage, allOf(containsString("CopyFields still in Solr"),
            containsString("Field: 'id'"),
            containsString("dest=type"),
            containsString("source=id")));
        MatcherAssert.assertThat(errorMessage, allOf(containsString("CopyFields with modified attributes"),
            containsString("Field: '*'"),
            containsString("maxChars={expected=250, actual=300}")));
    }

}
