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

import java.util.ArrayList;
import java.util.List;

import com.indoqa.solr.utils.validation.SchemaCheckConfiguration;
import com.indoqa.solr.utils.validation.SchemaErrorHandling;
import org.apache.solr.client.solrj.SolrClient;

public class ClasspathSolrSchemaCheckerBuilder {

    private List<SolrClientCheckConfiguration> configurations;
    private SchemaErrorHandling schemaErrorHandling = SchemaErrorHandling.EXCEPTION_ON_FIRST_ERROR;

    public ClasspathSolrSchemaCheckerBuilder() {
        this.configurations = new ArrayList<>();
    }

    public ClasspathSolrSchemaCheckerBuilder add(SchemaCheckConfiguration configuration, SolrClient solrClient) {
        SolrClientCheckConfiguration solrClientValidationConfiguration = new SolrClientCheckConfiguration();
        solrClientValidationConfiguration.setSolrClient(solrClient);
        solrClientValidationConfiguration.setValidationConfiguration(configuration);
        this.configurations.add(solrClientValidationConfiguration);
        return this;
    }

    public ClasspathSolrSchemaCheckerBuilder onlyLogErrors() {
        this.schemaErrorHandling = SchemaErrorHandling.LOGGING_ONLY;
        return this;
    }

    public ClasspathSolrSchemaCheckerBuilder afterAllCoresThrowException() {
        this.schemaErrorHandling = SchemaErrorHandling.EXCEPTION_AFTER_ALL_CORES;
        return this;
    }

    public ClasspathSolrSchemaCheckerBuilder afterFirstErroneousCoreThrowException() {
        this.schemaErrorHandling = SchemaErrorHandling.EXCEPTION_AFTER_FIRST_ERRONEOUS_CORE;
        return this;
    }

    public ClasspathSolrSchemaCheckerBuilder onFirstErrorThrowException() {
        this.schemaErrorHandling = SchemaErrorHandling.EXCEPTION_ON_FIRST_ERROR;
        return this;
    }

    public ClasspathSolrSchemaChecker build() {
        return new ClasspathSolrSchemaChecker(this.schemaErrorHandling, this.configurations.toArray(new SolrClientCheckConfiguration[this.configurations.size()]));
    }
}
