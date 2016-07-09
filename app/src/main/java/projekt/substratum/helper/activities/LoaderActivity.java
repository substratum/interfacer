package projekt.substratum.helper.activities;

import android.app.Activity;
import android.os.Bundle;

import projekt.substratum.helper.util.Root;

/**
 * @author Nicholas Chum (nicholaschum)
 */

public class LoaderActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        Root.requestRootAccess();
    }
}