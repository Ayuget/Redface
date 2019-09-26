package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.data.api.model.Smiley;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

public class HTMLToFavoriteSmileyList implements Func1<String, List<Smiley>> {
	private final String FAVORITE_SMILEYS_TAG = "<td class=\"profilCase2\"><b>Smilies favoris";
	private static final Pattern SMILEY_PATTERN = Pattern.compile("(?:<img)(?:\\s+)(?:src=\")([^\"]+)(?:\")(?:\\s+)(?:alt=\")([^\"]+)(?:\")(?:[^\\/]*)(?:\\/>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


	@Override
	public List<Smiley> call(String pageSource) {
		int favoriteSmileysTagIndex = pageSource.indexOf(FAVORITE_SMILEYS_TAG);
		if (favoriteSmileysTagIndex == -1) {
			return Collections.emptyList();
		}

		String interestingSource = pageSource.substring(favoriteSmileysTagIndex);

		Matcher smileysMatcher = SMILEY_PATTERN.matcher(interestingSource);

		List<Smiley> foundSmileys = new ArrayList<>();

		while (smileysMatcher.find()) {
			foundSmileys.add(Smiley.create(smileysMatcher.group(2), smileysMatcher.group(1)));
		}

		return foundSmileys;
	}
}
