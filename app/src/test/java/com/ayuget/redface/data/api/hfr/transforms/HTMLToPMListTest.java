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
import com.ayuget.redface.data.api.model.PrivateMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class HTMLToPMListTest extends BaseTestCase {
    HTMLToPrivateMessageList htmlToPrivateMessageList = new HTMLToPrivateMessageList();
    @Test
    public void test_parsePrivateMessageList() throws IOException, ParseException {
        List<PrivateMessage> privateMessages = htmlToPrivateMessageList.call(readAssetFile("hfr_private_messages_list.html"));

        assertThat(privateMessages).hasSize(50);

        PrivateMessage firstPM = privateMessages.get(0);

        assertThat(firstPM.getSubject()).isEqualTo("De-ban IP");
        assertThat(firstPM.hasUnreadMessages()).isFalse();
        assertThat(firstPM.getRecipient()).isEqualTo("Sly Angel");
        assertThat(firstPM.hasBeenReadByRecipient()).isFalse();
        assertThat(firstPM.getTotalMessages()).isEqualTo(1);
        assertThat(firstPM.getLastResponseAuthor()).isEqualTo("Sly Angel");
    }
}
