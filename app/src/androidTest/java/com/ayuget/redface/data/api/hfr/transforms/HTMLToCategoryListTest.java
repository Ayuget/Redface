package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.BaseTestCase;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Subcategory;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class HTMLToCategoryListTest extends BaseTestCase {
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

        Category catProgramming = Category.create(10, "Programmation", "Programmation", subcatsProgramming);

        assertThat(categories).contains(catProgramming);
    }

}
