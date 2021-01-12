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

import java.util.List;
import java.util.Map;

import com.indoqa.solr.utils.validation.SchemaCheck;
import com.indoqa.solr.utils.validation.SolrSchema;
import com.indoqa.solr.utils.validation.SolrSchemaException;
import com.indoqa.solr.utils.validation.results.FieldsValidationResult;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;

public class SchemaFieldsValidation extends AbstractSchemaAbstractFieldsValidation<FieldsValidationResult> {

    @Override
    public SchemaCheck getSchemaCheck() {
        return SchemaCheck.FIELDS;
    }

    @Override
    public FieldsValidationResult validate(SolrSchema solrSchema, SchemaResponse schemaResponse, SolrClient solrClient)
        throws SolrSchemaException {
        List<Map<String, Object>> schemaResponseFields;

        if (schemaResponse == null) {
            schemaResponseFields = process(solrSchema.getCollectionName(), new SchemaRequest.Fields(), solrClient).getFields();
        } else {
            schemaResponseFields = schemaResponse.getSchemaRepresentation().getFields();
        }

        FieldsValidationResult result = new FieldsValidationResult();
        extractSchemaValidations(result, solrSchema.getFields(), schemaResponseFields);
        return result;
    }
}
