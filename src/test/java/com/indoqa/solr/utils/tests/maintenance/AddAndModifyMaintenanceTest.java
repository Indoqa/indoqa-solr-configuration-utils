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
import static com.indoqa.solr.utils.maintenance.handlers.SchemaOperations.ALLOW_MODIFYING_REMOVING_FIELDS;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.Optional;

import com.indoqa.solr.utils.InitializationFailedException;
import com.indoqa.solr.utils.maintenance.ClasspathSolrCoreMaintainer;
import com.indoqa.solr.utils.maintenance.ClasspathSolrCoreMaintainerBuilder;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.schema.SchemaRepresentation;
import org.junit.BeforeClass;
import org.junit.Test;

public class AddAndModifyMaintenanceTest extends AbstractMaintenanceTest {

    protected static String INITIAL_COLLECTION = "initial";
    protected static SolrSchema CHANGED_SCHEMA;

    @BeforeClass
    public static void setupSchema() {
        try {
            CHANGED_SCHEMA = new SolrSchema(
                INITIAL_COLLECTION,
                "src/test/resources/solr/maintenance/add_modify_changed/conf/schema.xml");
        } catch (SolrSchemaException e) {
            throw new IllegalStateException("Could not initialize SolrSchema.", e);
        }
    }

    @Test
    public void test() throws InitializationFailedException {
        SchemaRepresentation schema = getSchema();
        assertEquals(18, schema.getFieldTypes().size());
        assertEquals(5, schema.getFields().size());
        assertEquals(2, schema.getDynamicFields().size());
        assertEquals(3, schema.getCopyFields().size());

        assertTrue(
            "CopyField should be present", schema
                .getCopyFields()
                .stream()
                .anyMatch(c -> "*".equals(c.get("source")) && "name".equals(c.get("dest"))));
        Optional<Map<String, Object>> copyField = schema
            .getCopyFields()
            .stream()
            .filter(c -> "*".equals(c.get("source")) && "name".equals(c.get("dest")))
            .findFirst();
        assertTrue("CopyField should be present", copyField.isPresent());
        assertEquals(250, copyField.get().get("maxChars"));

        ClasspathSolrCoreMaintainer classpathSolrCoreMaintainer = new ClasspathSolrCoreMaintainerBuilder()
            .addConfiguration(configOf(getRunningSolr(), CHANGED_SCHEMA, ALLOW_MODIFYING_REMOVING_FIELDS))
            .build();
        classpathSolrCoreMaintainer.initialize();

        schema = getSchema();
        assertEquals(16, schema.getFieldTypes().size());
        assertEquals(4, schema.getFields().size());
        assertEquals(1, schema.getDynamicFields().size());
        assertEquals(2, schema.getCopyFields().size());

        assertTrue(
            "CopyField should be present", schema
                .getCopyFields()
                .stream()
                .anyMatch(c -> "*".equals(c.get("source")) && "name".equals(c.get("dest"))));
        copyField = schema
            .getCopyFields()
            .stream()
            .filter(c -> "*".equals(c.get("source")) && "name".equals(c.get("dest")))
            .findFirst();
        assertTrue("CopyField should be present", copyField.isPresent());
        assertEquals(260, copyField.get().get("maxChars"));
    }

    protected SolrClient getRunningSolr() {
        return getSolrInfrastructure().getAddModifySolrClient();
    }
}
