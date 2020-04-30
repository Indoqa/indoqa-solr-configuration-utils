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

import static com.indoqa.solr.utils.tests.validation.helper.Operation.*;

import java.util.Arrays;

import com.indoqa.solr.utils.tests.validation.helper.Operation;

public enum SchemaOperations {

    ALLOW_ADDING_FIELDS(ADD), ALLOW_MODIFYING_FIELDS(MODIFY), ALLOW_REMOVING_FIELDS(REMOVE),
    ALLOW_ADDING_MODIFYING_FIELDS(ADD, MODIFY),
    ALLOW_ADDING_REMOVING_FIELDS(ADD, REMOVE),
    ALLOW_MODIFYING_REMOVING_FIELDS(MODIFY, REMOVE),
    ALLOW_ADDING_MODIFYING_REMOVING_FIELDS(ADD, MODIFY, REMOVE);

    private Operation[] operations;

    SchemaOperations(Operation ... operations) {
        Arrays.sort(operations);
        this.operations = operations;
    }

    public boolean allowsOperation(Operation operation) {
        return Arrays.binarySearch(this.operations, operation) >= 0;
    }
}
