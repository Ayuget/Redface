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
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HTMLToProfileTest extends BaseTestCase {

    @Test
    public void parseAyugetProfile() throws IOException {
        HTMLToProfile htmlToProfile = new HTMLToProfile();

        Profile profile = htmlToProfile.call(readAssetFile("hfr_profile_page_ayuget.html"));

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
        assertThat(profile.personalSmilies()).hasSize(7);
    }

    @Test
    public void parseMarcProfile() throws IOException {
        HTMLToProfile htmlToProfile = new HTMLToProfile();

        Profile profile = htmlToProfile.call(readAssetFile("hfr_profile_page_marc.html"));

        assertThat(profile.username()).isEqualTo("Marc");
        assertThat(profile.status()).isEqualTo("Super Administrateur");
        assertThat(profile.city()).isEqualTo("Lyon");
        assertThat(profile.hobbies()).isEqualTo("Marie, Noémie et Simon");
        assertThat(profile.messageCount()).isEqualTo(110623);
        assertThat(profile.personalSmilies()).hasSize(2);
    }

    @Test
    public void parseManuLMProfile() throws IOException {
        HTMLToProfile htmlToProfile = new HTMLToProfile();

        Profile profile = htmlToProfile.call(readAssetFile("hfr_profile_page_manulm.html"));

        assertThat(profile.username()).isEqualTo("ManuLM");
        assertThat(profile.personalSmilies()).hasSize(0);
    }

    @Test
    public void parseToyonosProfile() throws IOException {
        HTMLToProfile htmlToProfile = new HTMLToProfile();

        Profile profile = htmlToProfile.call(readAssetFile("hfr_profile_page_toyonos.html"));

        assertThat(profile.username()).isEqualTo("toyonos");
        assertThat(profile.employment()).isEqualTo("Ingénieur info (pour faire original)");
        assertThat(profile.arrivalDate()).isEqualTo("18/02/2002");
        assertThat(profile.messageSignature()).isEqualTo("<a rel=\"nofollow\" href=\"http://bit.ly/18R06ju\" target=\"_blank\" class=\"cLink\"><strong>Marre de perdre du temps à chercher vos sous titres ?</strong></a> | <a rel=\"nofollow\" href=\"http://bit.ly/17O2DZU\" target=\"_blank\" class=\"cLink\">HFR4droid</a>");
    }
}
