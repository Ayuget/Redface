package com.ayuget.redface;

import android.test.InstrumentationTestCase;

import java.io.IOException;
import java.io.InputStream;

public class BaseTestCase extends InstrumentationTestCase {
    public String readAssetFile(String filepath) throws IOException {
        InputStream is = getInstrumentation().getContext().getResources().getAssets().open(filepath);
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
