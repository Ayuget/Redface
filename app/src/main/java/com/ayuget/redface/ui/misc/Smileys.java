package com.ayuget.redface.ui.misc;


import com.ayuget.redface.data.api.model.Smiley;

import java.util.Arrays;
import java.util.List;

public class Smileys {
    private static final List<Smiley> DEFAULT_SMILEYS = Arrays.asList(
            Smiley.make(":O", "https://forum-images.hardware.fr/icones/redface.gif"),
            Smiley.make(":)", "https://forum-images.hardware.fr/icones/smile.gif"),
            Smiley.make(":(", "https://forum-images.hardware.fr/icones/frown.gif"),
            Smiley.make(":D", "https://forum-images.hardware.fr/icones/biggrin.gif"),
            Smiley.make(";)", "https://forum-images.hardware.fr/icones/wink.gif"),
            Smiley.make(":ouch:", "https://forum-images.hardware.fr/icones/smilies/ouch.gif"),
            Smiley.make(":??:", "https://forum-images.hardware.fr/icones/confused.gif"),
            Smiley.make(":p", "https://forum-images.hardware.fr/icones/tongue.gif"),
            Smiley.make(":pfff:", "https://forum-images.hardware.fr/icones/smilies/pfff.gif"),
            Smiley.make(":ange:", "https://forum-images.hardware.fr/icones/smilies/ange.gif"),
            Smiley.make(":non:", "https://forum-images.hardware.fr/icones/smilies/non.gif"),
            Smiley.make(":bounce:", "https://forum-images.hardware.fr/icones/smilies/bounce.gif"),
            Smiley.make(":fou:", "https://forum-images.hardware.fr/icones/smilies/fou.gif"),
            Smiley.make(":jap:", "https://forum-images.hardware.fr/icones/smilies/jap.gif"),
            Smiley.make(":lol:", "https://forum-images.hardware.fr/icones/smilies/lol.gif"),
            Smiley.make(":wahoo:", "https://forum-images.hardware.fr/icones/smilies/wahoo.gif"),
            Smiley.make(":kaola:", "https://forum-images.hardware.fr/icones/smilies/kaola.gif"),
            Smiley.make(":love:", "https://forum-images.hardware.fr/icones/smilies/love.gif"),
            Smiley.make(":heink:", "https://forum-images.hardware.fr/icones/smilies/heink.gif"),
            Smiley.make(":cry:", "https://forum-images.hardware.fr/icones/smilies/cry.gif"),
            Smiley.make(":whistle:", "https://forum-images.hardware.fr/icones/smilies/whistle.gif"),
            Smiley.make(":sol:", "https://forum-images.hardware.fr/icones/smilies/sol.gif"),
            Smiley.make(":pt1cable:", "https://forum-images.hardware.fr/icones/smilies/pt1cable.gif"),
            Smiley.make(":sleep:", "https://forum-images.hardware.fr/icones/smilies/sleep.gif"),
            Smiley.make(":sweat:", "https://forum-images.hardware.fr/icones/smilies/sweat.gif"),
            Smiley.make(":hello:", "https://forum-images.hardware.fr/icones/smilies/hello.gif"),
            Smiley.make(":na:", "https://forum-images.hardware.fr/icones/smilies/na.gif"),
            Smiley.make(":sarcastic:", "https://forum-images.hardware.fr/icones/smilies/sarcastic.gif")
    );

    public static List<Smiley> defaultSmileys() {
        return DEFAULT_SMILEYS;
    }
}
