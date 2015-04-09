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

package com.ayuget.redface.data.api.model;

public class Response {
    private final boolean successful;

    private final ResponseCode code;

    private Response(boolean successful, ResponseCode code) {
        this.successful = successful;
        this.code = code;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public ResponseCode getCode() {
        return code;
    }

    public static Response buildSuccess(ResponseCode code) {
        return new Response(true, code);
    }

    public static Response buildFailure(ResponseCode code) {
        return new Response(false, code);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Response{");
        sb.append("successful=").append(successful);
        sb.append(", code=").append(code);
        sb.append('}');
        return sb.toString();
    }
}
