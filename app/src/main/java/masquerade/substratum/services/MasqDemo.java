
package masquerade.substratum.services;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class MasqDemo {
    public static final String MASQUERADE_TOKEN = "masquerade_token";
    public static final String PRIMARY_COMMAND_KEY = "primary_command_key";
    public static final String JOB_TIME_KEY = "job_time_key";
    public static final String INSTALL_LIST_KEY = "install_list";
    public static final String UNINSTALL_LIST_KEY = "uninstall_list";
    public static final String WITH_RESTART_UI_KEY = "with_restart_ui";
    public static final String BOOTANIMATION_PID_KEY = "bootanimation_pid";
    public static final String BOOTANIMATION_FILE_NAME = "bootanimation_file_name";
    public static final String FONTS_PID = "fonts_pid";
    public static final String FONTS_FILENAME = "fonts_filename";
    public static final String AUDIO_PID = "audio_pid";
    public static final String AUDIO_FILENAME = "audio_filename";
    public static final String COMMAND_VALUE_INSTALL = "install";
    public static final String COMMAND_VALUE_UNINSTALL = "uninstall";
    public static final String COMMAND_VALUE_RESTART_UI = "restart_ui";
    public static final String COMMAND_VALUE_CONFIGURATION_SHIM = "configuration_shim";
    public static final String COMMAND_VALUE_BOOTANIMATION = "bootanimation";
    public static final String COMMAND_VALUE_FONTS = "fonts";
    public static final String COMMAND_VALUE_AUDIO = "audio";
    public static final String INTENT_STATUS_CHANGED = "masquerade.substratum.STATUS_CHANGED";
    public static final String COMMAND_VALUE_JOB_COMPLETE = "job_complete";

    class MasqReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), INTENT_STATUS_CHANGED)) {
                String command = intent.getStringExtra(PRIMARY_COMMAND_KEY);
                if (TextUtils.equals(command, COMMAND_VALUE_FONTS)) {
                    // update ui, dismiss progress dialog, etc
                } else if (TextUtils.equals(command, COMMAND_VALUE_BOOTANIMATION)) {

                } else if (TextUtils.equals(command, COMMAND_VALUE_AUDIO)) {

                } else if (TextUtils.equals(command, COMMAND_VALUE_JOB_COMPLETE)) {

                }
            }
        }
    }

    // demo code for building a base intent for JobService commands
    public static Intent getMasqIntent(Context ctx) {
        Intent intent = new Intent();
        intent.setClassName("masquerade.substratum", "masquerade.substratum.services.JobService");
        // Credit StackOverflow http://stackoverflow.com/a/28132098
        // Use dummy PendingIntent for service to validate caller at onBind
        PendingIntent pending = PendingIntent.getActivity(ctx, 0, new Intent(), 0);
        intent.putExtra(MASQUERADE_TOKEN, pending);
        intent.putExtra(JOB_TIME_KEY, System.currentTimeMillis());
        return intent;
    }

    public static void install(Context context, ArrayList<String> overlay_apks) {
        // populate list however
        Intent masqIntent = getMasqIntent(context);
        masqIntent.putExtra(PRIMARY_COMMAND_KEY, COMMAND_VALUE_INSTALL);
        masqIntent.putExtra(INSTALL_LIST_KEY, overlay_apks);
        context.startService(masqIntent);
    }

    public static void uninstall(Context context, ArrayList<String> packages_to_remove) {
        Intent masqIntent = getMasqIntent(context);
        masqIntent.putExtra(PRIMARY_COMMAND_KEY, COMMAND_VALUE_UNINSTALL);
        masqIntent.putExtra(UNINSTALL_LIST_KEY, packages_to_remove);
        // only need to set if true, will restart SystemUI when done processing packages
        masqIntent.putExtra(WITH_RESTART_UI_KEY, true);
        context.startService(masqIntent);
    }

    public static void restartSystemUI(Context context) {
        Intent masqIntent = getMasqIntent(context);
        masqIntent.putExtra(PRIMARY_COMMAND_KEY, COMMAND_VALUE_RESTART_UI);
        context.startService(masqIntent);
    }

    public static void configurationChangeShim(Context context) {
        Intent masqIntent = getMasqIntent(context);
        masqIntent.putExtra(PRIMARY_COMMAND_KEY, COMMAND_VALUE_CONFIGURATION_SHIM);
        context.startService(masqIntent);
    }
    
    public static void clearThemedBootAnimation(Context context) {
        applyThemedBootAnimation(context, null);
    }
    
    public static void applyThemedBootAnimation(Context context, String fileName) {
        Intent masqIntent = getMasqIntent(context);
        masqIntent.putExtra(PRIMARY_COMMAND_KEY, COMMAND_VALUE_BOOTANIMATION);
        if (fileName != null) {
            masqIntent.putExtra(BOOTANIMATION_FILE_NAME, fileName);
        } else {
            // nothing. to reset to stock, just don't add PID and FILE
        }
        context.startService(masqIntent);
    }
    
    public static void clearThemedFont(Context context) {
        applyThemedFont(context, null, null);
    }

    public static void applyThemedFont(Context context, String pid, String fileName) {
        Intent masqIntent = getMasqIntent(context);
        masqIntent.putExtra(PRIMARY_COMMAND_KEY, COMMAND_VALUE_FONTS);
        if (pid != null) {
            masqIntent.putExtra(FONTS_PID, pid);
            masqIntent.putExtra(FONTS_FILENAME, fileName);
        }
        context.startService(masqIntent);
    }

    public static void clearThemedSounds(Context context) {
        applyThemedSounds(context, null, null);
    }

    public static void applyThemedSounds(Context context, String pid, String fileName) {
        Intent masqIntent = getMasqIntent(context);
        masqIntent.putExtra(PRIMARY_COMMAND_KEY, COMMAND_VALUE_AUDIO);
        if (pid != null) {
            masqIntent.putExtra(AUDIO_PID, pid);
            masqIntent.putExtra(AUDIO_FILENAME, fileName);
        }
        context.startService(masqIntent);
    }
}
