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
import static org.junit.Assert.*;

import java.util.Optional;

import com.indoqa.solr.utils.validation.SchemaCheck;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.results.*;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class SingleValidationTest extends AbstractSolrSchemaCheckerTest {

    @Test
    public void testChangedNameValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), CHANGED_SCHEMA, SchemaCheck.NAME);
        assertEquals(1, schemaValidationResult.getResults().size());

        Optional<SchemaNameValidationResult> nameValidationResult = schemaValidationResult.getResult(SchemaNameValidationResult.class);
        DifferentValue differentValue = nameValidationResult.get().getDifferentValue();

        assertNotNull(differentValue);
        assertEquals("example core zero 1", differentValue.getExpected());
        assertEquals("example core zero", differentValue.getActual());

        String errorMessage = schemaValidationResult.getErrorMessage(0);
        MatcherAssert.assertThat(errorMessage, containsString("Schema name differ."));
        MatcherAssert.assertThat(errorMessage,
            allOf(containsString("expected=example core zero 1"), containsString("actual=example core zero")));
    }

    @Test
    public void testChangedVersionValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), CHANGED_SCHEMA, SchemaCheck.VERSION);
        assertEquals(1, schemaValidationResult.getResults().size());

        Optional<SchemaVersionValidationResult> versionValidationResult = schemaValidationResult.getResult(
            SchemaVersionValidationResult.class);

        DifferentValue differentValue = versionValidationResult.get().getDifferentValue();

        assertNotNull(differentValue);
        assertEquals(1.2f, differentValue.getExpected());
        assertEquals(1.1f, differentValue.getActual());

        String errorMessage = schemaValidationResult.getErrorMessage(0);
        MatcherAssert.assertThat(errorMessage, containsString("Schema version differ."));
        MatcherAssert.assertThat(errorMessage, allOf(containsString("expected=1.2"), containsString("actual=1.1")));
    }

    @Test
    public void testChangedUniqueKeyValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), CHANGED_SCHEMA, SchemaCheck.UNIQUE_KEY);
        assertEquals(1, schemaValidationResult.getResults().size());

        Optional<SchemaUniqueKeyValidationResult> uniqueKeyValidationResult = schemaValidationResult.getResult(
            SchemaUniqueKeyValidationResult.class);

        DifferentValue differentValue = uniqueKeyValidationResult.get().getDifferentValue();

        assertNotNull(differentValue);
        assertEquals("type", differentValue.getExpected());
        assertEquals("id", differentValue.getActual());

        String errorMessage = schemaValidationResult.getErrorMessage(0);
        MatcherAssert.assertThat(errorMessage, containsString("Schema uniqueKey differ."));
        MatcherAssert.assertThat(errorMessage, allOf(containsString("expected=type"), containsString("actual=id")));
    }
}
