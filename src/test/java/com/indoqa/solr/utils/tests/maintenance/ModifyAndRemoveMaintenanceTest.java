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

import java.util.Map;
import java.util.Optional;

import com.indoqa.solr.utils.InitializationFailedException;
import com.indoqa.solr.utils.maintenance.ClasspathSolrCoreMaintainer;
import com.indoqa.solr.utils.maintenance.ClasspathSolrCoreMaintainerBuilder;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.schema.FieldTypeDefinition;
import org.apache.solr.client.solrj.response.schema.SchemaRepresentation;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModifyAndRemoveMaintenanceTest extends AbstractMaintenanceTest {

    protected static String INITIAL_COLLECTION = "initial";
    protected static SolrSchema CHANGED_SCHEMA;

    @BeforeClass
    public static void setupSchema() {
        try {
            CHANGED_SCHEMA = new SolrSchema(
                INITIAL_COLLECTION,
                "src/test/resources/solr/maintenance/modify_remove_changed/conf/schema.xml");
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
        assertEquals(2, schema.getCopyFields().size());
        FieldTypeDefinition newTextNgram = schema
            .getFieldTypes()
            .stream()
            .filter(f -> "new_text_ngram".equals(f.getAttributes().get("name")))
            .findFirst()
            .get();
        assertTrue("TermPositions should be true", (Boolean) newTextNgram.getAttributes().get("termPositions"));
        assertEquals(
            "31",
            newTextNgram
                .getAnalyzer()
                .getFilters()
                .stream()
                .filter(f -> "solr.NGramFilterFactory".equals(f.get("class")))
                .findFirst()
                .get()
                .get("maxGramSize"));
        assertEquals(
            "_",
            newTextNgram
                .getAnalyzer()
                .getCharFilters()
                .stream()
                .filter(f -> "solr.PatternReplaceCharFilterFactory".equals(f.get("class")))
                .findFirst()
                .get()
                .get("replacement"));

        assertEquals(5, schema.getFields().size());
        assertTrue(
            "MultiValued should be true",
            (Boolean) schema.getFields().stream().filter(f -> "language".equals(f.get("name"))).findFirst().get().get("multiValued"));
        assertTrue(
            "MultiValued should be true",
            (Boolean) schema.getFields().stream().filter(f -> "name".equals(f.get("name"))).findFirst().get().get("multiValued"));

        assertEquals(2, schema.getDynamicFields().size());
        assertEquals(
            "dates",
            schema.getDynamicFields().stream().filter(d -> "registered_*".equals(d.get("name"))).findFirst().get().get("type"));
        assertEquals(
            "date",
            schema.getDynamicFields().stream().filter(d -> "open_to_*".equals(d.get("name"))).findFirst().get().get("type"));

        assertEquals(2, schema.getCopyFields().size());
        assertTrue(
            "CopyField should be present", schema
                .getCopyFields()
                .stream()
                .anyMatch(c -> "name".equals(c.get("source")) && "language".equals(c.get("dest"))));
        Optional<Map<String, Object>> copyField = schema
            .getCopyFields()
            .stream()
            .filter(c -> "name".equals(c.get("source")) && "type".equals(c.get("dest")))
            .findFirst();
        assertTrue("CopyField should be present", copyField.isPresent());
        assertEquals(255, copyField.get().get("maxChars"));

        ClasspathSolrCoreMaintainer classpathSolrCoreMaintainer = new ClasspathSolrCoreMaintainerBuilder()
            .addConfiguration(configOf(getRunningSolr(), CHANGED_SCHEMA, ALLOW_MODIFYING_REMOVING_FIELDS))
            .build();
        classpathSolrCoreMaintainer.initialize();

        schema = getSchema();
        assertEquals(17, schema.getFieldTypes().size());
        assertEquals(4, schema.getFields().size());
        assertEquals(1, schema.getDynamicFields().size());
        assertEquals(1, schema.getCopyFields().size());
        newTextNgram = schema
            .getFieldTypes()
            .stream()
            .filter(f -> "new_text_ngram".equals(f.getAttributes().get("name")))
            .findFirst()
            .get();
        assertTrue("TermPositions should be false", (Boolean) newTextNgram.getAttributes().get("termPositions"));
        assertEquals(
            "31",
            newTextNgram
                .getAnalyzer()
                .getFilters()
                .stream()
                .filter(f -> "solr.NGramFilterFactory".equals(f.get("class")))
                .findFirst()
                .get()
                .get("maxGramSize"));
        assertEquals(
            "_",
            newTextNgram
                .getAnalyzer()
                .getCharFilters()
                .stream()
                .filter(f -> "solr.PatternReplaceCharFilterFactory".equals(f.get("class")))
                .findFirst()
                .get()
                .get("replacement"));

        assertEquals(4, schema.getFields().size());
        assertFalse(
            "MultiValued should be false",
            (Boolean) schema.getFields().stream().filter(f -> "name".equals(f.get("name"))).findFirst().get().get("multiValued"));

        assertEquals(1, schema.getDynamicFields().size());
        assertEquals(
            "long",
            schema.getDynamicFields().stream().filter(d -> "open_to_*".equals(d.get("name"))).findFirst().get().get("type"));

        assertEquals(1, schema.getCopyFields().size());
        assertTrue(
            "CopyField should be present", schema
                .getCopyFields()
                .stream()
                .anyMatch(c -> "name".equals(c.get("source")) && "type".equals(c.get("dest"))));
        copyField = schema
            .getCopyFields()
            .stream()
            .filter(c -> "name".equals(c.get("source")) && "type".equals(c.get("dest")))
            .findFirst();
        assertTrue("CopyField should be present", copyField.isPresent());
        assertEquals(250, copyField.get().get("maxChars"));
    }

    protected SolrClient getRunningSolr() {
        return getSolrInfrastructure().getModifyRemoveSolrClient();
    }

}
