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
package com.indoqa.solr.utils.tests.maintenance;

import static com.indoqa.solr.utils.maintenance.SolrCoreMaintainerConfiguration.configOf;
import static com.indoqa.solr.utils.maintenance.handlers.SchemaOperations.*;
import static org.junit.Assert.*;

import com.indoqa.solr.utils.InitializationFailedException;
import com.indoqa.solr.utils.maintenance.ClasspathSolrCoreMaintainer;
import com.indoqa.solr.utils.maintenance.handlers.SchemaOperations;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.schema.FieldTypeDefinition;
import org.apache.solr.client.solrj.response.schema.SchemaRepresentation;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.BeforeClass;
import org.junit.Test;

public class WrongOperationMaintenanceTest extends AbstractMaintenanceTest {

    protected static String INITIAL_COLLECTION = "initial";
    protected static SolrSchema CHANGED_SCHEMA;

    @BeforeClass
    public static void setupSchema() {
        try {
            CHANGED_SCHEMA = new SolrSchema(INITIAL_COLLECTION,
                "src/test/resources/solr/maintenance/wrong_operation_changed/conf/schema.xml");
        } catch (SolrSchemaException e) {
            throw new IllegalStateException("Could not initialize SolrSchema.", e);
        }
    }

    @Test
    public void testNoAdd() {
        assertSchema();

        runSolrCoreMaintainer(ALLOW_MODIFYING_REMOVING_FIELDS, "ADD");

        assertSchema();
    }

    @Test
    public void testNoModify() {
        assertSchema();

        runSolrCoreMaintainer(ALLOW_ADDING_REMOVING_FIELDS, "MODIFY");

        assertSchema();
    }

    @Test
    public void testNoRemove() {
        assertSchema();

        runSolrCoreMaintainer(ALLOW_ADDING_MODIFYING_FIELDS, "REMOVE");

        assertSchema();
    }

    @Test
    public void testNoAddRemove() {
        assertSchema();

        runSolrCoreMaintainer(ALLOW_MODIFYING_FIELDS, "ADD");

        assertSchema();
    }

    @Test
    public void testNoAddModify() {
        assertSchema();

        runSolrCoreMaintainer(ALLOW_REMOVING_FIELDS, "ADD");

        assertSchema();
    }

    @Test
    public void testNoModifyRemove() {
        assertSchema();

        runSolrCoreMaintainer(ALLOW_ADDING_FIELDS, "REMOVE");

        assertSchema();
    }

    private void runSolrCoreMaintainer(SchemaOperations schemaOperations, String operation) {
        try {
            new ClasspathSolrCoreMaintainer(configOf(getRunningSolr(), CHANGED_SCHEMA, schemaOperations)).initialize();
            fail("Should have thrown an InitializationFailedException.");
        } catch (InitializationFailedException e) {
            MatcherAssert.assertThat(e.getMessage(), CoreMatchers.containsString("Operation to '" + operation + "' fields not permitted."));
        }
    }

    private void assertSchema() {
        SchemaRepresentation schema = getSchema();
        assertEquals(5, schema.getFieldTypes().size());
        assertEquals(4, schema.getFields().size());
        assertEquals(2, schema.getDynamicFields().size());
        assertEquals(1, schema.getCopyFields().size());

        FieldTypeDefinition newTextNgram = schema
            .getFieldTypes()
            .stream()
            .filter(f -> "new_text_ngram".equals(f.getAttributes().get("name")))
            .findFirst()
            .get();
        assertFalse("TermPositions should be false", (Boolean) newTextNgram.getAttributes().get("termPositions"));
        assertEquals("100", newTextNgram.getAttributes().get("positionIncrementGap"));

        FieldTypeDefinition longFieldType = schema
            .getFieldTypes()
            .stream()
            .filter(f -> "long".equals(f.getAttributes().get("name")))
            .findFirst()
            .get();
        assertEquals("0", longFieldType.getAttributes().get("precisionStep"));

        assertTrue("CopyField should be present",
            schema
                .getCopyFields()
                .stream()
                .filter(c -> "name".equals(c.get("source")) && "language".equals(c.get("dest")))
                .findFirst()
                .isPresent());

        assertTrue((Boolean) schema
            .getDynamicFields()
            .stream()
            .filter(d -> "open_to_*".equals(d.get("name")))
            .findFirst()
            .get()
            .get("indexed"));
    }

    protected SolrClient getRunningSolr() {
        return getSolrInfrastructure().getWrongOperationSolrClient();
    }
}
