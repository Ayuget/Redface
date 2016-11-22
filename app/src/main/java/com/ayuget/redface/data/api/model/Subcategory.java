/*
 * Copyright 2015 Ayuget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ayuget.redface.data.api.model;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Subcategory implements Parcelable, SelectableItem {
    /**
     * Subcategory name (e.g "Carte mère")
     */
    @Override
    public abstract String name();

    /**
     * Subcategory URL name ("slug"), e.g "CarteMere"
     */
    public abstract String slug();

    /**
     * Subcategory id, e.g "331"
     */
    public abstract int id();

    public static Subcategory create(String name, String slug) {
        return new AutoValue_Subcategory(name, slug, -1);
    }

    public static Subcategory create(String name, int id) {
        return new AutoValue_Subcategory(name, "", id);
    }

    public static Subcategory create(String name, String slug, int id) {
        return new AutoValue_Subcategory(name, slug, id);
    }
}
