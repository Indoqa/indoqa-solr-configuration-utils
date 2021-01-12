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
package com.indoqa.solr.utils.validation;

public final class SchemaCheckConfiguration {

    private SolrSchema solrSchema;
    private SchemaCheck[] solrSchemaChecks;

    public static SchemaCheckConfiguration of(SolrSchema solrSchema, SchemaCheck... solrSchemaChecks) {
        SchemaCheckConfiguration result = new SchemaCheckConfiguration();

        result.setSolrSchema(solrSchema);
        result.setSolrSchemaChecks(solrSchemaChecks);

        return result;
    }

    private SchemaCheckConfiguration() {
        super();
    }

    public SolrSchema getSolrSchema() {
        return this.solrSchema;
    }

    public void setSolrSchema(SolrSchema solrSchema) {
        this.solrSchema = solrSchema;
    }

    public SchemaCheck[] getSolrSchemaChecks() {
        return this.solrSchemaChecks;
    }

    public void setSolrSchemaChecks(SchemaCheck[] solrSchemaChecks) {
        this.solrSchemaChecks = solrSchemaChecks;
    }

    public boolean needsCheck(SchemaCheck schemaCheck) {
        for (SchemaCheck eachCheck : solrSchemaChecks) {
            if (eachCheck.equals(schemaCheck) || eachCheck.contains(schemaCheck)) {
                return true;
            }
        }
        return false;
    }
}
