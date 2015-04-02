package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicStatus;
import com.ayuget.redface.util.DateUtils;
import com.ayuget.redface.util.HTMLUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

public class HTMLToTopicList implements Func1<String, List<Topic>> {
    private static final Pattern TOPIC_PATTERN = Pattern.compile(
            "(?:(?:<th\\s*class=\"padding\".*?<a\\s*href=\"/forum1\\.php\\?config=hfr\\.inc&amp;cat=([0-9]+).*?\"\\s*class=\"cHeader\">(.*?)</a></th>)" +
                    "|(<tr\\s*class=\"sujet\\s*ligne_booleen.*?(ligne_sticky)?\".*?" +
                    "<td.*?class=\"sujetCase1.*?><img\\s*src=\".*?([A-Za-z0-9]+)\\.gif\".*?" +
                    "<td.*?class=\"sujetCase3\".*?>(<span\\s*class=\"red\"\\s*title=\".*?\">\\[non lu\\]</span>\\s*)?.*?<a.*?class=\"cCatTopic\"\\s*title=\"Sujet n°([0-9]+)\">(.+?)</a></td>.*?" +
                    "<td.*?class=\"sujetCase4\".*?(?:(?:<a.*?class=\"cCatTopic\">(.+?)</a>)|&nbsp;)</td>.*?" +
                    "<td.*?class=\"sujetCase5\".*?(?:(?:<a\\s*href=\".*?#t([0-9]+)\"><img.*?src=\".*?([A-Za-z0-9]+)\\.gif\"\\s*title=\".*?\\(p\\.([0-9]+)\\)\".*?/></a>)|&nbsp;)</td>.*?" +
                    "<td.*?class=\"sujetCase6.*?>(?:<a\\s*rel=\"nofollow\"\\s*href=\"/profilebdd.*?>)?(.+?)(?:</a>)?</td>.*?" +
                    "<td.*?class=\"sujetCase7\".*?>(.+?)</td>.*?" +
                    "<td.*?class=\"sujetCase9.*?>.*?class=\"Tableau\">" +
                    "([0-9]+)-([0-9]+)-([0-9]+).*?([0-9]+):([0-9]+)<br /><b>(.+?)</b>.*?</td>.*?" +
                    "</tr>))"
            , Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern isLockedPattern = Pattern.compile("lock\\.gif");

    private static final Pattern hasUnreadPostsPattern = Pattern.compile("closedb_new\\.gif");

    private boolean isTopicLocked(String value) {
        Matcher m = isLockedPattern.matcher(value);
        return m.find();
    }

    private boolean hasUnreadPosts(String value) {
        Matcher m = hasUnreadPostsPattern.matcher(value);
        return m.find();
    }

    private TopicStatus extractTopicStatusFromImageName(String imageName) {
        if (imageName == null) {
            return TopicStatus.NONE;
        }
        else if (imageName.equals("flag1")) {
            return TopicStatus.FLAGGED_NEW_CONTENT;
        }
        else if (imageName.equals("flag0")) {
            return TopicStatus.READ_NEW_CONTENT;
        }
        else if (imageName.equals("favoris")) {
            return TopicStatus.FAVORITE_NEW_CONTENT;
        }
        else if (imageName.equals("closed")) {
            return TopicStatus.NO_NEW_CONTENT;
        }
        else {
            return TopicStatus.NONE;
        }
    }

    @Override
    public List<Topic> call(String source) {
        List<Topic> topics = new ArrayList<>();

        Matcher m = TOPIC_PATTERN.matcher(source);

        while (m.find()) {
            int topicId = Integer.parseInt(m.group(7));
            String subject = HTMLUtils.escapeHTML(m.group(8));
            String author = m.group(13);
            int pagesCount = m.group(9) != null ? Integer.parseInt(m.group(9)) : 1;
            String lastPostAuthor = m.group(20);
            Date lastPostDate = DateUtils.fromHTMLDate(m.group(17), m.group(16), m.group(15), m.group(18), m.group(19));
            boolean isSticky = m.group(4) != null;
            boolean isLocked = isTopicLocked(m.group(3));
            TopicStatus status = isLocked ? TopicStatus.LOCKED : extractTopicStatusFromImageName(m.group(11) != null ? m.group(11) : m.group(5));
            int lastReadPage = m.group(12) != null ? Integer.parseInt(m.group(12)) : -1;
            long lastReadPostId = m.group(10) != null ? Long.parseLong(m.group(10)) : -1;

            Topic topic = new Topic(topicId);
            topic.setSubject(subject);
            topic.setPagesCount(pagesCount);
            topic.setAuthor(author);
            topic.setStatus(status);
            topic.setLastPostAuthor(lastPostAuthor);
            topic.setLastPostDate(lastPostDate);
            topic.setSticky(isSticky);
            topic.setLocked(isLocked);
            topic.setLastReadPostPage(lastReadPage);
            topic.setLastReadPostId(lastReadPostId);
            topic.setHasUnreadPosts(hasUnreadPosts(m.group(3)));

            topics.add(topic);
        }


        return topics;
    }
}
