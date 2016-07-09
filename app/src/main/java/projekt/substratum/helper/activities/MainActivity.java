package projekt.substratum.helper.activities;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import projekt.substratum.helper.util.Root;

/**
 * @author Nicholas Chum (nicholaschum)
 */

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager p = getPackageManager();
        p.setComponentEnabledSetting(
                getComponentName(),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        finish();
        Root.requestRootAccess();
    }
}