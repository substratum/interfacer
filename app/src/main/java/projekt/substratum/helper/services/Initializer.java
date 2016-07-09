package projekt.substratum.helper.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import projekt.substratum.helper.util.Root;

/**
 * @author Nicholas Chum (nicholaschum)
 */

public class Initializer extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Root.requestRootAccess();
    }
}