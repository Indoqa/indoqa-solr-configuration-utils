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
import static com.indoqa.solr.utils.maintenance.handlers.SchemaOperations.ALLOW_ADDING_REMOVING_FIELDS;

import java.io.IOException;

import com.indoqa.solr.utils.tests.rules.SolrInfrastructureRule;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaRepresentation;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.junit.ClassRule;

public abstract class AbstractMaintenanceTest {

    @ClassRule
    public static SolrInfrastructureRule solrInfrastructureRule = new SolrInfrastructureRule();

    public static SolrInfrastructureRule getSolrInfrastructure() {
        return solrInfrastructureRule;
    }

    protected SchemaRepresentation getSchema() {
        try {
            SchemaRequest schemaRequest = new SchemaRequest();
            SchemaResponse process = schemaRequest.process(getRunningSolr());
            return process.getSchemaRepresentation();
        } catch (SolrServerException | IOException e) {
            throw new IllegalStateException("Error getting schema.", e);
        }
    }

    protected abstract SolrClient getRunningSolr();

}
