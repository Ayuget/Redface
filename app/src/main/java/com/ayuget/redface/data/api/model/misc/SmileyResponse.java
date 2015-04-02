package com.ayuget.redface.data.api.model.misc;

import com.ayuget.redface.data.api.model.Smiley;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SmileyResponse {
    @SerializedName("stickers")
    private List<Smiley> smileys;

    public List<Smiley> getSmileys() {
        return smileys;
    }

    public void setSmileys(List<Smiley> smileys) {
        this.smileys = smileys;
    }
}

