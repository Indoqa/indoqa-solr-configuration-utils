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
package com.indoqa.solr.utils.maintenance;

import com.indoqa.solr.utils.maintenance.handlers.SchemaOperations;
import com.indoqa.solr.utils.validation.SolrSchema;
import org.apache.solr.client.solrj.SolrClient;

public class SolrCoreMaintainerConfiguration {

    private SolrClient solrClient;
    private SolrSchema solrSchema;
    private SchemaOperations schemaOperations;

    public static SolrCoreMaintainerConfiguration configOf(SolrClient client, SolrSchema solrSchema, SchemaOperations schemaOperations) {
        SolrCoreMaintainerConfiguration result = new SolrCoreMaintainerConfiguration();
        result.setSolrClient(client);
        result.setSolrSchema(solrSchema);
        result.setSchemaOperations(schemaOperations);
        return result;
    }

    public SolrClient getSolrClient() {
        return solrClient;
    }

    public void setSolrClient(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public SolrSchema getSolrSchema() {
        return solrSchema;
    }

    public void setSolrSchema(SolrSchema solrSchema) {
        this.solrSchema = solrSchema;
    }

    public SchemaOperations getSchemaOperations() {
        return schemaOperations;
    }

    public void setSchemaOperations(SchemaOperations schemaOperations) {
        this.schemaOperations = schemaOperations;
    }
}
