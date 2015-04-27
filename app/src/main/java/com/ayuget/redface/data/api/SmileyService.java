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

package com.ayuget.redface.data.api;

import com.ayuget.redface.data.api.model.misc.SmileyResponse;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface SmileyService {
    @GET("/api/forum.hardware.fr/users/{user_id}/stickers/recent?locale=fr_formal&lim=50&off=0&_method=GET")
    Observable<SmileyResponse> getUserSmileys(@Path("user_id") int userId);

    @GET("/api/forum.hardware.fr/stickers/search/np=1_bc=0/{search_expression}?locale=fr_formal&lim=70&off=0&_method=GET")
    Observable<SmileyResponse> searchSmileys(@Path("search_expression") String searchExpression);

    @GET("/api/forum.hardware.fr/stickers/popular?locale=fr_formal&lim=&off=0&_method=GET")
    Observable<SmileyResponse> getPopularSmileys();
}
