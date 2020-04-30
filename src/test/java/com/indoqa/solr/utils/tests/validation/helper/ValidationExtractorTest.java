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
package com.indoqa.solr.utils.tests.validation.helper;

import com.indoqa.solr.utils.validation.results.*;
import org.junit.Assert;
import org.junit.Test;

public class ValidationExtractorTest {

    @Test
    public void testFieldAttributesExtractValidations() {
        FieldAttributesValidationResult fieldValidation = new FieldAttributesValidationResult();
        AttributesValidationResult attributesValidation = new AttributesValidationResult();
        attributesValidation.addAttributeOnlyInSchema("a", "1");
        attributesValidation.addAttributeOnlyInSchema("b", "2");
        attributesValidation.addAttributeStillInSolr("c", "3");
        attributesValidation.addAttributeStillInSolr("d", "4");
        attributesValidation.addDifferentAttribute("e", "5", "7");
        attributesValidation.addDifferentAttribute("f", "6", "8");
        fieldValidation.addAttributeValidation(attributesValidation);

        SchemaValidationResult validationResult = new SchemaValidationResult();
        validationResult.addValidationResult(fieldValidation);
        ExtractedValidations actualExtractedValidations = ValidationExtractor.extractValidations(Operation.ADD, validationResult);
        Assert.assertNotNull(actualExtractedValidations.getValidations());
        Assert.assertEquals(1, actualExtractedValidations.getValidations().size());

        actualExtractedValidations = ValidationExtractor.extractValidations(Operation.MODIFY, validationResult);
        Assert.assertNotNull(actualExtractedValidations.getValidations());
        Assert.assertEquals(1, actualExtractedValidations.getValidations().size());

        actualExtractedValidations = ValidationExtractor.extractValidations(Operation.REMOVE, validationResult);
        Assert.assertNotNull(actualExtractedValidations.getValidations());
        Assert.assertEquals(1, actualExtractedValidations.getValidations().size());
    }

    @Test
    public void testFieldTypesExtractValidations() {
        FieldTypesValidationResult fieldTypesValidation = new FieldTypesValidationResult();
        FieldTypeValidationResult fieldTypeValidation = new FieldTypeValidationResult();

        FilterValidationResult filterValidation = new FilterValidationResult();
        filterValidation.addAttributeOnlyInSchema("filter1", "value1");
        filterValidation.addDifferentAttribute("filter2", "value2", "value2a");
        filterValidation.addAttributeOnlyInSchema("filter3", "value3");

        AnalyzerValidationResult analyzerValidation = new AnalyzerValidationResult();
        analyzerValidation.addFiltersOnlyInSchema(filterValidation);
        analyzerValidation.addFiltersStillInSolr(filterValidation);
        analyzerValidation.addFiltersModified(filterValidation);

        fieldTypeValidation.addAnalyzerModified(analyzerValidation);
        fieldTypeValidation.addAnalyzerOnlyInSchema(analyzerValidation);
        fieldTypeValidation.addAnalyzerStillInSolr(analyzerValidation);
        fieldTypesValidation.addFieldsModified(fieldTypeValidation);
        fieldTypesValidation.addFieldsOnlyInSchema(fieldTypeValidation);
        fieldTypesValidation.addFieldStillInSolr(fieldTypeValidation);
        AttributesValidationResult attributesValidation = new AttributesValidationResult();
        attributesValidation.addAttributeOnlyInSchema("a", "1");
        attributesValidation.addAttributeOnlyInSchema("b", "2");
        attributesValidation.addAttributeStillInSolr("c", "3");
        attributesValidation.addAttributeStillInSolr("d", "4");
        attributesValidation.addDifferentAttribute("e", "5", "7");
        attributesValidation.addDifferentAttribute("f", "6", "8");
        fieldTypeValidation.addAttributeValidation(attributesValidation);

        SchemaValidationResult validationResult = new SchemaValidationResult();
        validationResult.addValidationResult(fieldTypesValidation);
        ExtractedValidations actualExtractedValidations = ValidationExtractor.extractValidations(Operation.ADD, validationResult);
        Assert.assertNotNull(actualExtractedValidations.getValidations());
        Assert.assertEquals(1, actualExtractedValidations.getValidations().size());

        actualExtractedValidations = ValidationExtractor.extractValidations(Operation.MODIFY, validationResult);
        Assert.assertNotNull(actualExtractedValidations.getValidations());
        Assert.assertEquals(1, actualExtractedValidations.getValidations().size());

        actualExtractedValidations = ValidationExtractor.extractValidations(Operation.REMOVE, validationResult);
        Assert.assertNotNull(actualExtractedValidations.getValidations());
        Assert.assertEquals(1, actualExtractedValidations.getValidations().size());
    }

    @Test
    public void testFieldAttributesHasValidationsAdd() {
        FieldAttributesValidationResult fieldValidation = new FieldAttributesValidationResult();
        AttributesValidationResult attributesValidation = new AttributesValidationResult();
        attributesValidation.addAttributeOnlyInSchema("a", "c");
        attributesValidation.addAttributeOnlyInSchema("b", "d");
        fieldValidation.addAttributeValidation(attributesValidation);

        SchemaValidationResult validationResult = new SchemaValidationResult();
        validationResult.addValidationResult(fieldValidation);
        Assert.assertTrue("Should have ADD validations", ValidationExtractor.hasValidations(Operation.ADD, validationResult));
        Assert.assertFalse("Should not have MODIFY validations", ValidationExtractor.hasValidations(Operation.MODIFY, validationResult));
        Assert.assertFalse("Should not have REMOVE validations", ValidationExtractor.hasValidations(Operation.REMOVE, validationResult));
    }
    @Test
    public void testFieldAttributesHasValidationsModify() {
        FieldAttributesValidationResult fieldValidation = new FieldAttributesValidationResult();
        AttributesValidationResult attributesValidation = new AttributesValidationResult();
        attributesValidation.addDifferentAttribute("a", "c", "1");
        attributesValidation.addDifferentAttribute("b", "d", "2");
        fieldValidation.addAttributeValidation(attributesValidation);

        SchemaValidationResult validationResult = new SchemaValidationResult();
        validationResult.addValidationResult(fieldValidation);
        Assert.assertFalse("Should have ADD validations", ValidationExtractor.hasValidations(Operation.ADD, validationResult));
        Assert.assertTrue("Should not have MODIFY validations", ValidationExtractor.hasValidations(Operation.MODIFY, validationResult));
        Assert.assertFalse("Should not have REMOVE validations", ValidationExtractor.hasValidations(Operation.REMOVE, validationResult));
    }
    @Test
    public void testFieldAttributesHasValidationsRemove() {
        FieldAttributesValidationResult fieldValidation = new FieldAttributesValidationResult();
        AttributesValidationResult attributesValidation = new AttributesValidationResult();
        attributesValidation.addAttributeStillInSolr("a", "c");
        attributesValidation.addAttributeStillInSolr("b", "d");
        fieldValidation.addAttributeValidation(attributesValidation);

        SchemaValidationResult validationResult = new SchemaValidationResult();
        validationResult.addValidationResult(fieldValidation);
        Assert.assertFalse("Should have ADD validations", ValidationExtractor.hasValidations(Operation.ADD, validationResult));
        Assert.assertFalse("Should not have MODIFY validations", ValidationExtractor.hasValidations(Operation.MODIFY, validationResult));
        Assert.assertTrue("Should not have REMOVE validations", ValidationExtractor.hasValidations(Operation.REMOVE, validationResult));
    }
}