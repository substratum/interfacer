package masquerade.substratum.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

class IconPackApplicator {

    private Context mContext;
    private String iconPackName;
    private String toast_text = null;
    private int delayOne, delayTwo;
    private Boolean bypass;

    private static void grantPermission(final String packager, final String permission) {
        Root.runCommand("pm grant " + packager + " " + permission);
    }

    void apply(Context mContext, String iconPackName, String delayOne, String delayTwo,
               Boolean bypass) {
        this.mContext = mContext;
        this.iconPackName = iconPackName;
        this.delayOne = Integer.parseInt(delayOne);
        this.delayTwo = Integer.parseInt(delayTwo);
        this.bypass = bypass;
        iconInjector();
    }

    private boolean checkChangeConfigurationPermissions() {
        String permission = "android.permission.CHANGE_CONFIGURATION";
        int res = mContext.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void iconInjector() {
        if (!checkChangeConfigurationPermissions()) {
            Log.e("Masquerade", "Masquerade was not granted " +
                    "CHANGE_CONFIGURATION permissions, allowing now...");
            grantPermission("masquerade.substratum",
                    "android.permission.CHANGE_CONFIGURATION");
        } else {
            Log.d("Masquerade",
                    "Masquerade already granted CHANGE_CONFIGURATION permissions!");
        }
        try {
            // Move home, since this is where we want our config code affected
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            mContext.startActivity(i);

            // Take a fragment of memory to remember what the user's default config is
            final Locale current_locale = mContext.getResources().getConfiguration().locale;
            Locale to_be_changed = Locale.JAPAN;
            // There are definitely Japanese locale users using our app, so we should take
            // account for these people and switch to Chinese for 2 seconds.
            if (current_locale == Locale.JAPAN) {
                to_be_changed = Locale.CHINA;
            }
            final Locale changer = to_be_changed;

            // Reflect back to framework and cause the language to change, we need this!
            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            final Object am = activityManagerNative.getMethod("getDefault").invoke
                    (activityManagerNative);
            final Object config = am.getClass().getMethod("getConfiguration").invoke(am);

            // Sniff Substratum's Resources
            try {
                Context otherContext = mContext.createPackageContext("projekt.substratum", 0);
                Resources resources = otherContext.getResources();
                if (bypass) {
                    int toast = resources.getIdentifier("studio_applied_toast", "string",
                            "projekt.substratum");
                    toast_text = String.format(resources.getString(toast), iconPackName);
                } else {
                    int toast = resources.getIdentifier("studio_configuration_changed", "string",
                            "projekt.substratum");
                    toast_text = resources.getString(toast);
                }
            } catch (Exception e) {
                // Suppress warning
            }

            // First window refresh to kick the change on the home screen
            final Handler handle = new Handler();
            handle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        config.getClass().getDeclaredField(
                                "locale").set(config, changer);
                        config.getClass().getDeclaredField(
                                "userSetLocale").setBoolean(config, true);

                        am.getClass().getMethod("updateConfiguration",
                                android.content.res.Configuration.class).invoke(am, config);

                        // Change the locale back to pre-icon pack application
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    config.getClass().getDeclaredField("locale").set(
                                            config, current_locale);
                                    config.getClass().getDeclaredField("userSetLocale")
                                            .setBoolean(config, true);

                                    am.getClass().getMethod("updateConfiguration",
                                            android.content.res.Configuration
                                                    .class).invoke(am, config);

                                    if (toast_text != null)
                                        Toast.makeText(
                                                mContext, toast_text, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    // Suppress warning
                                }
                            }
                        }, delayTwo); // 2 second delay for Home refresh
                    } catch (Exception e) {
                        // Suppress warning
                    }
                }
            }, delayOne); // 1 second delay for Home refresh
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}