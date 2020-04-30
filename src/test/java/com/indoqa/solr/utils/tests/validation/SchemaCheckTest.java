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

import static org.junit.Assert.*;

import com.indoqa.solr.utils.validation.SchemaCheck;
import org.junit.Test;

public class SchemaCheckTest {

    @Test
    public void testContains() {
        assertTrue("COMPLETE should contain ALL_FIELDS", SchemaCheck.COMPLETE.contains(SchemaCheck.ALL_FIELDS));
        assertTrue("COMPLETE should contain BASIC", SchemaCheck.COMPLETE.contains(SchemaCheck.BASIC));
        assertTrue("COMPLETE should contain SETTINGS", SchemaCheck.COMPLETE.contains(SchemaCheck.SETTINGS));

        assertTrue("COMPLETE should contain NAME", SchemaCheck.COMPLETE.contains(SchemaCheck.NAME));
        assertTrue("COMPLETE should contain DYNAMIC_FIELDS", SchemaCheck.COMPLETE.contains(SchemaCheck.DYNAMIC_FIELDS));

        assertFalse("BASIC should not contain DYNAMIC_FIELDS", SchemaCheck.BASIC.contains(SchemaCheck.DYNAMIC_FIELDS));
        assertFalse("BASIC should not contain SETTINGS", SchemaCheck.BASIC.contains(SchemaCheck.SETTINGS));
        assertFalse("BASIC should not contain UNIQUE_KEY", SchemaCheck.BASIC.contains(SchemaCheck.UNIQUE_KEY));
    }

}
