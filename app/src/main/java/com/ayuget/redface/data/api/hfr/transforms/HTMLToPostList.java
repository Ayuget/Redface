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

import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.util.DateUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

public class HTMLToPostList implements Func1<String, List<Post>> {
    /**
     * Default number of posts per topic page. Used to initialize default capacity
     * for the list of posts (small performance improvement ?)
     */
    private static final int DEFAULT_POSTS_COUNT = 40;

    private static final Pattern PAGE_PATTERN = Pattern.compile("Pages\\s*:\\s*(\\d+)");
    private int getTopicPageCount(Document html) {
        Elements htmlMetas = html.getElementsByTag("meta");
        for(Element htmlMeta: htmlMetas) {
            Elements htmlDescs = htmlMeta.getElementsByAttributeValue("name", "Description");
            for(Element htmlDesc: htmlDescs) {
                String desc = htmlDesc.attr("content");
                Matcher m = PAGE_PATTERN.matcher(desc);
                if(m.find())
                    return Integer.valueOf(m.group(1));
            }
        }

        return UIConstants.UNKNOWN_PAGES_COUNT;
    }

    private long getPostId(Element htmlPost) {
        Elements htmlPostIds = htmlPost.getElementsByAttributeValueStarting("href", "#t");
        if(!htmlPostIds.isEmpty()) {
            return Long.parseLong(htmlPostIds.first().attr("href").substring(2));
        }

        return 0;
    }

    private static final Pattern PROFIL_URL_PATTERN = Pattern.compile("/hfr/profil-(\\d+).htm");
    private int getAuthorUserId(Element htmlPost) {
        Elements htmlPostUserIds = htmlPost.getElementsByAttributeValueStarting("title", "Voir son profil");
        for(Element htmlPostUserId: htmlPostUserIds) {
            String profilUrl = htmlPostUserId.parent().attr("href");
            Matcher m = PROFIL_URL_PATTERN.matcher(profilUrl);
            if(m.find())
                return Integer.valueOf(m.group(1));
        }

        return 0;
    }

    private String getAuthorName(Element htmlPost) {
        Elements htmlBolds = htmlPost.getElementsByTag("b");
        for(Element htmlBold: htmlBolds) {
            if(htmlBold.hasClass("s2")) {
                return htmlBold.text();
            }
        }

        return "Unnamed";
    }

    private String getAvatarUrl(Element htmlPost) {
        Elements htmlAvatars = htmlPost.getElementsByClass("avatar_center");
        for(Element htmlAvatar: htmlAvatars) {
            Elements htmlAvatarImgs = htmlAvatar.getElementsByTag("img");
            for(Element htmlAvatarImg: htmlAvatarImgs) {
                return htmlAvatarImg.attr("src");
            }
        }

        return "";
    }

    private static final Pattern POST_DATE_PATTERN = Pattern.compile("Posté le\\s*(\\d+)-(\\d+)-(\\d+).*(\\d+):(\\d+):(\\d+)");
    private Date getPostDate(Element htmlPost) {
        Elements htmlToolbars = htmlPost.getElementsByClass("toolbar");
        for(Element htmlToolbar : htmlToolbars) {
            String html = htmlToolbar.html();
            Matcher m = POST_DATE_PATTERN.matcher(html);
            if(m.find()) {
                return DateUtils.fromHTMLDate(m.group(3), m.group(2), m.group(1), m.group(4), m.group(5), m.group(6));
            }
        }

        return null;
    }

    private static final Pattern QUOTE_NB_PATTERN  = Pattern.compile("Message cité (\\d+) fois");
    private int getQuoteCount(Element htmlEditBlock) {
        Elements htmlLinks = htmlEditBlock.getElementsByTag("a");
        for(Element htmlLink: htmlLinks) {
            String text = htmlLink.text();
            Matcher m = QUOTE_NB_PATTERN.matcher(text);
            if(m.find()) {
                return Integer.valueOf(m.group(1));
            }
        }
        return 0;
    }

    private static final Pattern EDIT_DATE_PATTERN = Pattern.compile("Message édité par .* le\\s*(\\d+)-(\\d+)-(\\d+).*(\\d+):(\\d+):(\\d+)");
    private Date getLastEditDate(Element htmlEditBlock) {
        String text = htmlEditBlock.text();
        Matcher m = EDIT_DATE_PATTERN.matcher(text);
        if(m.find()) {
            return DateUtils.fromHTMLDate(m.group(3), m.group(2), m.group(1), m.group(4), m.group(5), m.group(6));
        }
        return null;
    }

    private String getPostHTMLContent(Element htmlPost) {
        Elements htmlMsgs = htmlPost.getElementsByAttributeValueStarting("id", "para");
        for(Element htmlMsgTmp : htmlMsgs) {
            //removing the "quote" part
            Element htmlMsg = htmlMsgTmp.clone();
            Elements htmlEdits = htmlMsg.getElementsByClass("edited");
            for(Element htmlEdit: htmlEdits) {
                htmlEdit.remove();
            }

            Elements htmlDivs = htmlMsg.getElementsByTag("div");
            for(Element htmlDiv: htmlDivs) {
                if(htmlDiv.hasAttr("style")) {
                    htmlDiv.remove();
                }
            }

            return htmlMsg.html();
        }

        return "";
    }

    @Override
    public List<Post> call(String source) {
        Document doc = Jsoup.parse(source);
        int topicPagesCount = getTopicPageCount(doc);

        List<Post> posts = new ArrayList<>(DEFAULT_POSTS_COUNT);

        Elements htmlPosts = doc.getElementsByClass("message");
        for(Element htmlPost: htmlPosts) {
            Post post = new Post(getPostId(htmlPost));
            post.setAuthor(getAuthorName(htmlPost));
            post.setAuthorId(getAuthorUserId(htmlPost));
            post.setAvatarUrl(getAvatarUrl(htmlPost));
            post.setPostDate(getPostDate(htmlPost));

            Elements htmlEditBlocks = htmlPost.getElementsByClass("edited");
            for(Element htmlEditBlock: htmlEditBlocks) {
                post.setLastEditionDate(getLastEditDate(htmlEditBlock));
                post.setQuoteCount(getQuoteCount(htmlEditBlock));
            }

            post.setHtmlContent(getPostHTMLContent(htmlPost));

            if (topicPagesCount != UIConstants.UNKNOWN_PAGES_COUNT) {
                post.setTopicPagesCount(topicPagesCount);
            }

            posts.add(post);
        }

        return posts;
    }
}
