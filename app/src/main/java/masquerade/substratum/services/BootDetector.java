package masquerade.substratum.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Nicholas Chum (nicholaschum)
 */

public class BootDetector extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, Helper.class);
            context.startService(pushIntent);
        }
    }
}