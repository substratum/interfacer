package projekt.substratum.helper.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Nicholas Chum (nicholaschum)
 */

public class BootCompletedDetector extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, Helper.class);
            context.startService(pushIntent);
        }
    }
}