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
package com.indoqa.solr.utils.maintenance.handlers;

import static com.indoqa.solr.utils.SolrConstants.*;

import java.io.IOException;
import java.util.List;

import com.indoqa.solr.utils.InitializationFailedException;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.tests.validation.helper.ExtractedValidations;
import com.indoqa.solr.utils.tests.validation.helper.Operation;
import com.indoqa.solr.utils.tests.validation.helper.ValidationExtractor;
import com.indoqa.solr.utils.validation.results.SchemaValidationResult;
import com.indoqa.solr.utils.validation.results.ValidationResult;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.apache.solr.common.params.ModifiableSolrParams;

public abstract class AbstractFieldsHandler {

    private static final int DEFAULT_TIMEOUT = 10;

    private Operation operation;

    protected AbstractFieldsHandler(Operation operation) {
        this.operation = operation;
    }

    public void handle(SchemaOperations schemaOperations, SolrClient solrClient, SolrSchema solrSchema,
        SchemaValidationResult validationResult) throws InitializationFailedException {

        ExtractedValidations extractedValidations = ValidationExtractor.extractValidations(this.operation, validationResult);
        if (!extractedValidations.hasValidations()) {
            return;
        }
        if (!this.supports(schemaOperations)) {
            String fieldErrors = buildErrorMessage(extractedValidations);
            throw new InitializationFailedException(
                "Could not perform schema updates: '" + schemaOperations + "': Operation to '" + this.operation + "' fields not permitted.\n" + fieldErrors);
        }

        handle(solrClient, solrSchema, extractedValidations);
    }

    public void checkEarly(SchemaOperations schemaOperations, SchemaValidationResult validationResult) throws InitializationFailedException {
        if(!ValidationExtractor.hasValidations(this.operation, validationResult)) {
            return;
        }
        if (!this.supports(schemaOperations)) {
            ExtractedValidations extractedValidations = ValidationExtractor.extractValidations(this.operation, validationResult);
            String fieldErrors = buildErrorMessage(extractedValidations);
            throw new InitializationFailedException(
                "Could not perform schema updates: '" + schemaOperations + "': Operation to '" + this.operation + "' fields not permitted.\n" + fieldErrors);
        }
    }

    private String buildErrorMessage(ExtractedValidations extractedValidations) {
        List<ValidationResult> validations = extractedValidations.getValidations();
        StringBuilder result = new StringBuilder();
        for (ValidationResult validation : validations) {
            result.append(validation.getErrorMessage());
        }
        return result.toString();
    }

    protected abstract void handle(SolrClient solrClient, SolrSchema solrSchema, ExtractedValidations extractedValidations)
        throws InitializationFailedException;

    protected boolean supports(SchemaOperations schemaOperations) {
        if (schemaOperations.allowsOperation(this.operation)) {
            return true;
        }
        return false;
    }

    protected void processUpdates(SolrClient solrClient, SolrSchema solrSchema, List<SchemaRequest.Update> updates)
        throws InitializationFailedException {
        SchemaRequest.MultiUpdate multiUpdate = new SchemaRequest.MultiUpdate(updates, createParams());
        try {
            SchemaResponse.UpdateResponse response = process(multiUpdate, solrClient, solrSchema.getCollectionName());
            if (response.getStatus() > 0 || response.getResponse().get(RESPONSE_ERRORS) != null) {
                throw new InitializationFailedException(
                    "Manual intervention needed! Could not update schema: " + response.getResponse().get(RESPONSE_ERRORS));
            }
        } catch (SolrServerException | IOException e) {
            throw new InitializationFailedException("Manual intervention needed! Could not update schema.", e);
        }
    }

    protected ModifiableSolrParams createParams() {
        ModifiableSolrParams solrParams = new ModifiableSolrParams();
        solrParams.set(PARAMETER_UPDATE_TIMEOUT_SECS, DEFAULT_TIMEOUT);
        return solrParams;
    }

    private SchemaResponse.UpdateResponse process(SchemaRequest.Update update, SolrClient solrClient, String collectionName)
        throws IOException, SolrServerException {
        if (solrClient instanceof CloudSolrClient) {
            return update.process(solrClient, collectionName);
        }
        return update.process(solrClient);
    }

}
