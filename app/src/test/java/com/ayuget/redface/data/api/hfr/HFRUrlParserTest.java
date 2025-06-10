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

package com.ayuget.redface.data.api.hfr;

import com.ayuget.redface.BaseTestCase;
import com.ayuget.redface.data.api.MDLink;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.ui.misc.PagePosition;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class HFRUrlParserTest extends BaseTestCase {
    HFREndpoints hfrEndpoints;

    Category dummyCat;

    @Mock
    CategoriesStore categoriesStore;

    public HFRUrlParserTest() {
        this.hfrEndpoints = new HFREndpoints();
        dummyCat = Category.builder()
                .id(66)
                .name("Dummy")
                .slug("Dummy")
                .subcategories(new LinkedList<Subcategory>())
                .build();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(categoriesStore.getCategoryBySlug(anyString())).thenReturn(dummyCat);
    }

    @Test
    public void test_parseRewrittenTopicLink() {
        HFRUrlParser urlParser = new HFRUrlParser(hfrEndpoints, categoriesStore);

        String rewrittenTopicUrl = "http://forum.hardware.fr/hfr/Discussions/politique/hollande-social-democratie-sujet_47220_1.htm";

        MDLink parsedLink = urlParser.parseUrl(rewrittenTopicUrl).toBlocking().first();

        assertThat(parsedLink.isTopic()).isTrue();
        assertThat(parsedLink.getTopicId()).isEqualTo(47220);
        assertThat(parsedLink.getTopicPage()).isEqualTo(1);
        assertThat(parsedLink.getPagePosition()).isEqualTo(PagePosition.top());
        assertThat(parsedLink.getCategory()).isEqualTo(dummyCat);
    }

    @Test
    public void test_parseStandardTopicLink() {
        HFRUrlParser urlParser = new HFRUrlParser(hfrEndpoints, categoriesStore);

        String standardTopicUrl = "http://forum.hardware.fr/forum2.php?config=hfr.inc&cat=13&subcat=423&post=59264&page=8748&p=1&sondage=0&owntopic=2&trash=0&trash_post=0&print=0&numreponse=0&quote_only=0&new=0&nojs=0#t42586267";

        when(categoriesStore.getCategoryById(13)).thenReturn(dummyCat);

        MDLink parsedLink = urlParser.parseUrl(standardTopicUrl).toBlocking().first();
        assertThat(parsedLink.isTopic()).isTrue();
        assertThat(parsedLink.getTopicId()).isEqualTo(59264);
        assertThat(parsedLink.getTopicPage()).isEqualTo(8748);
        assertThat(parsedLink.getPagePosition()).isEqualTo(PagePosition.at(42586267L));
        assertThat(parsedLink.getCategory()).isEqualTo(dummyCat);
    }

    @Test
    public void test_ParseRedirectedUrl()  {
        HFRUrlParser urlParser = new HFRUrlParser(hfrEndpoints, categoriesStore);

        // http://forum.hardware.fr/forum2.php?config=hfr.inc&cat=13&subcat=430&post=61179&page=1&p=1&sondage=0&owntopic=0&trash=0&trash_post=0&print=0&numreponse=45255613&quote_only=0&new=0&nojs=0#t45255613
        // will become another URL
        String redirectedUrl = "https://forum.hardware.fr/hfr/Discussions/Sports/football-ballon-rond-sujet_61179_40331.htm#t45255613";

        MDLink parsedLink = urlParser.parseUrl(redirectedUrl).toBlocking().first();

        assertThat(parsedLink.isTopic()).isTrue();
        assertThat(parsedLink.getTopicId()).isEqualTo(61179);
        assertThat(parsedLink.getTopicPage()).isEqualTo(40331);
        assertThat(parsedLink.getPagePosition()).isEqualTo(PagePosition.at(45255613L));

    }
}
