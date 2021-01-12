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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.apache.solr.client.solrj.request.schema.SchemaRequest;

public class SchemaUpdates {

    List<SchemaRequest.Update> fieldTypesUpdates = new ArrayList<>();
    List<SchemaRequest.Update> fieldUpdates = new ArrayList<>();
    List<SchemaRequest.Update> dynamicUpdates = new ArrayList<>();
    List<SchemaRequest.Update> copyFieldUpdates = new ArrayList<>();

    public void addAllFieldTypeAdd(List<SchemaRequest.AddFieldType> updates) {
        this.fieldTypesUpdates.addAll(updates);
    }

    public void addAllFieldUpdatesAdd(List<SchemaRequest.AddField> updates) {
        this.fieldUpdates.addAll(updates);
    }

    public void addAllDynamicUpdatesAdd(List<SchemaRequest.AddDynamicField> updates) {
        this.dynamicUpdates.addAll(updates);
    }

    public void addAllCopyFieldUpdatesAdd(List<SchemaRequest.AddCopyField> updates) {
        this.copyFieldUpdates.addAll(updates);
    }

    public void addAllFieldTypeUpdatesRemove(List<SchemaRequest.DeleteFieldType> updates) {
        this.fieldTypesUpdates.addAll(updates);
    }

    public void addAllFieldUpdatesRemove(List<SchemaRequest.DeleteField> updates) {
        this.fieldUpdates.addAll(updates);
    }

    public void addAllDynamicUpdatesRemove(List<SchemaRequest.DeleteDynamicField> updates) {
        this.dynamicUpdates.addAll(updates);
    }

    public void addAllCopyFieldUpdatesRemove(List<SchemaRequest.DeleteCopyField> updates) {
        this.copyFieldUpdates.addAll(updates);
    }

    public void addAllFieldTypeModify(List<SchemaRequest.ReplaceFieldType> updates) {
        this.fieldTypesUpdates.addAll(updates);
    }

    public void addAllFieldUpdatesModify(List<SchemaRequest.ReplaceField> updates) {
        this.fieldUpdates.addAll(updates);
    }

    public void addAllDynamicUpdatesModify(List<SchemaRequest.ReplaceDynamicField> updates) {
        this.dynamicUpdates.addAll(updates);
    }

    public void addAllCopyFieldUpdatesModify(SchemaRequest.DeleteCopyField deleteCopyField, SchemaRequest.AddCopyField addCopyField) {
        this.copyFieldUpdates.add(deleteCopyField);
        this.copyFieldUpdates.add(addCopyField);
    }


    public List<SchemaRequest.Update> getAllUpdatesAdd() {
        return Stream // order matters since fields can depend on each other
            .of(this.fieldTypesUpdates, this.fieldUpdates, this.dynamicUpdates, this.copyFieldUpdates)
            .flatMap(Collection::stream)
            .filter(this::filterAdds)
            .collect(toList());
    }

    public List<SchemaRequest.Update> getAllUpdatesRemove() {
        return Stream // order matters since fields can depend on each other
            .of(this.copyFieldUpdates, this.dynamicUpdates, this.fieldUpdates, this.fieldTypesUpdates)
            .flatMap(Collection::stream)
            .filter(this::filterDeletes)
            .collect(toList());
    }

    public List<SchemaRequest.Update> getAllUpdatesModify() {
        return Stream // order matters since fields can depend on each other
            .of(this.fieldTypesUpdates, this.fieldUpdates, this.dynamicUpdates, this.copyFieldUpdates)
            .flatMap(Collection::stream)
            .filter(this::filterModifications)
            .collect(toList());
    }

    private boolean filterModifications(SchemaRequest.Update update) {
        if (update instanceof SchemaRequest.ReplaceFieldType) {
            return true;
        }
        if (update instanceof SchemaRequest.ReplaceField) {
            return true;
        }
        if (update instanceof SchemaRequest.ReplaceDynamicField) {
            return true;
        }
        if (update instanceof SchemaRequest.DeleteCopyField) {
            return true;
        }
        if (update instanceof SchemaRequest.AddCopyField) {
            return true;
        }
        return false;
    }

    private boolean filterDeletes(SchemaRequest.Update update) {
        if (update instanceof SchemaRequest.DeleteCopyField) {
            return true;
        }
        if (update instanceof SchemaRequest.DeleteDynamicField) {
            return true;
        }
        if (update instanceof SchemaRequest.DeleteField) {
            return true;
        }
        if (update instanceof SchemaRequest.DeleteFieldType) {
            return true;
        }
        return false;
    }

    private boolean filterAdds(SchemaRequest.Update update) {
        if (update instanceof SchemaRequest.AddFieldType) {
            return true;
        }
        if (update instanceof SchemaRequest.AddField) {
            return true;
        }
        if (update instanceof SchemaRequest.AddDynamicField) {
            return true;
        }
        if (update instanceof SchemaRequest.AddCopyField) {
            return true;
        }
        return false;
    }
}
