package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.data.api.model.Topic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

public class HTMLToTopic implements Func1<String, Topic> {
    public static final Pattern TOPIC_DETAILS_PATTERN = Pattern.compile("(?:<input type=\"hidden\" name=\"post\")(?:\\s*)(?:value=\")(\\d+)(?:\")(?:.*?)(?:<h3>)(.*?)(?:</h3>)(?:.*)(?:class=\"cHeader\">)(.*?)(?:</a></div>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public Topic call(String s) {
        Matcher m = TOPIC_DETAILS_PATTERN.matcher(s);

        if (m.find()) {
            int topicId = Integer.valueOf(m.group(1));
            String topicSubject = m.group(2);
            int pagesCount = Integer.valueOf(m.group(3));

            // Sets up a topic with all vital informations for it to be displayed
            Topic topic = new Topic(topicId);
            topic.setSubject(topicSubject);
            topic.setPagesCount(pagesCount);

            return topic;
        }
        return null;
    }
}
