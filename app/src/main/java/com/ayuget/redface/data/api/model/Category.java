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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Category implements Parcelable, SelectableItem {
    /**
     * Category identifier in the MD forum
     */
    private final int id;

    /**
     * Category name (e.g. "Hardware")
     */
    private final String name;

    /**
     * Category URL name ("slug"), e.g "HardwarePeripheriques"
     */
    private final String slug;

    private final List<Subcategory> subcategories;

    protected Category(int id, String name, String slug, List<Subcategory> subcategories) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.subcategories = subcategories;
    }

    public static Category create(int id, String name, String slug, List<Subcategory> subcategories) {
        return new Category(id, name, slug, subcategories);
    }

    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public List<Subcategory> getSubcategories() {
        return subcategories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (id != category.id) return false;
        if (!name.equals(category.name)) return false;
        if (!slug.equals(category.slug)) return false;
        if (!subcategories.equals(category.subcategories)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + slug.hashCode();
        result = 31 * result + subcategories.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Category{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", slug='").append(slug).append('\'');
        sb.append(", subcategories=").append(subcategories);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Parcelable boilerplate code
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.slug);
        dest.writeTypedList(this.subcategories);
    }

    private Category(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.slug = in.readString();
        this.subcategories = new ArrayList<>();
        in.readTypedList(subcategories, Subcategory.CREATOR);
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
