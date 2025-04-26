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

package com.ayuget.redface.data.api.hfr.transforms;

import android.os.Build;

import com.ayuget.redface.BaseTestCase;
import com.ayuget.redface.BuildConfig;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Subcategory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class HTMLToCategoryListTest extends BaseTestCase {
    @Test
    public void test_parseHFRCategories() throws IOException {
        HTMLToCategoryList htmlToCategoryList = new HTMLToCategoryList();

        List<Category> categories = htmlToCategoryList.call(readAssetFile("hfr_homepage.html"));

        assertThat(categories.size()).isGreaterThan(0);

        // Just verify that a certain category is present, with the correct subcategories
        // Useful to assert that the parsing is still working
        List<Subcategory> subcatsProgramming = Arrays.asList(
                Subcategory.create("C++", "C-2"),
                Subcategory.create("HTML/CSS", "HTML-CSS-Javascript"),
                Subcategory.create("Java", "Java"),
                Subcategory.create("PHP", "PHP"),
                Subcategory.create("SQL/NoSQL", "SGBD-SQL"),
                Subcategory.create("VB/VBA/VBS", "VB-VBA-VBS")
        );

        Category catProgramming = Category.builder()
                .id(10)
                .name("Programmation")
                .slug("Programmation")
                .subcategories(subcatsProgramming)
                .build();

        assertThat(categories).contains(catProgramming);
    }

}
