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

package com.ayuget.redface.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class HTTPRedirectionTest {
    @Test
    public void testResolveURL() throws Exception {
        String redirectedUrl = "http://forum.hardware.fr/forum2.php?config=hfr.inc&cat=13&subcat=430&post=61179&page=1&p=1&sondage=0&owntopic=0&trash=0&trash_post=0&print=0&numreponse=45255613&quote_only=0&new=0&nojs=0#t45255613";
        String targetUrl = "http://forum.hardware.fr/hfr/Discussions/Sports/football-ballon-rond-sujet_61179_40331.htm#t45255613";

        String resolvedUrl = HTTPRedirection.resolve(redirectedUrl).toBlocking().first();
        assertThat(resolvedUrl).isEqualTo(targetUrl);
    }
}
