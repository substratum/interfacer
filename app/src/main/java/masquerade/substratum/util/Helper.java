package masquerade.substratum.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

public class Helper extends BroadcastReceiver {

    private static final String SUBSTRATUM_PACKAGE = "projekt.substratum";
    private static final String MASQUERADE_TOKEN = "masquerade_token";
    private static final String[] AUTHORIZED_CALLERS = new String[]{
            SUBSTRATUM_PACKAGE,
            "masquerade.substratum"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isCallerAuthorized(intent)) {
            Log.d("Masquerade", "Caller not authorized");
            return;
        }
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
            new Uninstaller().uninstall(intent, "pm-uninstall", false,
                    intent.getBooleanExtra("restart_systemui", false));
        } else if (intent.getStringArrayListExtra("pm-uninstall-specific") != null) {
            new Uninstaller().uninstall(intent, "pm-uninstall-specific", true,
                    intent.getBooleanExtra("restart_systemui", false));
        } else if (intent.getStringArrayListExtra("icon-handler") != null) {
            String icon_pack_name = intent.getStringArrayListExtra("icon-handler").get(0);
            String main_delay = intent.getStringArrayListExtra("icon-handler").get(2);
            String delay_one = intent.getStringArrayListExtra("icon-handler").get(3);
            String delay_two = intent.getStringArrayListExtra("icon-handler").get(4);
            final String bypass = intent.getStringArrayListExtra("icon-handler").get(5);
            if (bypass == null) {
                if (intent.getStringArrayListExtra("icon-handler").get(1).contains("pm") ||
                        intent.getStringArrayListExtra("icon-handler").get(1).contains("om") ||
                        intent.getStringArrayListExtra("icon-handler").get(1).contains("overlay")) {
                    Log.d("Masquerade", "Running command: \"" +
                            intent.getStringArrayListExtra("icon-handler").get(1) + "\"");
                    Root.runCommand(intent.getStringArrayListExtra("icon-handler").get(1));
                }
            }
            final Context mContext = context;
            final String icon_pack = icon_pack_name;
            final String delay_one_time = delay_one;
            final String delay_two_time = delay_two;
            final Handler handle = new Handler();
            handle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new IconPackApplicator().apply(
                            mContext, icon_pack, delay_one_time, delay_two_time, bypass == null);
                }
            }, Integer.parseInt(main_delay));
        } else if (intent.getStringExtra("om-commands") != null) {
            if (intent.getStringExtra("om-commands").contains("pm") ||
                    intent.getStringExtra("om-commands").contains("om") ||
                    intent.getStringExtra("om-commands").contains("overlay")) {
                Log.d("Masquerade", "Running command: \"" +
                        intent.getStringExtra("om-commands") + "\"");
                Root.runCommand(intent.getStringExtra("om-commands"));
            }
        }
    }

    private boolean isCallerAuthorized(Intent intent) {
        PendingIntent token = null;
        try {
            token = intent.getParcelableExtra(MASQUERADE_TOKEN);
        } catch (Exception e) {
            Log.d("Masquerade", "Attempt to start service without a token, unauthorized");
        }
        if (token == null) {
            return false;
        }
        // SECOND: we got a token, validate originating package
        // if not in our white list, return null
        String callingPackage = token.getCreatorPackage();
        boolean isValidPackage = false;
        for (int i = 0; i < AUTHORIZED_CALLERS.length; i++) {
            if (TextUtils.equals(callingPackage, AUTHORIZED_CALLERS[i])) {
                Log.d("Masquerade", callingPackage
                        + " is an authorized calling package, next validate calling package perms");
                isValidPackage = true;
                break;
            }
        }
        if (!isValidPackage) {
            Log.d("Masquerade", callingPackage
                    + " is not an authorized calling package");
            return false;
        }
        return true;
    }
}