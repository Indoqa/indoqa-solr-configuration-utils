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

import static com.indoqa.solr.utils.validation.SchemaCheck.COMPLETE;
import static com.indoqa.solr.utils.validation.SchemaCheckConfiguration.of;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;

import com.indoqa.solr.utils.InitializationFailedException;
import com.indoqa.solr.utils.maintenance.handlers.*;
import com.indoqa.solr.utils.maintenance.handlers.SchemaOperations;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.classpath.ClasspathSolrSchemaChecker;
import com.indoqa.solr.utils.validation.classpath.ClasspathSolrSchemaCheckerBuilder;
import com.indoqa.solr.utils.validation.results.SchemaValidationResult;
import org.apache.solr.client.solrj.SolrClient;

public class ClasspathSolrCoreMaintainer {

    private final SolrCoreMaintainerConfiguration[] configurations;
    private final List<AbstractFieldsHandler> handlers;
    private ClasspathSolrSchemaChecker schemaChecker;

    public ClasspathSolrCoreMaintainer(SolrCoreMaintainerConfiguration... configurations) {
        this.configurations = configurations;
        this.handlers = Arrays.asList(new AddFieldsHandler(), new RemoveFieldsHandler(), new ModifyFieldsHandler());
        this.schemaChecker = new ClasspathSolrSchemaCheckerBuilder().onlyLogErrors().build();
    }

    @PostConstruct
    public void initialize() throws InitializationFailedException {
        for (SolrCoreMaintainerConfiguration eachConfig : this.configurations) {
            SolrSchema solrSchema = eachConfig.getSolrSchema();
            try {
                SchemaValidationResult schemaValidationResult = this.schemaChecker.validateSolrSchema(of(solrSchema, COMPLETE),
                    eachConfig.getSolrClient());
                this.updateCore(eachConfig.getSolrClient(), eachConfig.getSchemaOperations(), solrSchema, schemaValidationResult);
            } catch (SolrSchemaException e) {
                throw new InitializationFailedException("Could not initialize schema checker for " + solrSchema.getCollectionName() + ".",
                    e);
            }
        }
    }

    public void setSchemaChecker(ClasspathSolrSchemaChecker schemaChecker) {
        this.schemaChecker = schemaChecker;
    }

    private void updateCore(SolrClient solrClient, SchemaOperations schemaOperations, SolrSchema solrSchema, SchemaValidationResult validationResult)
        throws InitializationFailedException {
        if (validationResult.isEmpty()) {
            return;
        }

        for (AbstractFieldsHandler eachHandler : this.handlers) {
            eachHandler.checkEarly(schemaOperations, validationResult);
        }

        for (AbstractFieldsHandler eachHandler : this.handlers) {
            eachHandler.handle(schemaOperations, solrClient, solrSchema, validationResult);
        }
    }
}
