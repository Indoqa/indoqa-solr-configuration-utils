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
package com.indoqa.solr.utils.validation;

import static org.joox.JOOX.$;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import com.indoqa.lang.io.ResourceLoader;
import org.apache.solr.client.solrj.request.schema.AnalyzerDefinition;
import org.apache.solr.client.solrj.request.schema.FieldTypeDefinition;
import org.joox.JOOX;
import org.joox.Match;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SolrSchema {

    private final String collectionName;
    private final String schemaLocation;
    private final Document document;

    public SolrSchema(String collectionName, String schemaLocation) throws SolrSchemaException {
        this.collectionName = collectionName;
        this.schemaLocation = schemaLocation;
        try {
            this.document = $(getSchema(schemaLocation)).document();
        } catch (SAXException | IOException e) {
            throw new SolrSchemaException("Could not load schema for location '" + schemaLocation + "'.", e);
        }
    }
    private InputStream getSchema(String schemaLocation) throws IOException {
        URL url = ResourceLoader.getUrl(schemaLocation);
        if (url == null) {
            throw new IllegalArgumentException("Could not find " + schemaLocation + " in classpath.");
        }
        return url.openStream();
    }

    public String getSchemaLocation() {
        return this.schemaLocation;
    }

    public String getCollectionName() {
        return this.collectionName;
    }

    public String getUniqueKey() {
        return $(document).find("uniqueKey").cdata();
    }

    public String getDefaultSearchField() {
        return $(document).attr("defaultSearchField");
    }

    public String getName() {
        return $(document).attr("name");
    }

    public Float getVersion() {
        String version = $(document).attr("version");
        if (version == null) {
            return null;
        }
        return Float.valueOf(version);
    }

    public List<Map<String, Object>> getDynamicFields() {
        return this.getFieldsFor("dynamicField");
    }

    public List<Map<String, Object>> getFields() {
        return this.getFieldsFor("field");
    }

    public List<FieldTypeDefinition> getFieldTypes() {
        List<FieldTypeDefinition> result = new ArrayList<>();
        Match fieldTypes = $(document).find(JOOX.or(JOOX.tag("fieldType"), JOOX.tag("fieldtype")));
        for (Element eachField : fieldTypes) {
            FieldTypeDefinition fieldTypeDefinition = new FieldTypeDefinition();
            fieldTypeDefinition.setAttributes(extractFieldAttributes(eachField));

            Match analyzer = $(eachField).find("analyzer");
            fieldTypeDefinition.setQueryAnalyzer(extractAnalyzer(analyzer, "query"));
            fieldTypeDefinition.setIndexAnalyzer(extractAnalyzer(analyzer, "index"));
            fieldTypeDefinition.setMultiTermAnalyzer(extractAnalyzer(analyzer, "multiterm"));
            fieldTypeDefinition.setAnalyzer(extractAnalyzer(analyzer, ""));

            fieldTypeDefinition.setSimilarity(extractSimilarity($(eachField).find("similarity")));
            result.add(fieldTypeDefinition);
        }
        return result;
    }

    public List<Map<String, Object>> getCopyFields() {
        return this.getFieldsFor("copyField", DataTypeHandling.INTEGER, DataTypeHandling.LONG);
    }

    private Map<String, Object> extractSimilarity(Match similarity) {
        if (similarity.isEmpty()) {
            return null;
        }
        Element element = similarity.get(0);
        Map<String, Object> result = extractFieldAttributes(element);
        for (Element eachElement : $(element).children().get()) {
            result.put(eachElement.getAttribute("name"), eachElement.getTextContent());
        }
        return result;
    }

    private AnalyzerDefinition extractAnalyzer(Match analyzers, String type) {
        for (Element analyzer : analyzers) {
            if (analyzer.getAttribute("type").equals(type)) {
                return extractAnalyzer(analyzer);
            }
        }
        return null;
    }

    private AnalyzerDefinition extractAnalyzer(Element analyzer) {
        AnalyzerDefinition result = new AnalyzerDefinition();

        Map<String, Object> attributes = extractFieldAttributes(analyzer);
        attributes.remove("type");
        result.setAttributes(attributes);
        result.setCharFilters(extractFilters(analyzer, "charFilter"));
        result.setFilters(extractFilters(analyzer, "filter"));
        result.setTokenizer(extractTokenizer(analyzer));

        return result;
    }

    private Map<String, Object> extractTokenizer(Element analyzer) {
        Match tokenizer = $(analyzer).find("tokenizer");
        if (tokenizer.isEmpty()) {
            return null;
        }
        return extractFieldAttributes(tokenizer.get(0));
    }

    private List<Map<String, Object>> extractFilters(Element analyzer, String filterType) {
        Match elements = $(analyzer).find(filterType);
        if (elements.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> result = new ArrayList<>(elements.size());
        for (Element element : elements) {
            result.add(extractFieldAttributes(element));
        }
        return result;
    }

    private Map<String, Object> extractFieldAttributes(Element eachField, DataTypeHandling...dataTypeHandlings) {
        Map<String, Object> fieldAttributes = new HashMap<>();

        NamedNodeMap attributes = eachField.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);
            fieldAttributes.put(item.getNodeName(), getValue(item.getNodeValue(), dataTypeHandlings));
        }
        return fieldAttributes;
    }

    private Object getValue(String nodeValue, DataTypeHandling...dataTypesHandling) {
        if (Boolean.TRUE.toString().equalsIgnoreCase(nodeValue)) {
            return true;
        }
        if (Boolean.FALSE.toString().equalsIgnoreCase(nodeValue)) {
            return false;
        }

        List<DataTypeHandling> handling = Arrays.asList(dataTypesHandling);
        if (handling.contains(DataTypeHandling.INTEGER)) {
            try {
                return Integer.parseInt(nodeValue);
            } catch (NumberFormatException e) {
                //ignore value might be no int
            }
        }

        if (handling.contains(DataTypeHandling.LONG)) {
            try{
                return Long.parseLong(nodeValue);
            }catch(NumberFormatException ef) {
                //ignore value might be no long
            }
        }

        return nodeValue;
    }

    private List<Map<String, Object>> getFieldsFor(String fieldDescription, DataTypeHandling...dataTypeHandlings) {
        List<Map<String, Object>> result = new ArrayList<>();

        Match fields = $(document).find(fieldDescription);
        for (Element eachField : fields) {
            Map<String, Object> resultField = extractFieldAttributes(eachField, dataTypeHandlings);
            result.add(resultField);
        }

        return result;
    }

    private enum DataTypeHandling {
        INTEGER, LONG
    }
}
