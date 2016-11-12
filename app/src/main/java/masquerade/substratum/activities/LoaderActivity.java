package masquerade.substratum.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import masquerade.substratum.util.Root;

public class LoaderActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Masquerade", "Masquerade is now securing superuser permissions!");
        Root.requestRootAccess();
        finish();
    }
}