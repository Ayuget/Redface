package com.ayuget.redface.ui.misc;


import com.ayuget.redface.data.api.model.Smiley;

import java.util.Arrays;
import java.util.List;

public class Smileys {
    private static final List<Smiley> DEFAULT_SMILEYS = Arrays.asList(
            Smiley.create(":O", "https://forum-images.hardware.fr/icones/redface.gif"),
            Smiley.create(":)", "https://forum-images.hardware.fr/icones/smile.gif"),
            Smiley.create(":(", "https://forum-images.hardware.fr/icones/frown.gif"),
            Smiley.create(":D", "https://forum-images.hardware.fr/icones/biggrin.gif"),
            Smiley.create(";)", "https://forum-images.hardware.fr/icones/wink.gif"),
            Smiley.create(":ouch:", "https://forum-images.hardware.fr/icones/smilies/ouch.gif"),
            Smiley.create(":??:", "https://forum-images.hardware.fr/icones/confused.gif"),
            Smiley.create(":p", "https://forum-images.hardware.fr/icones/tongue.gif"),
            Smiley.create(":pfff:", "https://forum-images.hardware.fr/icones/smilies/pfff.gif"),
            Smiley.create(":ange:", "https://forum-images.hardware.fr/icones/smilies/ange.gif"),
            Smiley.create(":non:", "https://forum-images.hardware.fr/icones/smilies/non.gif"),
            Smiley.create(":bounce:", "https://forum-images.hardware.fr/icones/smilies/bounce.gif"),
            Smiley.create(":fou:", "https://forum-images.hardware.fr/icones/smilies/fou.gif"),
            Smiley.create(":jap:", "https://forum-images.hardware.fr/icones/smilies/jap.gif"),
            Smiley.create(":lol:", "https://forum-images.hardware.fr/icones/smilies/lol.gif"),
            Smiley.create(":wahoo:", "https://forum-images.hardware.fr/icones/smilies/wahoo.gif"),
            Smiley.create(":kaola:", "https://forum-images.hardware.fr/icones/smilies/kaola.gif"),
            Smiley.create(":love:", "https://forum-images.hardware.fr/icones/smilies/love.gif"),
            Smiley.create(":heink:", "https://forum-images.hardware.fr/icones/smilies/heink.gif"),
            Smiley.create(":cry:", "https://forum-images.hardware.fr/icones/smilies/cry.gif"),
            Smiley.create(":whistle:", "https://forum-images.hardware.fr/icones/smilies/whistle.gif"),
            Smiley.create(":sol:", "https://forum-images.hardware.fr/icones/smilies/sol.gif"),
            Smiley.create(":pt1cable:", "https://forum-images.hardware.fr/icones/smilies/pt1cable.gif"),
            Smiley.create(":sleep:", "https://forum-images.hardware.fr/icones/smilies/sleep.gif"),
            Smiley.create(":sweat:", "https://forum-images.hardware.fr/icones/smilies/sweat.gif"),
            Smiley.create(":hello:", "https://forum-images.hardware.fr/icones/smilies/hello.gif"),
            Smiley.create(":na:", "https://forum-images.hardware.fr/icones/smilies/na.gif"),
            Smiley.create(":sarcastic:", "https://forum-images.hardware.fr/icones/smilies/sarcastic.gif")
    );

    public static List<Smiley> defaultSmileys() {
        return DEFAULT_SMILEYS;
    }
}
