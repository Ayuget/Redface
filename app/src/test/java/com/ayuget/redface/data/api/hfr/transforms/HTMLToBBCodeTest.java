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

import com.ayuget.redface.BaseTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

@RunWith(RobolectricTestRunner.class)
public class HTMLToBBCodeTest extends BaseTestCase {
    @Test
    public void test_quoteParsing() throws IOException {
        var htmlToBBCode = new HTMLToBBCode();

        var bbCode = htmlToBBCode.call(readAssetFile("hfr_quote.html"));

        var expectedQuote = "[quotemsg=2751289,12948,963963]La plus grosse difficulté c'est de garder la motivation                                                                  :D.\n" +
                "     Me connaissant, ça peut disparaître dès demain. Je ne voudrais pas vous donner de faux espoirs :o.\n" +
                "[/quotemsg]\n";

        assertThat(bbCode).isEqualTo(expectedQuote);
    }

    @Test
    public void test_postContentParsing() throws IOException {
        var htmlToBBCode = new HTMLToBBCode();

        var bbCode = htmlToBBCode.call(readAssetFile("hfr_edit_post.html"));

        var expextedQuote = "La plus grosse difficulté c'est de garder la motivation                                                                  :D.\n" +
                "     Me connaissant, ça peut disparaître dès demain. Je ne voudrais pas vous donner de faux espoirs :o." +
                "\n";

        assertThat(bbCode).isEqualTo(expextedQuote);
    }
}
