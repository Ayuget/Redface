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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import static org.assertj.core.api.Assertions.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class HTMLToBBCodeTest extends BaseTestCase {
    @Test
    public void test_quoteParsing() throws IOException {
        HTMLToBBCode htmlToBBCode = new HTMLToBBCode();

        String bbCode = htmlToBBCode.call(readAssetFile("hfr_quote.html"));

        String expectedQuote = "[quotemsg=1906537,16716,459815]\n" +
                "\n" +
                "N'empêche j'comprends pas, l'appli devrait être payante pour rémunérer un minimum les devs. Vu que certains peuvent pas se passer d'HFR et qu'y aller via le navigateur de SP c'est pas terrible, les users devraient raquer vu que ça leur rend bien service.\n" +
                "\n" +
                "De quelle pub tu parles???? :heink:\n" +
                "\n" +
                "Tu sais comment est utilisé le fric de la pub et des affils? Tu crois que c'est pour payer des porsche à sly et des ssd à marc? Tu te sens volé ou utilisé en allant sur HFR?[/quotemsg]\n" +
                "\n";

        assertThat(bbCode).isEqualTo(expectedQuote);
    }
}
