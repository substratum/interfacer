package masquerade.substratum.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Nicholas Chum (nicholaschum)
 */

public class Helper extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Masquerade",
                "BroadcastReceiver has accepted Substratum's commands and is running now...");
        Root.requestRootAccess();

        if (intent.getStringExtra("substratum-check") != null) {
            if (intent.getStringExtra("substratum-check").equals("masquerade-ball")) {
                Intent runCommand = new Intent();
                runCommand.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                runCommand.setAction("projekt.substratum.MASQUERADE_BALL");
                runCommand.putExtra("substratum-check", "masquerade-ball");
                context.sendBroadcast(runCommand);
                Log.d("Masquerade", "BroadcastReceiver was triggered to check for system " +
                        "integrity and service activation.");
            }
        } else if (intent.getStringArrayListExtra("pm-uninstall") != null) {
            new Uninstaller().Uninstaller(intent, "pm-uninstall", false,
                    intent.getBooleanExtra("restart_systemui", false));
        } else if (intent.getStringArrayListExtra("pm-uninstall-specific") != null) {
            new Uninstaller().Uninstaller(intent, "pm-uninstall-specific", true,
                    intent.getBooleanExtra("restart_systemui", false));
        } else if (intent.getStringExtra("om-commands") != null) {
            Log.d("Masquerade", "Running command: \"" +
                    intent.getStringExtra("om-commands") + "\"");
            Root.runCommand(intent.getStringExtra("om-commands"));
        }
    }
}