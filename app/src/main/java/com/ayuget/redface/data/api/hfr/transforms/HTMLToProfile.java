package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.data.api.model.Profile;
import com.ayuget.redface.data.api.model.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

public class HTMLToProfile implements Func1<String, Profile> {
    public static final Pattern PROFILE_PATTERN = Pattern.compile(
            "<td\\s*class=\"profilCase4\"\\s*rowspan=\"6\"\\s*style=\"text-align:center\">\\s*" +
                    "(?:(?:<div\\s*class=\"avatar_center\"\\s*style=\"clear:both\"><img\\s*src=\"(.*?)\")|</td>).*?" +
                    "<td\\s*class=\"profilCase2\">Date de naissance.*?</td>\\s*<td\\s*class=\"profilCase3\">(.*?)</td>.*?"
            , Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private final User user;

    public HTMLToProfile(User user) {
        this.user = user;
    }

    @Override
    public Profile call(String s) {
        Matcher m = PROFILE_PATTERN.matcher(s);

        if (m.find()) {
            String avatarUrl = m.group(1);

            return Profile.builder()
                    .username(user.getUsername())
                    .avatarUrl(avatarUrl)
                    .build();
        }
        return null;
    }
}
