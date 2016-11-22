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

import com.ayuget.redface.data.api.model.Subcategory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

public class HTMLToSubcategoryIdList implements Func1<String, List<Subcategory>> {

    private static Pattern subcategoryId = Pattern.compile("<option\\s*value=\"([0-9]+)\"\\s*>(.+?)</option>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public List<Subcategory> call(String source) {
        List<Subcategory> subcategories = new ArrayList<>();

        Matcher m = subcategoryId.matcher(source);

        while (m.find()) {
            Subcategory subcategory = Subcategory.create(m.group(2), Integer.parseInt(m.group(1)));
            subcategories.add(subcategory);
        }

        return subcategories;
    }
}
