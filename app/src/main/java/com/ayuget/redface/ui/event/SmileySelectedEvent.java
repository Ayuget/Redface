package com.ayuget.redface.ui.event;

public class SmileySelectedEvent {
    private final String smileyCode;

    public SmileySelectedEvent(String smileyCode) {
        this.smileyCode = smileyCode;
    }

    public String getSmileyCode() {
        return smileyCode;
    }
}
