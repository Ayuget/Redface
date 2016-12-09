package com.ayuget.redface.ui.misc;


import com.ayuget.redface.data.api.model.Smiley;

import java.util.Arrays;
import java.util.List;

public class Smileys {
    private static final List<Smiley> DEFAULT_SMILEYS = Arrays.asList(
            Smiley.make(":O", "http://forum-images.hardware.fr/icones/redface.gif"),
            Smiley.make(":)", "http://forum-images.hardware.fr/icones/smile.gif"),
            Smiley.make(":(", "http://forum-images.hardware.fr/icones/frown.gif"),
            Smiley.make(":D", "http://forum-images.hardware.fr/icones/biggrin.gif"),
            Smiley.make(";)", "http://forum-images.hardware.fr/icones/wink.gif"),
            Smiley.make(":ouch:", "http://forum-images.hardware.fr/icones/smilies/ouch.gif"),
            Smiley.make(":??:", "http://forum-images.hardware.fr/icones/confused.gif"),
            Smiley.make(":p", "http://forum-images.hardware.fr/icones/tongue.gif"),
            Smiley.make(":pfff:", "http://forum-images.hardware.fr/icones/smilies/pfff.gif"),
            Smiley.make(":ange:", "http://forum-images.hardware.fr/icones/smilies/ange.gif"),
            Smiley.make(":non:", "http://forum-images.hardware.fr/icones/smilies/non.gif"),
            Smiley.make(":bounce:", "http://forum-images.hardware.fr/icones/smilies/bounce.gif"),
            Smiley.make(":fou:", "http://forum-images.hardware.fr/icones/smilies/fou.gif"),
            Smiley.make(":jap:", "http://forum-images.hardware.fr/icones/smilies/jap.gif"),
            Smiley.make(":lol:", "http://forum-images.hardware.fr/icones/smilies/lol.gif"),
            Smiley.make(":wahoo:", "http://forum-images.hardware.fr/icones/smilies/wahoo.gif"),
            Smiley.make(":kaola:", "http://forum-images.hardware.fr/icones/smilies/kaola.gif"),
            Smiley.make(":love:", "http://forum-images.hardware.fr/icones/smilies/love.gif"),
            Smiley.make(":heink:", "http://forum-images.hardware.fr/icones/smilies/heink.gif"),
            Smiley.make(":cry:", "http://forum-images.hardware.fr/icones/smilies/cry.gif"),
            Smiley.make(":whistle:", "http://forum-images.hardware.fr/icones/smilies/whistle.gif"),
            Smiley.make(":sol:", "http://forum-images.hardware.fr/icones/smilies/sol.gif"),
            Smiley.make(":pt1cable:", "http://forum-images.hardware.fr/icones/smilies/pt1cable.gif"),
            Smiley.make(":sleep:", "http://forum-images.hardware.fr/icones/smilies/sleep.gif"),
            Smiley.make(":sweat:", "http://forum-images.hardware.fr/icones/smilies/sweat.gif"),
            Smiley.make(":hello:", "http://forum-images.hardware.fr/icones/smilies/hello.gif"),
            Smiley.make(":na:", "http://forum-images.hardware.fr/icones/smilies/na.gif"),
            Smiley.make(":sarcastic:", "http://forum-images.hardware.fr/icones/smilies/sarcastic.gif")
    );

    public static List<Smiley> defaultSmileys() {
        return DEFAULT_SMILEYS;
    }
}
