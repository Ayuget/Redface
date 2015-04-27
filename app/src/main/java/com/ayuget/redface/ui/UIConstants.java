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
