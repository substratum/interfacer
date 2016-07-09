package projekt.substratum.helper.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import projekt.substratum.helper.util.Root;

/**
 * @author Nicholas Chum (nicholaschum)
 */

public class Helper extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Root.requestRootAccess();
        Root.runCommand(intent.getStringExtra("om-commands"));
    }
}