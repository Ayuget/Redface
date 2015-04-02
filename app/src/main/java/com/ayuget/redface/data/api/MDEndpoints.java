package com.ayuget.redface.data.api;

import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicFilter;

public interface MDEndpoints {
    /**
     * Homepage URL (with the list of categories)
     */
    public String homepage();

    /**
     * Category URL (page containing related topics)
     */
    public String category(Category category, int page, TopicFilter topicFilter);

    /**
     * Subcategory URL (page containing related topics)
     */
    public String subcategory(Category category, Subcategory subcategory, int page, TopicFilter topicFilter);

    /**
     * Topic URL
     */
    public String topic(Topic topic, int page);
    public String topic(Topic topic);
    public String topic(Category category, int topicId);

    /**
     * Forum's base url
     */
    public String baseurl();

    public String loginUrl();

    public String userAvatar(int user_id);

    public String smileyApiHost();

    public String replyUrl();

    public String editUrl();

    public String quote(Category category, Topic topic, int postId);

    public String editPost(Category category, Topic topic, int postId);
}
