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
package com.indoqa.solr.utils.validation.checks;

import static java.text.MessageFormat.format;

import java.io.IOException;

import com.indoqa.solr.utils.validation.SchemaCheck;
import com.indoqa.solr.utils.validation.SchemaCheckConfiguration;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.results.AbstractValidationResult;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.schema.AbstractSchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;

public interface SchemaValidation<V extends AbstractValidationResult> {

    default boolean shouldBeChecked(SchemaCheckConfiguration configuration) {
        return configuration.needsCheck(getSchemaCheck());
    }

    default <T extends SolrResponse> T process(String collectionName, AbstractSchemaRequest<T> request, SolrClient solrClient)
        throws SolrSchemaException {
        try {
            return request.process(solrClient);
        } catch (SolrServerException | IOException e) {
            String message = format("Could not check validity of Solr schema for core/collection {0}. ", collectionName);
            throw new SolrSchemaException(message, e);
        }
    }

    SchemaCheck getSchemaCheck();

    V validate(SolrSchema solrSchema, SchemaResponse schemaResponse, SolrClient solrClient) throws SolrSchemaException;

}
