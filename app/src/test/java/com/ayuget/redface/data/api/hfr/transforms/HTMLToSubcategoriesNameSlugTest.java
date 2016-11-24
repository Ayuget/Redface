/*
 * Copyright 2016 nbonnec
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

import com.ayuget.redface.BaseTestCase;
import com.ayuget.redface.data.api.model.Subcategory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HTMLToSubcategoriesNameSlugTest extends BaseTestCase {

    @Test
    public void test_parseSubcategories() throws IOException {
        HTMLToSubcategoriesNameSlug parser = new HTMLToSubcategoriesNameSlug();
        List<Subcategory> subcategories = parser.call(readAssetFile("hfr_topics_page.html"));

        assertThat(subcategories.size()).isEqualTo(10);

        int cnt = 0;
        assertThat(subcategories.get(cnt).name()).isEqualTo("Autres OS Mobiles");
        assertThat(subcategories.get(cnt).slug()).isEqualTo("autres-os-mobiles");

        cnt++;
        assertThat(subcategories.get(cnt).name()).isEqualTo("Opérateur");
        assertThat(subcategories.get(cnt).slug()).isEqualTo("operateur");

        cnt++;
        assertThat(subcategories.get(cnt).name()).isEqualTo("Téléphone Android");
        assertThat(subcategories.get(cnt).slug()).isEqualTo("telephone-android");

        cnt++;
        assertThat(subcategories.get(cnt).name()).isEqualTo("Téléphone Windows Phone");
        assertThat(subcategories.get(cnt).slug()).isEqualTo("telephone-windows-phone");

        cnt++;
        assertThat(subcategories.get(cnt).name()).isEqualTo("Téléphone");
        assertThat(subcategories.get(cnt).slug()).isEqualTo("telephone");

        cnt++;
        assertThat(subcategories.get(cnt).name()).isEqualTo("Tablette");
        assertThat(subcategories.get(cnt).slug()).isEqualTo("tablette");

        cnt++;
        assertThat(subcategories.get(cnt).name()).isEqualTo("Android");
        assertThat(subcategories.get(cnt).slug()).isEqualTo("android");

        cnt++;
        assertThat(subcategories.get(cnt).name()).isEqualTo("Windows Phone");
        assertThat(subcategories.get(cnt).slug()).isEqualTo("windows-phone");

        cnt++;
        assertThat(subcategories.get(cnt).name()).isEqualTo("GPS / PDA");
        assertThat(subcategories.get(cnt).slug()).isEqualTo("GPS-PDA");

        cnt++;
        assertThat(subcategories.get(cnt).name()).isEqualTo("Accessoires");
        assertThat(subcategories.get(cnt).slug()).isEqualTo("accessoires");

    }
}
