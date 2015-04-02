package com.ayuget.redface.ui;

public class UIConstants {
    public static final int INVALID_POINTER_ID = -1;

    /**
     * Mime-type for pages displayed in the post webview
     */
    public static final String MIME_TYPE = "text/html";

    /**
     * Character encoding for the HTML pages
     */
    public static final String POSTS_ENCODING = "utf-8";

    public static final int UNKNOWN_PAGES_COUNT = -1;

    /**
     * Topic context menu actions
     */
    public static final int TOPIC_ACTION_GO_TO_LAST_READ_PAGE = 1;
    public static final int TOPIC_ACTION_GO_TO_FIRST_PAGE = 2;
    public static final int TOPIC_ACTION_GO_TO_SPECIFIC_PAGE = 3;
    public static final int TOPIC_ACTION_GO_TO_LAST_PAGE = 4;
    public static final int TOPIC_ACTION_REPLY_TO_TOPIC = 5;

    /**
     * Reply intent
     */
    public static final int REPLY_REQUEST_CODE = 1;
    public static final int REPLY_RESULT_KO = 1;
    public static final String ARG_REPLY_TOPIC = "TOPIC";
    public static final String ARG_REPLY_WAS_EDIT = "EDITED";
    public static final String ARG_REPLY_CONTENT = "replyContent";
    public static final String ARG_EDITED_POST_ID = "editedPostId";
}
