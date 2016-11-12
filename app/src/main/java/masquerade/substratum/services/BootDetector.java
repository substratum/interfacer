package masquerade.substratum.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import masquerade.substratum.util.Helper;

public class BootDetector extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent pushIntent = new Intent(context, Helper.class);
        context.startService(pushIntent);
    }
}