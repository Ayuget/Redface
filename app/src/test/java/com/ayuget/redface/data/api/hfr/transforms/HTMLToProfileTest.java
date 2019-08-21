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
import com.ayuget.redface.data.api.model.Profile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HTMLToProfileTest extends BaseTestCase {

    @Test
    public void test_parseProfile() throws IOException {
        HTMLToProfile htmlToProfile = new HTMLToProfile();

        Profile profile = htmlToProfile.call(readAssetFile("hfr_profile_page.html"));

        assertThat(profile.username()).isEqualTo("Ayuget");
        assertThat(profile.avatarUrl()).isEqualTo("https://forum-images.hardware.fr/images/mesdiscussions-65747.jpg");
        assertThat(profile.emailAddress()).isEqualTo("<i>Vous n'avez pas accès à cette information</i>");
        assertThat(profile.birthday()).isNull();
        assertThat(profile.sexGenre()).isEqualTo("homme");
        assertThat(profile.city()).isNull();
        assertThat(profile.employment()).isNull();
        assertThat(profile.hobbies()).isNull();
        assertThat(profile.status()).isEqualTo("Membre");
        assertThat(profile.messageCount()).isEqualTo(15116);
        assertThat(profile.arrivalDate()).isEqualTo("07/10/2002");
        assertThat(profile.lastMessageDate()).isEqualTo("30-07-2019 à 09:28");
        assertThat(profile.personalQuote()).isEqualTo("R.oger");
        assertThat(profile.messageSignature()).isNull();
    }
}
