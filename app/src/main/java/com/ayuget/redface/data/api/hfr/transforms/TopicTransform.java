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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicTransform {
    protected static final Pattern TOPIC_PATTERN = Pattern.compile(
            "(?:(?:<th\\s*class=\"padding\".*?<a\\s*href=\"/forum1\\.php\\?config=hfr\\.inc&amp;cat=([0-9]+).*?\"\\s*class=\"cHeader\">(.*?)</a></th>)" +
                    "|(<tr\\s*class=\"sujet\\s*ligne_booleen.*?(ligne_sticky)?\".*?" +
                    "<td.*?class=\"sujetCase1.*?><img\\s*src=\".*?([A-Za-z0-9]+)\\.gif\".*?" +
                    "<td.*?class=\"sujetCase3\".*?>(<span\\s*class=\"red\"\\s*title=\".*?\">\\[non lu\\]</span>\\s*)?.*?<a.*?class=\"cCatTopic\"\\s*title=\"Sujet n°([0-9]+)\">(.+?)</a></td>.*?" +
                    "<td.*?class=\"sujetCase4\".*?(?:(?:<a.*?class=\"cCatTopic\">(.+?)</a>)|&nbsp;)</td>.*?" +
                    "<td.*?class=\"sujetCase5\".*?(?:(?:<a\\s*href=\".*?#t([0-9]+)\"><img.*?src=\".*?([A-Za-z0-9]+)\\.gif\"\\s*title=\".*?\\(p\\.([0-9]+)\\)\".*?/></a>)|&nbsp;)</td>.*?" +
                    "<td.*?class=\"sujetCase6.*?>(?:<a\\s*rel=\"nofollow\"\\s*href=\"/profilebdd.*?>)?(?:<span.*?>)?(.+?)(?:</span>)?(?:</a>)?</td>.*?" +
                    "<td.*?class=\"sujetCase7\".*?>(.+?)</td>.*?" +
                    "<td.*?class=\"sujetCase9.*?>.*?class=\"Tableau\">" +
                    "([0-9]+)-([0-9]+)-([0-9]+).*?([0-9]+):([0-9]+)<br /><b>(.*?)</b>.*?</td>.*?" +
                    "</tr>))"
            , Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    protected static final Pattern TOPIC_LOCKED_PATTERN = Pattern.compile("lock\\.gif");

    protected static final Pattern HAS_UNREAD_POSTS_PATTERN = Pattern.compile("(closedb_new|closedb)\\.gif");

    protected boolean isTopicLocked(String value) {
        Matcher m = TOPIC_LOCKED_PATTERN.matcher(value);
        return m.find();
    }

    protected boolean hasUnreadPosts(String value) {
        Matcher m = HAS_UNREAD_POSTS_PATTERN.matcher(value);
        return m.find();
    }
}
