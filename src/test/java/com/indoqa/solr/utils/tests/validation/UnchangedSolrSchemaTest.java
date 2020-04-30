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

import static org.junit.Assert.assertTrue;

import com.indoqa.solr.utils.validation.SchemaCheck;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.results.SchemaValidationResult;
import org.junit.Test;

public class UnchangedSolrSchemaTest extends AbstractSolrSchemaCheckerTest {

    private static SolrSchema INITIAL_SCHEMA;

    static {
        try {
            INITIAL_SCHEMA = new SolrSchema(INITIAL_COLLECTION, INITIAL_PATH);
        } catch (SolrSchemaException e) {
            throw new IllegalStateException("Could not initialize SolrSchema.", e);
        }
    }

    @Test
    public void testNameValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), INITIAL_SCHEMA, SchemaCheck.NAME);
        assertTrue("Name validation should not have errors.", schemaValidationResult.isEmpty());
    }

    @Test
    public void testVersionValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), INITIAL_SCHEMA, SchemaCheck.VERSION);
        assertTrue("Version validation should not have errors.", schemaValidationResult.isEmpty());
    }

    @Test
    public void testUniqueKeyValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), INITIAL_SCHEMA, SchemaCheck.UNIQUE_KEY);
        assertTrue("UniqueKey validation should not have errors.", schemaValidationResult.isEmpty());
    }

    @Test
    public void testFieldsValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), INITIAL_SCHEMA, SchemaCheck.FIELDS);
        assertTrue("Fields validation should not have errors.", schemaValidationResult.isEmpty());
    }

    @Test
    public void testDynamicFieldsValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), INITIAL_SCHEMA, SchemaCheck.DYNAMIC_FIELDS);
        assertTrue("DynamicFields validation should not have errors.", schemaValidationResult.isEmpty());
    }

    @Test
    public void testFieldTypesValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), INITIAL_SCHEMA, SchemaCheck.FIELD_TYPES);
        assertTrue("FieldTypes validation should not have errors.", schemaValidationResult.isEmpty());
    }

    @Test
    public void testCopyFieldsValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), INITIAL_SCHEMA, SchemaCheck.COPY_FIELDS);
        assertTrue("CopyFields validation should not have errors.", schemaValidationResult.isEmpty());
    }

    @Test
    public void testBasicValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), INITIAL_SCHEMA, SchemaCheck.BASIC);
        assertTrue("Basic validation should not have errors.", schemaValidationResult.isEmpty());
    }

    @Test
    public void testAllFieldsValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), INITIAL_SCHEMA, SchemaCheck.ALL_FIELDS);
        assertTrue("AllFields validation should not have errors.", schemaValidationResult.isEmpty());
    }

    @Test
    public void testSettingsValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), INITIAL_SCHEMA, SchemaCheck.SETTINGS);
        assertTrue("Settings validation should not have errors.", schemaValidationResult.isEmpty());
    }

    @Test
    public void testCompleteValidation() throws SolrSchemaException {
        SchemaValidationResult schemaValidationResult = this.validateSchema(getInitialSolr(), INITIAL_SCHEMA, SchemaCheck.COMPLETE);
        assertTrue("Complete validation should not have errors.", schemaValidationResult.isEmpty());
    }
}
