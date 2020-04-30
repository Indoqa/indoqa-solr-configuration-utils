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
package com.indoqa.solr.utils.tests.validation;

import static com.indoqa.solr.utils.tests.matchers.CompoundMatcher.*;
import static org.hamcrest.MatcherAssert.assertThat;

import com.indoqa.solr.utils.tests.rules.SolrInfrastructureRule;
import com.indoqa.solr.utils.validation.SchemaCheck;
import com.indoqa.solr.utils.validation.SchemaCheckConfiguration;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.classpath.ClasspathSolrSchemaChecker;
import com.indoqa.solr.utils.validation.classpath.ClasspathSolrSchemaCheckerBuilder;
import com.indoqa.solr.utils.validation.results.*;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class AbstractSolrSchemaCheckerTest {

    @ClassRule
    public static SolrInfrastructureRule solrInfrastructureRule = new SolrInfrastructureRule();

    public static SolrInfrastructureRule getSolrInfrastructure() {
        return solrInfrastructureRule;
    }

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().muteForSuccessfulTests();

    protected static String INITIAL_COLLECTION = "initial";
    protected static String INITIAL_PATH = "src/test/resources/solr/validation/initial/conf/schema.xml";
    protected static SolrSchema CHANGED_SCHEMA;

    protected ClasspathSolrSchemaChecker classpathSolrSchemaChecker;

    @BeforeClass
    public static void setupSchema() {
        try {
            CHANGED_SCHEMA = new SolrSchema(INITIAL_COLLECTION, "src/test/resources/solr/validation/changed/conf/schema.xml");
        } catch (SolrSchemaException e) {
            throw new IllegalStateException("Could not initialize SolrSchema.", e);
        }
    }

    @Before
    public void setup() throws SolrSchemaException {
        ClasspathSolrSchemaCheckerBuilder builder = new ClasspathSolrSchemaCheckerBuilder();
        builder.add(SchemaCheckConfiguration.of(new SolrSchema(INITIAL_COLLECTION, INITIAL_PATH), SchemaCheck.COMPLETE),
            getSolrInfrastructure().getInitialSolrClient());
        builder.onlyLogErrors();
        this.classpathSolrSchemaChecker = builder.build();
    }

    protected SchemaValidationResult validateSchema(SolrClient solrClient, SolrSchema schemaToValidate, SchemaCheck schemaCheck)
        throws SolrSchemaException {
        return this.classpathSolrSchemaChecker.validateSolrSchema(
            SchemaCheckConfiguration.of(schemaToValidate, schemaCheck),
            solrClient);
    }

    public static SolrClient getInitialSolr() {
        return getSolrInfrastructure().getInitialSolrClient();
    }

    public static void printErrorsMessageOnTestError(ValidationResult result, ValidationResult expected) {
        System.out.println("==========RESULT============");
        System.out.println(result.getErrorMessage());
        System.out.println("==========EXPECTED============");
        System.out.println(expected.getErrorMessage());
    }

    public static void assertSameProperties(FieldTypeValidationResult result, FieldTypeValidationResult expected) {
        printErrorsMessageOnTestError(result, expected);
        assertThat(result, isSameFieldTypeValidationAs(expected));
    }

    public static void assertSameProperties(FieldsValidationResult result, FieldsValidationResult expected) {
        printErrorsMessageOnTestError(result, expected);
        assertThat(result, isSameAbstractFieldsValidationResult(expected));
    }

    public static void assertSameProperties(AbstractFieldsValidationResult result, AbstractFieldsValidationResult expected) {
        printErrorsMessageOnTestError(result, expected);
        assertThat(result, isSameAbstractFieldsValidationResult(expected));
    }
}
