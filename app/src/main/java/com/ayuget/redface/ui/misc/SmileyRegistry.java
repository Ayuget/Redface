package com.ayuget.redface.ui.misc;

import android.util.LruCache;

import com.ayuget.redface.data.api.model.Smiley;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple cache built specifically to deal with smiley actions inside a topic (long-press on
 * a smiley), since there seems to be no standard way to get the {@code alt} attribute from the
 * image (in our case, the smiley) which was long-pressed.
 * <p>
 * So the idea here is to register a mapping between smiley URLs and the corresponding
 * {@link Smiley} instance so that in the context menu handler, one can find back the smiley
 * which was long-pressed from its URL.
 */
public class SmileyRegistry {
	private static final Pattern SMILEY_PATTERN = Pattern.compile("((?:https://forum-images.hardware.fr/)(?:(?:icones/smilies)|(?:images/perso))(?:[^\"]*?))(?:\" alt=\")([^\"]+)(?:\")",
		Pattern.CASE_INSENSITIVE | Pattern.DOTALL
	);

	private LruCache<String, Smiley> smileyCache = new LruCache<>(200);

	public void register(String smileyUrl, Smiley smiley) {
		smileyCache.put(smileyUrl, smiley);
	}

	public Smiley getSmileyFromUrl(String smileyUrl) {
		return smileyCache.get(smileyUrl);
	}

	public void registerSmiliesFromSource(String htmlSource) {
		Matcher m = SMILEY_PATTERN.matcher(htmlSource);

		while (m.find()) {
			Smiley smiley = Smiley.create(m.group(2), m.group(1));
			register(smiley.imageUrl(), smiley);
		}
	}
}
