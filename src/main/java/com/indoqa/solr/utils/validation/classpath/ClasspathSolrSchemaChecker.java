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
package com.indoqa.solr.utils.validation.classpath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import com.indoqa.solr.utils.validation.SchemaCheckConfiguration;
import com.indoqa.solr.utils.validation.SchemaErrorHandling;
import com.indoqa.solr.utils.validation.SolrSchemaChecker;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.checks.*;
import com.indoqa.solr.utils.validation.results.SchemaValidationResult;
import com.indoqa.solr.utils.validation.results.ValidationResult;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClasspathSolrSchemaChecker implements SolrSchemaChecker<SchemaValidationResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathSolrSchemaChecker.class);

    private SolrClientCheckConfiguration[] solrClientValidationConfigurations;

    private List<SchemaValidation> schemaValidations = new ArrayList<>();
    private SchemaErrorHandling schemaErrorHandling;

    public ClasspathSolrSchemaChecker(SchemaErrorHandling errorHandling,
        SolrClientCheckConfiguration... solrClientValidationConfigurations) {
        this.solrClientValidationConfigurations = solrClientValidationConfigurations;

        this.schemaValidations.add(new SchemaNameValidation());
        this.schemaValidations.add(new SchemaVersionValidation());
        this.schemaValidations.add(new SchemaUniqueKeyValidation());
        this.schemaValidations.add(new SchemaFieldsValidation());
        this.schemaValidations.add(new SchemaDynamicFieldsValidation());
        this.schemaValidations.add(new SchemaCopyFieldsValidation());
        this.schemaValidations.add(new SchemaFieldTypesValidation());

        this.schemaErrorHandling = errorHandling;
    }

    public List<SchemaValidation> getSchemaValidations() {
        return schemaValidations;
    }

    @PostConstruct
    public void initialize() throws SolrSchemaException {
        if (solrClientValidationConfigurations == null || solrClientValidationConfigurations.length == 0) {
            throw new IllegalArgumentException("No solr client validation config(s) defined. Can not check validity of Solr server.");
        }

        SchemaCheckerValidationResult result = new SchemaCheckerValidationResult();
        for (SolrClientCheckConfiguration eachValidation : solrClientValidationConfigurations) {
            ValidationResult validation = this.validateSolrSchema(eachValidation.getValidationConfiguration(),
                eachValidation.getSolrClient());
            result.addValidationResult(validation);
            if (!result.isEmpty()) {
                if (shouldThrowException(SchemaErrorHandling.EXCEPTION_AFTER_FIRST_ERRONEOUS_CORE)) {
                    String errorMessage = result.getErrorMessage(0);
                    LOGGER.error(errorMessage);
                    throw new SolrSchemaException(errorMessage);
                }
            }
        }

        if (!result.isEmpty()) {
            String errorMessage = result.getErrorMessage(0);
            LOGGER.error(errorMessage);
            if (shouldThrowException(SchemaErrorHandling.EXCEPTION_AFTER_ALL_CORES)) {
                throw new SolrSchemaException(errorMessage);
            }
        }
    }

    @Override
    public boolean shouldThrowException(SchemaErrorHandling schemaErrorHandling) {
        return this.schemaErrorHandling.equals(schemaErrorHandling);
    }

    @Override
    public SchemaValidationResult validateSolrSchema(SchemaCheckConfiguration validationConfiguration, SolrClient solrClient)
        throws SolrSchemaException {
        SchemaResponse schemaResponse = null;
        try {
            SchemaRequest schemaRequest = new SchemaRequest();
            schemaResponse = schemaRequest.process(solrClient);
        } catch (SolrServerException | IOException e) {
            throw new SolrSchemaException("Could not check validity of Solr schema. ", e);
        } catch (ClassCastException e) {
            LOGGER.warn("Could not read complete schema.xml for {}", validationConfiguration.getSolrSchema().getCollectionName(), e);
        }

        SchemaValidationResult result = new SchemaValidationResult();
        result.setCollectionName(validationConfiguration.getSolrSchema().getCollectionName());
        for (SchemaValidation eachSchemaValidation : schemaValidations) {
            if (eachSchemaValidation.shouldBeChecked(validationConfiguration)) {
                result.addValidationResult(eachSchemaValidation.validate(validationConfiguration.getSolrSchema(),
                    schemaResponse,
                    solrClient));
            }
            if (!result.isEmpty() && this.shouldThrowException(SchemaErrorHandling.EXCEPTION_ON_FIRST_ERROR)) {
                String errorMessage = result.getErrorMessage(0);
                LOGGER.error(errorMessage);
                throw new SolrSchemaException(errorMessage);
            }
        }
        return result;
    }
}
