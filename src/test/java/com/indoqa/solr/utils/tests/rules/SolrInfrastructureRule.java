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
package com.indoqa.solr.utils.tests.rules;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.JettyConfig;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.junit.rules.ExternalResource;

public class SolrInfrastructureRule extends ExternalResource {

    private JettySolrRunner jetty;

    private SolrClient onlyAddSolrClient;
    private SolrClient onlyRemoveSolrClient;
    private SolrClient onlyModifySolrClient;

    private SolrClient addModifySolrClient;
    private SolrClient addRemoveSolrClient;
    private SolrClient modifyRemoveSolrClient;

    private SolrClient addModifyRemoveSolrClient;

    private SolrClient wrongOperationSolrClient;

    private SolrClient initialSolrClient;
    private SolrClient changedSolrClient;
    private SolrClient fullSolrClient;

    public SolrClient getOnlyAddSolrClient() {
        return onlyAddSolrClient;
    }

    public SolrClient getOnlyRemoveSolrClient() {
        return onlyRemoveSolrClient;
    }

    public SolrClient getOnlyModifySolrClient() {
        return onlyModifySolrClient;
    }

    public SolrClient getAddModifySolrClient() {
        return addModifySolrClient;
    }

    public SolrClient getAddRemoveSolrClient() {
        return addRemoveSolrClient;
    }

    public SolrClient getModifyRemoveSolrClient() {
        return modifyRemoveSolrClient;
    }

    public SolrClient getAddModifyRemoveSolrClient() {
        return addModifyRemoveSolrClient;
    }

    public SolrClient getWrongOperationSolrClient() {
        return wrongOperationSolrClient;
    }

    public SolrClient getInitialSolrClient() {
        return this.initialSolrClient;
    }

    public SolrClient getChangedSolrClient() {
        return this.changedSolrClient;
    }

    public SolrClient getFullSolrClient() {
        return fullSolrClient;
    }

    @Override
    protected void after() {
        try {
            this.jetty.stop();
        } catch (Exception e) {
        }

        clearSolrDirectory();
    }

    private void clearSolrDirectory() {
        try {
            FileUtils.deleteDirectory(new File("target/solr"));
        } catch (IOException e) {
        }
    }

    @Override
    protected void before() throws Exception {
        clearSolrDirectory();

        FileUtils.copyDirectory(new File("src/test/resources/solr"), new File("target/solr"));
        String solrHome = "target/solr";
        JettyConfig jettyConfig = new JettyConfig.Builder().setContext("/solr").stopAtShutdown(true).build();
        jetty = new JettySolrRunner(solrHome, jettyConfig);

        jetty.start();

        String baseSolrUrl = "http://localhost:" + this.jetty.getLocalPort() + "/solr";
        this.onlyAddSolrClient = buildSolrClient(baseSolrUrl, "/only_add");
        this.onlyRemoveSolrClient = buildSolrClient(baseSolrUrl, "/only_remove");
        this.onlyModifySolrClient = buildSolrClient(baseSolrUrl, "/only_modify");

        this.addModifySolrClient = buildSolrClient(baseSolrUrl, "/add_modify");
        this.addRemoveSolrClient = buildSolrClient(baseSolrUrl, "/add_remove");
        this.modifyRemoveSolrClient = buildSolrClient(baseSolrUrl, "/modify_remove");

        this.addModifyRemoveSolrClient = buildSolrClient(baseSolrUrl, "/add_modify_remove");

        this.initialSolrClient = buildSolrClient(baseSolrUrl, "/initial");
        this.changedSolrClient = buildSolrClient(baseSolrUrl, "/changed");
        this.fullSolrClient = buildSolrClient(baseSolrUrl, "/full");

        this.wrongOperationSolrClient = buildSolrClient(baseSolrUrl, "/wrong_operation");
    }

    private HttpSolrClient buildSolrClient(String baseSolrUrl, String schema) {
        return new HttpSolrClient.Builder(baseSolrUrl + schema).build();
    }
}
