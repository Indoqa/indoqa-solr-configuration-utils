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
package com.indoqa.solr.utils.validation.results;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Optional;

public class SchemaVersionValidationResult extends AbstractValidationResult {

    private DifferentValue differentValue;

    public SchemaVersionValidationResult(Float schemaVersion, Float responseSchemaVersion) {
        if (Float.compare(schemaVersion, responseSchemaVersion) != 0) {
            differentValue = DifferentValue.of(schemaVersion, responseSchemaVersion);
        }
    }

    @Override
    public Optional<List<? extends AbstractValidationResult>> getModified() {
        if (this.isEmpty()) {
            return super.getModified();
        }
        return Optional.of(singletonList(this));
    }

    @Override
    public boolean isEmpty() {
        return this.differentValue == null;
    }

    @Override
    public String getErrorMessage(int levelOfIndentation) {
        StringBuilder result = new StringBuilder();

        result.append("Schema version differ. ");
        result.append(differentValue.toString());
        result.append('\n');

        return result.toString();
    }

    public DifferentValue getDifferentValue() {
        return differentValue;
    }
}
