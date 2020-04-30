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

import java.util.Objects;

public class DifferentValue {

    private Object expected;
    private Object actual;

    public static DifferentValue of(Object expected, Object actual) {
        DifferentValue differentValue = new DifferentValue();

        differentValue.setExpected(expected);
        differentValue.setActual(actual);

        return differentValue;
    }

    public Object getExpected() {
        return expected;
    }

    public void setExpected(Object expected) {
        this.expected = expected;
    }

    public Object getActual() {
        return actual;
    }

    public void setActual(Object actual) {
        this.actual = actual;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DifferentValue that = (DifferentValue) o;
        return Objects.equals(getExpected(), that.getExpected()) && Objects.equals(getActual(), that.getActual());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExpected(), getActual());
    }

    @Override
    public String toString() {
        return "{" + "expected=" + expected + ", actual=" + actual + '}';
    }
}
