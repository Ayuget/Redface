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

package com.ayuget.redface.data.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.api.model.User;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class CategoriesStore {
    private static final String LOG_TAG = CategoriesStore.class.getSimpleName();

    private static final String CATEGORIES_PREFS = "RedfaceCategories";
    private static final String CATEGORY_NAME_PREFIX = "category_";
    private static final String SUBCATEGORY_NAME_PREFIX = "subcategory_";
    private static final String USER_MAPPING_NAME_PREFIX = "user_mapping_";
    private static final char PRIMARY_SEPARATOR = '|';
    private static final char SECONDARY_SEPARATOR = '#';

    /**
     * Shared preferences where the categories will be persisted
     */
    private SharedPreferences categoriesPrefs;

    /**
     * Categories cache, key is the category id
     */
    private Map<Integer, Category> categoriesCache;

    /**
     * User categories cache, key is the user
     */
    private Map<String, List<Category>> userCategoriesCache;

    private Category metaCategory;

    public CategoriesStore(Context context) {
        this.userCategoriesCache = new HashMap<>();
        this.categoriesCache = new HashMap<>();

        this.categoriesPrefs = context.getSharedPreferences(CATEGORIES_PREFS, 0);

        this.metaCategory = Category.create(0, context.getResources().getString(R.string.navdrawer_item_my_topics), "meta", Collections.<Subcategory>emptyList());

        loadFromSharedPreferences();
    }

    public List<Category> getCategories(User user) {
        if (userCategoriesCache.containsKey(user.getUsername())) {
            return userCategoriesCache.get(user.getUsername());
        }
        else {
            return null;
        }
    }

    public Category getMetaCategory() {
        return this.metaCategory;
    }

    public Category getCategoryById(int categoryId) {
        return categoriesCache.get(categoryId);
    }

    public Category getCategoryBySlug(String categorySlug) {
        for (Category category : categoriesCache.values()) {
            if (category.getSlug().equals(categorySlug)) {
                return category;
            }
        }

        return null;
    }

    protected void loadFromSharedPreferences() {
        Map<String, ?> prefsMap = categoriesPrefs.getAll();
        List<String> userKeys = new LinkedList<>();

        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            String entryKey = entry.getKey();
            String entryValue = (String) entry.getValue();

            if (entryKey.startsWith(CATEGORY_NAME_PREFIX)) {
                List<String> tokens = Splitter.on(PRIMARY_SEPARATOR).splitToList(entryValue);

                if (tokens.size() == 4) {
                    int catId = Integer.valueOf(tokens.get(0));
                    String catName = tokens.get(1);
                    String catSlug = tokens.get(2);

                    List<Subcategory> subcategories = new LinkedList<>();

                    for (String subcatSlug : Splitter.on(SECONDARY_SEPARATOR).split(tokens.get(3))) {
                        String subcatKey = SUBCATEGORY_NAME_PREFIX + subcatSlug;

                        if (categoriesPrefs.contains(subcatKey)) {
                            String subcatValue = categoriesPrefs.getString(subcatKey, "");
                            List<String> subcatsTokens = Splitter.on(PRIMARY_SEPARATOR).splitToList(subcatValue);

                            if (subcatsTokens.size() == 2) {
                                subcategories.add(Subcategory.create(subcatsTokens.get(0), subcatsTokens.get(1)));
                            }
                            else {
                                Log.e(LOG_TAG, String.format("Error while deserializing subcategory '%s'", subcatValue));
                            }
                        }
                    }
                    Log.d(LOG_TAG, String.format("Successfully loaded category '%s' from SharedPreferences", catName));

                    categoriesCache.put(catId, Category.create(catId, catName, catSlug, subcategories));
                }
                else {
                    Log.e(LOG_TAG, String.format("Error while deserializing category '%s'", entryValue));
                }
            }
            else if (entryKey.startsWith(USER_MAPPING_NAME_PREFIX)) {
                userKeys.add(entryKey);
            }
        }

        for (String userKey : userKeys) {
            String userMapping = categoriesPrefs.getString(userKey, "");
            String username = userKey.replace(USER_MAPPING_NAME_PREFIX, "");

            List<Category> userCategories = new LinkedList<>();

            for (String category : Splitter.on(PRIMARY_SEPARATOR).split(userMapping)) {
                try {
                    int categoryId = Integer.valueOf(category);

                    if (categoriesCache.containsKey(categoryId)) {
                        userCategories.add(categoriesCache.get(categoryId));
                    } else {
                        Log.e(LOG_TAG, String.format("Error while associating category '%s' to user '%s' : unknown category", category, username));
                    }
                }
                catch (NumberFormatException e) {
                    // Don't crash the app if an invalid category is found
                    Log.e(LOG_TAG, String.format("Error, deserializing category with non-int id : %s", category));
                }
            }

            userCategoriesCache.put(username, userCategories);
        }
    }

    /**
     * Store categories and user/categories relationship in SharedPreferences
     */
    public void storeCategories(User user, List<Category> categories) {
        if (! userCategoriesCache.containsKey(user.getUsername())) {
            userCategoriesCache.put(user.getUsername(), categories);

            for (Category category : categories) {
                categoriesCache.put(category.getId(), category);
            }

            SharedPreferences.Editor editor = categoriesPrefs.edit();

            for (Category category : categories) {
                String categoryKey = CATEGORY_NAME_PREFIX + category.getId();

                if (! categoriesPrefs.contains(categoryKey)) {
                    storeCategory(editor, categoryKey, category);
                }
            }

            String userRelationshipKey = USER_MAPPING_NAME_PREFIX + user.getUsername();
            editor.putString(userRelationshipKey, serializeCategoryIds(categories));

            editor.apply();
        }
    }

    protected void storeCategory(SharedPreferences.Editor editor, String categoryKey, Category category) {
        for (Subcategory subcategory : category.getSubcategories()) {
            String subcategoryKey = SUBCATEGORY_NAME_PREFIX + subcategory.getSlug();
            editor.putString(subcategoryKey, serializeSubcategory(subcategory));
        }

        editor.putString(categoryKey, serializeCategory(category));
    }

    protected String serializeCategory(Category category) {
        List<String> subcatsSlugs = new LinkedList<>();
        for (Subcategory subcategory : category.getSubcategories()) {
            subcatsSlugs.add(subcategory.getSlug());
        }

        return Joiner.on(PRIMARY_SEPARATOR).join(category.getId(), category.getName(), category.getSlug(), Joiner.on(SECONDARY_SEPARATOR).join(subcatsSlugs));
    }

    protected String serializeSubcategory(Subcategory subcategory) {
        return Joiner.on(PRIMARY_SEPARATOR).join(subcategory.getName(), subcategory.getSlug());
    }

    protected String serializeCategoryIds(List<Category> categories) {
        List<String> categoriesIds = new LinkedList<>();
        for (Category category : categories) {
            categoriesIds.add(String.valueOf(category.getId()));
        }

        return Joiner.on(PRIMARY_SEPARATOR).join(categoriesIds);
    }
}
