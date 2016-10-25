package com.ayuget.redface.image.rehost;


import android.os.Build;

import com.ayuget.redface.BaseTestCase;
import com.ayuget.redface.BuildConfig;
import com.ayuget.redface.image.HostedImage;
import com.ayuget.redface.image.ImageQuality;

import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
public class RehostResultParserTest extends BaseTestCase {
    @Test
    public void testParseResultPage() throws Exception {
        RehostResultParser rehostResultParser = new RehostResultParser();
        HostedImage hostedImage = rehostResultParser.parseResultPage(readAssetFile("hfr_rehost_response_page.html"));

        assertThat(hostedImage.url()).isEqualTo("http://reho.st/self/7d2b8d06572ae139d5cb0de7480920d207fd9d61.jpg");
        assertThat(hostedImage.variants()).containsOnly(
                MapEntry.entry(ImageQuality.ORIGINAL, "http://reho.st/self/7d2b8d06572ae139d5cb0de7480920d207fd9d61.jpg"),
                MapEntry.entry(ImageQuality.MEDIUM, "http://reho.st/medium/self/7d2b8d06572ae139d5cb0de7480920d207fd9d61.jpg"),
                MapEntry.entry(ImageQuality.PREVIEW, "http://reho.st/preview/self/7d2b8d06572ae139d5cb0de7480920d207fd9d61.jpg"),
                MapEntry.entry(ImageQuality.THUMBNAIL, "http://reho.st/thumb/self/7d2b8d06572ae139d5cb0de7480920d207fd9d61.jpg")
        );
    }
}
