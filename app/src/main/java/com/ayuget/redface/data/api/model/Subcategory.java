package com.ayuget.redface.data.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Subcategory implements Parcelable, SelectableItem {
    /**
     * Subcategory name (e.g "Carte mère")
     */
    private final String name;

    /**
     * Subcategory URL name ("slug"), e.g "CarteMere"
     */
    private final String slug;

    private Subcategory(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public static Subcategory create(String name, String slug) {
        return new Subcategory(name, slug);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subcategory that = (Subcategory) o;

        if (!name.equals(that.name)) return false;
        if (!slug.equals(that.slug)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + slug.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Subcategory{");
        sb.append("name='").append(name).append('\'');
        sb.append(", slug='").append(slug).append('\'');
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
        dest.writeString(this.name);
        dest.writeString(this.slug);
    }

    private Subcategory(Parcel in) {
        this.name = in.readString();
        this.slug = in.readString();
    }

    public static final Creator<Subcategory> CREATOR = new Creator<Subcategory>() {
        public Subcategory createFromParcel(Parcel source) {
            return new Subcategory(source);
        }

        public Subcategory[] newArray(int size) {
            return new Subcategory[size];
        }
    };
}
