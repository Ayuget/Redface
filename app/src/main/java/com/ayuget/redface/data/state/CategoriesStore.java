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
import android.text.TextUtils;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.ui.UIConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;


public class CategoriesStore {
    public static final int META_CATEGORY_ID = 666;

    private static final String CATEGORIES_PREFS = "RedfaceCategories";
    private static final String CATEGORY_NAME_PREFIX = "category_";
    private static final String SUBCATEGORY_NAME_PREFIX = "subcategory_";
    private static final String USER_MAPPING_NAME_PREFIX = "user_mapping_";
    private static final String PRIMARY_SEPARATOR = "\\|";
    private static final String PRIMARY_SEPARATOR_UNESCAPED = "|";
    private static final String SECONDARY_SEPARATOR = "#";

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

    private Category privateMessagesCategory;

    public CategoriesStore(Context context) {
        this.userCategoriesCache = new HashMap<>();
        this.categoriesCache = new HashMap<>();

        this.categoriesPrefs = context.getSharedPreferences(CATEGORIES_PREFS, 0);

        this.metaCategory = Category.builder()
                .id(META_CATEGORY_ID)
                .name(context.getResources().getString(R.string.navdrawer_item_my_topics))
                .slug("meta")
                .subcategories(Collections.<Subcategory>emptyList())
                .build();

        this.privateMessagesCategory = Category.builder()
                .id(UIConstants.PRIVATE_MESSAGE_CAT_ID)
                .name(context.getResources().getString(R.string.navdrawer_item_private_messages))
                .slug("pm")
                .subcategories(Collections.<Subcategory>emptyList())
                .build();

        loadFromSharedPreferences();
    }

    public List<Category> getCategories(User user) {
        if (userCategoriesCache.containsKey(user.getUsername())) {
            return userCategoriesCache.get(user.getUsername());
        } else {
            return null;
        }
    }

    public Category getMetaCategory() {
        return this.metaCategory;
    }

    public Category getPrivateMessagesCategory() {
        return privateMessagesCategory;
    }

    public Category getCategoryById(int categoryId) {
        if (categoryId == META_CATEGORY_ID) {
            return this.metaCategory;
        } else {
            return categoriesCache.get(categoryId);
        }
    }

    public Category getCategoryBySlug(String categorySlug) {
        for (Category category : categoriesCache.values()) {
            if (category.slug().equals(categorySlug)) {
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
                List<String> tokens = Arrays.asList(TextUtils.split(entryValue, PRIMARY_SEPARATOR));

                if (tokens.size() == 4) {
                    int catId = Integer.valueOf(tokens.get(0));
                    String catName = tokens.get(1);
                    String catSlug = tokens.get(2);

                    List<Subcategory> subcategories = new LinkedList<>();

                    for (String subcatSlug : TextUtils.split(tokens.get(3), SECONDARY_SEPARATOR)) {
                        String subcatKey = SUBCATEGORY_NAME_PREFIX + subcatSlug;

                        if (categoriesPrefs.contains(subcatKey)) {
                            String subcatValue = categoriesPrefs.getString(subcatKey, "");
                            List<String> subcatsTokens = Arrays.asList(TextUtils.split(subcatValue, PRIMARY_SEPARATOR));

                            if (subcatsTokens.size() == 2) {
                                subcategories.add(Subcategory.create(subcatsTokens.get(0), subcatsTokens.get(1)));
                            } else {
                                Timber.e("Error while deserializing subcategory '%s'", subcatValue);
                            }
                        }
                    }
                    Timber.d("Successfully loaded category '%s' from SharedPreferences", catName);

                    Category category = Category.builder()
                            .id(catId)
                            .name(catName)
                            .slug(catSlug)
                            .subcategories(subcategories)
                            .build();

                    categoriesCache.put(catId, category);
                } else {
                    Timber.e("Error while deserializing category '%s'", entryValue);
                }
            } else if (entryKey.startsWith(USER_MAPPING_NAME_PREFIX)) {
                userKeys.add(entryKey);
            }
        }

        for (String userKey : userKeys) {
            String userMapping = categoriesPrefs.getString(userKey, "");
            String username = userKey.replace(USER_MAPPING_NAME_PREFIX, "");

            List<Category> userCategories = new LinkedList<>();

            for (String category : TextUtils.split(userMapping, PRIMARY_SEPARATOR)) {
                try {
                    int categoryId = Integer.valueOf(category);

                    if (categoriesCache.containsKey(categoryId)) {
                        userCategories.add(categoriesCache.get(categoryId));
                    } else {
                        Timber.e("Error while associating category '%s' to user '%s' : unknown category", category, username);
                    }
                } catch (NumberFormatException e) {
                    // Don't crash the app if an invalid category is found
                    clearCategories(); // Playing it safe...
                    Timber.e("Error, deserializing category with non-int id : %s", category);
                }
            }

            userCategoriesCache.put(username, userCategories);
        }
    }

    /**
     * Store categories and user/categories relationship in SharedPreferences
     */
    public void storeCategories(User user, List<Category> categories) {
        if (!userCategoriesCache.containsKey(user.getUsername())) {
            userCategoriesCache.put(user.getUsername(), categories);

            for (Category category : categories) {
                categoriesCache.put(category.id(), category);
            }

            SharedPreferences.Editor editor = categoriesPrefs.edit();

            for (Category category : categories) {
                String categoryKey = CATEGORY_NAME_PREFIX + category.id();

                if (!categoriesPrefs.contains(categoryKey)) {
                    storeCategory(editor, categoryKey, category);
                }
            }

            String userRelationshipKey = USER_MAPPING_NAME_PREFIX + user.getUsername();
            editor.putString(userRelationshipKey, serializeCategoryIds(categories));

            editor.apply();
        }
    }

    public void clearCategories() {
        categoriesPrefs.edit()
                .clear()
                .apply();

        userCategoriesCache.clear();
    }

    protected void storeCategory(SharedPreferences.Editor editor, String categoryKey, Category category) {
        for (Subcategory subcategory : category.subcategories()) {
            String subcategoryKey = SUBCATEGORY_NAME_PREFIX + subcategory.slug();
            editor.putString(subcategoryKey, serializeSubcategory(subcategory));
        }

        editor.putString(categoryKey, serializeCategory(category));
    }

    protected String serializeCategory(Category category) {
        List<String> subcatsSlugs = new LinkedList<>();
        for (Subcategory subcategory : category.subcategories()) {
            subcatsSlugs.add(subcategory.slug());
        }

        List<String> categoryElements = Arrays.asList(
                String.valueOf(category.id()), category.name(), category.slug(), TextUtils.join(SECONDARY_SEPARATOR, subcatsSlugs)
        );

        return TextUtils.join(PRIMARY_SEPARATOR_UNESCAPED, categoryElements);
    }

    protected String serializeSubcategory(Subcategory subcategory) {
        return TextUtils.join(PRIMARY_SEPARATOR_UNESCAPED, Arrays.asList(subcategory.name(), subcategory.slug()));
    }

    protected String serializeCategoryIds(List<Category> categories) {
        List<String> categoriesIds = new LinkedList<>();
        for (Category category : categories) {
            categoriesIds.add(String.valueOf(category.id()));
        }

        return TextUtils.join(PRIMARY_SEPARATOR_UNESCAPED, categoriesIds);
    }
}
