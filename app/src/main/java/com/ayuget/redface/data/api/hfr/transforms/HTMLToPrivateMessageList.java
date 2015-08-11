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

import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.util.DateUtils;
import com.ayuget.redface.util.HTMLUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import rx.functions.Func1;

public class HTMLToPrivateMessageList extends TopicTransform implements Func1<String, List<PrivateMessage>> {
    private boolean hasNewMessages(String imageName) {
        return imageName.equals("closedbp");
    }

    @Override
    public List<PrivateMessage> call(String source) {
        List<PrivateMessage> privateMessages = new ArrayList<>();

        Matcher m = TOPIC_PATTERN.matcher(source);

        while (m.find()) {
            long privateMessageId = Long.parseLong(m.group(7));
            String subject = HTMLUtils.escapeHTML(m.group(8));
            String recipient = m.group(13);
            String lastResponseAuthor = m.group(20);
            Date lastResponseDate = DateUtils.fromHTMLDate(m.group(17), m.group(16), m.group(15), m.group(18), m.group(19));
            int totalMessages = Integer.parseInt(m.group(14));

            PrivateMessage privateMessage = new PrivateMessage.Builder()
                    .forRecipient(recipient)
                    .withId(privateMessageId)
                    .withSubject(subject)
                    .withLastResponse(lastResponseAuthor, lastResponseDate)
                    .withTotalMessages(totalMessages)
                    .withUnreadMessages(hasNewMessages(m.group(11) != null ? m.group(11) : m.group(5)))
                    .asReadByRecipient(m.group(6) == null)
                    .build();

            privateMessages.add(privateMessage);
        }


        return privateMessages;
    }
}
