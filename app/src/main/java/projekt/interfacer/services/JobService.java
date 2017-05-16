/*
 * Copyright (c) 2016-2017 Projekt Substratum
 * This file is part of Substratum.
 *
 * Substratum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Substratum is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Substratum.  If not, see <http://www.gnu.org/licenses/>.
 */

package projekt.interfacer.services;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import projekt.interfacer.utils.IOUtils;
import projekt.interfacer.utils.SoundUtils;
import projekt.substratum.IInterfacerInterface;

public class JobService extends Service {
    private static final String TAG = JobService.class.getSimpleName();
    private static final int BASE_API_VERSION = 1;
    private static final boolean DEBUG = true;
    private static final String INTENT_STATUS_CHANGED = "projekt.interfacer.STATUS_CHANGED";
    private static final String PRIMARY_COMMAND_KEY = "primary_command_key";
    private static final String COMMAND_VALUE_JOB_COMPLETE = "job_complete";
    private static final String COMMAND_VALUE_BOOTANIMATION = "bootanimation";
    private static final String COMMAND_VALUE_FONTS = "fonts";
    private static final String COMMAND_VALUE_AUDIO = "audio";
    private static final String INTERFACER_PACKAGE = "projekt.interfacer";
    private static final String SUBSTRATUM_PACKAGE = "projekt.substratum";
    private static final String[] AUTHORIZED_CALLERS = new String[]{
            INTERFACER_PACKAGE,
            SUBSTRATUM_PACKAGE,
    };
    private static final String INTENT_CALLER_AUTHORIZED = "projekt.interfacer.CALLER_AUTHORIZED";
    private static final Signature SUBSTRATUM_SIGNATURE = new Signature(""
            + "308202eb308201d3a003020102020411c02f2f300d06092a864886f70d01010b050030263124302206"
            + "03550403131b5375627374726174756d20446576656c6f706d656e74205465616d301e170d31363037"
            + "30333032333335385a170d3431303632373032333335385a3026312430220603550403131b53756273"
            + "74726174756d20446576656c6f706d656e74205465616d30820122300d06092a864886f70d01010105"
            + "000382010f003082010a02820101008855626336f645a335aa5d40938f15db911556385f72f72b5f8b"
            + "ad01339aaf82ae2d30302d3f2bba26126e8da8e76a834e9da200cdf66d1d5977c90a4e4172ce455704"
            + "a22bbe4a01b08478673b37d23c34c8ade3ec040a704da8570d0a17fce3c7397ea63ebcde3a2a3c7c5f"
            + "983a163e4cd5a1fc80c735808d014df54120e2e5708874739e22e5a22d50e1c454b2ae310b480825ab"
            + "3d877f675d6ac1293222602a53080f94e4a7f0692b627905f69d4f0bb1dfd647e281cc0695e0733fa3"
            + "efc57d88706d4426c4969aff7a177ac2d9634401913bb20a93b6efe60e790e06dad3493776c2c0878c"
            + "e82caababa183b494120edde3d823333efd464c8aea1f51f330203010001a321301f301d0603551d0e"
            + "04160414203ec8b075d1c9eb9d600100281c3924a831a46c300d06092a864886f70d01010b05000382"
            + "01010042d4bd26d535ce2bf0375446615ef5bf25973f61ecf955bdb543e4b6e6b5d026fdcab09fec09"
            + "c747fb26633c221df8e3d3d0fe39ce30ca0a31547e9ec693a0f2d83e26d231386ff45f8e4fd5c06095"
            + "8681f9d3bd6db5e940b1e4a0b424f5c463c79c5748a14a3a38da4dd7a5499dcc14a70ba82a50be5fe0"
            + "82890c89a27e56067d2eae952e0bcba4d6beb5359520845f1fdb7df99868786055555187ba46c69ee6"
            + "7fa2d2c79e74a364a8b3544997dc29cc625395e2f45bf8bdb2c9d8df0d5af1a59a58ad08b32cdbec38"
            + "19fa49201bb5b5aadeee8f2f096ac029055713b77054e8af07cd61fe97f7365d0aa92d570be98acb89"
            + "41b8a2b0053b54f18bfde092eb");
    private static final Signature[] AUTHORIZED_SIGNATURES = new Signature[]{
            SUBSTRATUM_SIGNATURE,
    };
    private static List<Sound> SOUNDS = Arrays.asList(
            new Sound(IOUtils.SYSTEM_THEME_UI_SOUNDS_PATH, "/SoundsCache/ui/", "Effect_Tick",
                    "Effect_Tick", RingtoneManager.TYPE_RINGTONE),
            new Sound(IOUtils.SYSTEM_THEME_UI_SOUNDS_PATH, "/SoundsCache/ui/", "lock_sound",
                    "Lock"),
            new Sound(IOUtils.SYSTEM_THEME_UI_SOUNDS_PATH, "/SoundsCache/ui/", "unlock_sound",
                    "Unlock"),
            new Sound(IOUtils.SYSTEM_THEME_UI_SOUNDS_PATH, "/SoundsCache/ui/",
                    "low_battery_sound", "LowBattery"),
            new Sound(IOUtils.SYSTEM_THEME_ALARM_PATH, "/SoundsCache/alarms/", "alarm", "alarm",
                    RingtoneManager.TYPE_ALARM),
            new Sound(IOUtils.SYSTEM_THEME_NOTIFICATION_PATH, "/SoundsCache/notifications/",
                    "notification", "notification", RingtoneManager.TYPE_NOTIFICATION),
            new Sound(IOUtils.SYSTEM_THEME_RINGTONE_PATH, "/SoundsCache/ringtones/", "ringtone",
                    "ringtone", RingtoneManager.TYPE_RINGTONE)
    );
    private static IOverlayManager mOMS;
    private static IPackageManager mPM;
    private static boolean isWaiting = false;
    private final IInterfacerInterface.Stub mBinder = new IInterfacerInterface.Stub() {
        @Override
        public void installPackage(List<String> paths) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            for (String path : paths) {
                log("Installer - installing \'" + path + "\'...");
                PackageInstallObserver observer = new PackageInstallObserver();
                install(path, observer);
            }
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void uninstallPackage(List<String> packages, boolean restartUi) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            for (String p : packages) {
                // TODO: Fix isOverlayEnabled function, for now it's causing NPE
                if (isOverlayEnabled(p)) {
                    log("Remover - disabling overlay for \'" + p + "\'...");
                    switchOverlay(p, false);
                }

                log("Remover - uninstalling \'" + p + "\'...");
                PackageDeleteObserver observer = new PackageDeleteObserver();
                uninstall(p, observer);
            }
            if (restartUi) restartUi();
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void restartSystemUI() {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            log("Restarting SystemUI...");
            restartUi();
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void configurationShim(int firstDelay, int secondDelay) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            new LocaleChanger(getSubsContext(), firstDelay, secondDelay).run();
        }

        @Override
        public void applyBootanimation(String name) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            if (name == null) {
                log("Restoring system boot animation...");
                clearBootAnimation();
            } else {
                log("Configuring themed boot animation...");
                copyBootAnimation(name);
            }
            informCompletion(COMMAND_VALUE_BOOTANIMATION);
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void applyFonts(String pid, String fileName) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            if (pid == null) {
                log("Restoring system font...");
                clearFonts();
            } else {
                log("Configuring theme font...");
                copyFonts(pid, fileName);
            }
            restartUi();
            informCompletion(COMMAND_VALUE_FONTS);
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void applyAudio(String pid, String fileName) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            if (pid == null) {
                log("Restoring system sounds...");
                clearSounds(JobService.this);
            } else {
                log("Configuring theme sounds...");
                applyThemedSounds(pid, fileName);
            }
            restartUi();
            informCompletion(COMMAND_VALUE_AUDIO);
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void enableOverlay(List<String> packages, boolean restartUi) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            for (String p : packages) {
                log("Enabler - enabling overlay for \'" + p + "\'...");
                switchOverlay(p, true);
            }
            if (restartUi) restartUi();
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void disableOverlay(List<String> packages, boolean restartUi) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            for (String p : packages) {
                log("Disabler - disabling overlay for \'" + p + "\'...");
                switchOverlay(p, false);
            }
            if (restartUi) restartUi();
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void changePriority(List<String> packages, boolean restartUi) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            log("PriorityJob - processing priority changes...");
            try {
                for (int i = 0; i < packages.size() - 1; i++) {
                    String parentName = packages.get(i);
                    String packageName = packages.get(i + 1);

                    getOMS().setPriority(packageName, parentName,
                            UserHandle.USER_SYSTEM);
                }
            } catch (RemoteException e) {
                // Exception
            }
            if (restartUi) restartUi();
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void copy(String source, String destination) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            log("CopyJob - copying \'" + source + "\' to \'" + destination +
                    "\'...");

            File sourceFile = new File(source);
            if (sourceFile.exists()) {
                if (sourceFile.isFile()) {
                    IOUtils.bufferedCopy(source, destination);
                } else {
                    IOUtils.copyFolder(source, destination);
                }
            } else {
                log("CopyJob - \'" + source + "\' does not exist, aborting...");
            }
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void move(String source, String destination) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            log("MoveJob - moving \'" + source + "\' to \'" + destination +
                    "\'...");

            File sourceFile = new File(source);
            if (sourceFile.exists()) {
                if (sourceFile.isFile()) {
                    IOUtils.bufferedCopy(source, destination);
                } else {
                    IOUtils.copyFolder(source, destination);
                }
                IOUtils.deleteRecursive(sourceFile);
            } else {
                log("MoveJob - \'" + source + "\' does not exist, aborting...");
            }
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void mkdir(String destination) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            log("MkdirJob - creating \'" + destination + "\'...");
            IOUtils.createDirIfNotExists(destination);
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void deleteDirectory(String directory, boolean withParent) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            if (withParent) {
                delete(directory);
            } else {
                for (File child : new File(directory).listFiles()) {
                    delete(child.getAbsolutePath());
                }
            }
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void applyProfile(List<String> enable, List<String> disable,
                                 String name, boolean restartUi) {
            // Verify caller identity
            if (!isCallerAuthorized(Binder.getCallingUid())) return;

            log("Applying profile...");
            boolean mRestartUi = restartUi;

            // Clear system theme folder content
            File themeDir = new File(IOUtils.SYSTEM_THEME_PATH);
            for (File f : themeDir.listFiles()) {
                IOUtils.deleteRecursive(f);
            }

            // Process theme folder
            File profileDir = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/substratum/profiles/" +
                    name + "/theme");

            if (profileDir.exists()) {
                File profileFonts = new File(profileDir, "fonts");
                if (profileFonts.exists()) {
                    IOUtils.copyFolder(profileFonts, new File(IOUtils.SYSTEM_THEME_FONT_PATH));
                    refreshFonts();
                    mRestartUi = true;
                } else {
                    clearFonts();
                }

                File profileSounds = new File(profileDir, "audio");
                if (profileSounds.exists()) {
                    IOUtils.copyFolder(profileSounds, new File(IOUtils.SYSTEM_THEME_AUDIO_PATH));
                    refreshSounds();
                    mRestartUi = true;
                } else {
                    clearSounds(JobService.this);
                }
            }

            // Disable all overlays installed
            for (String overlay : disable) {
                switchOverlay(overlay, false);
            }

            // Enable provided overlays
            for (String overlay : enable) {
                switchOverlay(overlay, true);
            }

            // Restart SystemUI when needed
            if (mRestartUi) {
                restartUi();
            }
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public int getVersion() {
            return BASE_API_VERSION;
        }

        @Override
        public boolean isAuthorised() {
            return isCallerAuthorized(Binder.getCallingUid());
        }

    };

    private static IOverlayManager getOMS() {
        if (mOMS == null) {
            mOMS = IOverlayManager.Stub.asInterface(
                    ServiceManager.getService("overlay"));
        }

        return mOMS;
    }

    private static IPackageManager getPM() {
        if (mPM == null) {
            mPM = IPackageManager.Stub.asInterface(
                    ServiceManager.getService("package"));
        }

        return mPM;
    }

    public static void log(String msg) {
        if (!DEBUG) {
            return;
        }
        // More -Wall than -Werror like
        Log.d(TAG, msg);
    }

    private static boolean isDebugBuild() {
        return Build.TYPE.equals("eng") || Build.TYPE.equals("userdebug");
    }

    private static boolean shouldCheckBuildType() {
        return SystemProperties.getBoolean("ro.interfacer.buildtype.check", false);
    }

    @Override
    public void onCreate() {
        // Needed here before any checks
        IOUtils.createThemeDirIfNotExists();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
    }

    private void informCompletion(String extra) {
        Intent intent = new Intent(INTENT_STATUS_CHANGED);
        intent.putExtra(PRIMARY_COMMAND_KEY, extra);
        sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    @SuppressWarnings("deprecation")
    private void install(String path, IPackageInstallObserver2 observer) {
        try {
            isWaiting = true;
            getPM().installPackageAsUser(path, observer,
                    PackageManager.INSTALL_REPLACE_EXISTING,
                    null,
                    UserHandle.USER_SYSTEM);
            while (isWaiting) {
                Thread.sleep(500);
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    @SuppressWarnings("deprecation")
    private void uninstall(String packageName, IPackageDeleteObserver observer) {
        try {
            isWaiting = true;
            getPM().deletePackageAsUser(packageName, observer, 0, UserHandle.USER_SYSTEM);
            while (isWaiting) {
                Thread.sleep(500);
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private void switchOverlay(String packageName, boolean enable) {
        try {
            getOMS().setEnabled(packageName, enable, UserHandle.USER_SYSTEM, false);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
    }

    private boolean isOverlayEnabled(String packageName) {
        boolean enabled = false;

        try {
            OverlayInfo info = getOMS().getOverlayInfo(packageName, UserHandle.USER_SYSTEM);
            if (info != null) {
                enabled = info.isEnabled();
            } else {
                log("OverlayInfo is null.");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }

        return enabled;
    }

    private void copyFonts(String pid, String zipFileName) {
        // Prepare local cache dir for font package assembly
        log("Copy Fonts - Package ID = " + pid + " filename = " + zipFileName);

        File cacheDir = new File(getCacheDir(), "/FontCache/");
        if (cacheDir.exists()) {
            IOUtils.deleteRecursive(cacheDir);
        }

        boolean created = cacheDir.mkdir();
        if (!created) {
            log("Could not create cache directory...");
        }

        // Copy system fonts into our cache dir
        IOUtils.copyFolder("/system/fonts", cacheDir.getAbsolutePath());

        // Append zip to filename since it is probably removed
        // for list presentation
        if (!zipFileName.endsWith(".zip")) {
            zipFileName = zipFileName + ".zip";
        }

        // Copy target themed fonts zip to our cache dir
        Context themeContext = getAppContext(pid);
        AssetManager am = themeContext.getAssets();
        try {
            InputStream inputStream = am.open("fonts/" + zipFileName);
            OutputStream outputStream = new FileOutputStream(getCacheDir()
                    .getAbsolutePath() + "/FontCache/" + zipFileName);
            IOUtils.bufferedCopy(inputStream, outputStream);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        // Unzip new fonts and delete zip file, overwriting any system fonts
        File fontZip = new File(getCacheDir(), "/FontCache/" + zipFileName);
        IOUtils.unzip(fontZip.getAbsolutePath(), cacheDir.getAbsolutePath());

        boolean deleted = fontZip.delete();
        if (!deleted) {
            log("Could not delete ZIP file...");
        }

        // Check if theme zip included a fonts.xml. If not, Substratum
        // is kind enough to provide one for us in it's assets
        try {
            File testConfig = new File(getCacheDir(), "/FontCache/" + "fonts.xml");
            if (!testConfig.exists()) {
                Context subContext = getSubsContext();
                AssetManager subsAm = subContext.getAssets();
                InputStream inputStream = subsAm.open("fonts.xml");
                OutputStream outputStream = new FileOutputStream(testConfig);
                IOUtils.bufferedCopy(inputStream, outputStream);
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        // Prepare system theme fonts folder and copy new fonts folder from our cache
        IOUtils.deleteThemedFonts();
        IOUtils.createFontDirIfNotExists();
        IOUtils.copyFolder(cacheDir.getAbsolutePath(), IOUtils.SYSTEM_THEME_FONT_PATH);

        // Let system know it's time for a font change
        refreshFonts();
    }

    private void clearFonts() {
        IOUtils.deleteThemedFonts();
        refreshFonts();
    }

    private void refreshFonts() {
        // Set permissions on font files and config xml
        File themeFonts = new File(IOUtils.SYSTEM_THEME_FONT_PATH);
        if (themeFonts.exists()) {
            // Set permissions
            IOUtils.setPermissionsRecursive(themeFonts,
                    FileUtils.S_IRWXU | FileUtils.S_IRGRP | FileUtils.S_IRWXO,
                    FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IROTH | FileUtils.S_IXOTH);
        }

        // Let system know it's time for a font change
        SystemProperties.set("sys.refresh_theme", "1");
        Typeface.recreateDefaults();
        float fontSize = Settings.System.getFloatForUser(getContentResolver(),
                Settings.System.FONT_SCALE, 1.0f, UserHandle.USER_CURRENT);
        Settings.System.putFloatForUser(getContentResolver(),
                Settings.System.FONT_SCALE, (fontSize + 0.0000001f), UserHandle.USER_CURRENT);
    }

    private void applyThemedSounds(String pid, String zipFileName) {
        // Prepare local cache dir for font package assembly
        log("CopySounds - Package ID = \'" + pid + "\'");
        log("CopySounds - File name = \'" + zipFileName + "\'");

        File cacheDir = new File(getCacheDir(), "/SoundsCache/");
        if (cacheDir.exists()) {
            IOUtils.deleteRecursive(cacheDir);
        }

        boolean created = cacheDir.mkdir();
        if (!created) {
            log("Could not create cache directory...");
        }

        // Append zip to filename since it is probably removed
        // for list presentation
        if (!zipFileName.endsWith(".zip")) {
            zipFileName = zipFileName + ".zip";
        }

        // Copy target themed sounds zip to our cache dir
        Context themeContext = getAppContext(pid);
        AssetManager am = themeContext.getAssets();
        try {
            InputStream inputStream = am.open("audio/" + zipFileName);
            OutputStream outputStream = new FileOutputStream(getCacheDir()
                    .getAbsolutePath() + "/SoundsCache/" + zipFileName);
            IOUtils.bufferedCopy(inputStream, outputStream);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        // Unzip new sounds and delete zip file
        File soundsZip = new File(getCacheDir(), "/SoundsCache/" + zipFileName);
        IOUtils.unzip(soundsZip.getAbsolutePath(), cacheDir.getAbsolutePath());

        boolean deleted = soundsZip.delete();
        if (!deleted) {
            log("Could not delete ZIP file...");
        }

        clearSounds(this);
        IOUtils.createAudioDirIfNotExists();

        for (Sound sound : SOUNDS) {
            File soundsCache = new File(getCacheDir(), sound.cachePath);

            if (!(soundsCache.exists() && soundsCache.isDirectory())) {
                continue;
            }

            IOUtils.createDirIfNotExists(sound.themePath);

            File mp3 = new File(getCacheDir(), sound.cachePath + sound.soundPath + ".mp3");
            File ogg = new File(getCacheDir(), sound.cachePath + sound.soundPath + ".ogg");
            if (ogg.exists()) {
                IOUtils.bufferedCopy(ogg,
                        new File(sound.themePath + File.separator + sound.soundPath + ".ogg"));
            } else if (mp3.exists()) {
                IOUtils.bufferedCopy(mp3,
                        new File(sound.themePath + File.separator + sound.soundPath + ".mp3"));
            }
        }

        // Let system know it's time for a sound change
        refreshSounds();
    }

    private void clearSounds(Context ctx) {
        IOUtils.deleteThemedAudio();
        SoundUtils.setDefaultAudible(ctx, RingtoneManager.TYPE_ALARM);
        SoundUtils.setDefaultAudible(ctx, RingtoneManager.TYPE_NOTIFICATION);
        SoundUtils.setDefaultAudible(ctx, RingtoneManager.TYPE_RINGTONE);
        SoundUtils.setDefaultUISounds(getContentResolver(), "lock_sound", "Lock.ogg");
        SoundUtils.setDefaultUISounds(getContentResolver(), "unlock_sound", "Unlock.ogg");
        SoundUtils.setDefaultUISounds(getContentResolver(), "low_battery_sound",
                "LowBattery.ogg");
    }

    private void refreshSounds() {
        File soundsDir = new File(IOUtils.SYSTEM_THEME_AUDIO_PATH);

        if (!soundsDir.exists()) {
            return;
        }

        // Set permissions
        IOUtils.setPermissionsRecursive(soundsDir,
                FileUtils.S_IRWXU | FileUtils.S_IRGRP | FileUtils.S_IRWXO,
                FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IROTH | FileUtils.S_IXOTH);

        int metaDataId = getSubsContext().getResources().getIdentifier(
                "content_resolver_notification_metadata",
                "string", SUBSTRATUM_PACKAGE);

        for (Sound sound : SOUNDS) {
            File themePath = new File(sound.themePath);

            if (!(themePath.exists() && themePath.isDirectory())) {
                continue;
            }

            File mp3 = new File(themePath, sound.soundPath + ".mp3");
            File ogg = new File(themePath, sound.soundPath + ".ogg");

            if (ogg.exists()) {
                if (sound.themePath.equals(IOUtils.SYSTEM_THEME_UI_SOUNDS_PATH) && sound.type !=
                        0) {
                    SoundUtils.setUIAudible(this, ogg, ogg, sound.type, sound.soundName);
                } else if (sound.themePath.equals(IOUtils.SYSTEM_THEME_UI_SOUNDS_PATH)) {
                    SoundUtils.setUISounds(getContentResolver(), sound.soundName, ogg
                            .getAbsolutePath());
                } else {
                    SoundUtils.setAudible(this, ogg, ogg, sound.type, getSubsContext().getString
                            (metaDataId));
                }
            } else if (mp3.exists()) {
                if (sound.themePath.equals(IOUtils.SYSTEM_THEME_UI_SOUNDS_PATH) && sound.type !=
                        0) {
                    SoundUtils.setUIAudible(this, mp3, mp3, sound.type, sound.soundName);
                } else if (sound.themePath.equals(IOUtils.SYSTEM_THEME_UI_SOUNDS_PATH)) {
                    SoundUtils.setUISounds(getContentResolver(), sound.soundName,
                            mp3.getAbsolutePath());
                } else {
                    SoundUtils.setAudible(this, mp3, mp3, sound.type, getSubsContext().getString
                            (metaDataId));
                }
            } else {
                if (sound.themePath.equals(IOUtils.SYSTEM_THEME_UI_SOUNDS_PATH)) {
                    SoundUtils.setDefaultUISounds(getContentResolver(), sound.soundName, sound
                            .soundPath + ".ogg");
                } else {
                    SoundUtils.setDefaultAudible(this, sound.type);
                }
            }
        }
    }

    private void copyBootAnimation(String fileName) {

        try {
            clearBootAnimation();

            File source = new File(fileName);
            File dest = new File(IOUtils.SYSTEM_THEME_BOOTANIMATION_PATH);

            IOUtils.bufferedCopy(source, dest);

            boolean deleted = source.delete();
            if (!deleted) {
                log("Could not delete source file...");
            }

            IOUtils.setPermissions(dest,
                    FileUtils.S_IRWXU | FileUtils.S_IRGRP | FileUtils.S_IROTH);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private void clearBootAnimation() {
        try {
            File f = new File(IOUtils.SYSTEM_THEME_BOOTANIMATION_PATH);
            if (f.exists()) {
                boolean deleted = f.delete();
                if (!deleted) {
                    log("Could not delete themed boot animation...");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    @SuppressWarnings({"unchecked", "ConfusingArgumentToVarargsMethod"})
    private void restartUi() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            Class ActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Method getDefault = ActivityManagerNative.getDeclaredMethod("getDefault", null);
            Object amn = getDefault.invoke(null, null);
            Method killApplicationProcess = amn.getClass().getDeclaredMethod
                    ("killApplicationProcess", String.class, int.class);

            stopService(new Intent().setComponent(new ComponentName("com.android.systemui", "com" +
                    ".android.systemui.SystemUIService")));
            am.killBackgroundProcesses("com.android.systemui");

            for (ActivityManager.RunningAppProcessInfo app : am.getRunningAppProcesses()) {
                if ("com.android.systemui".equals(app.processName)) {
                    killApplicationProcess.invoke(amn, app.processName, app.uid);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private Context getSubsContext() {
        return getAppContext(SUBSTRATUM_PACKAGE);
    }

    private Context getAppContext(String packageName) {
        Context ctx = null;

        try {
            ctx = getApplicationContext().createPackageContext(packageName,
                    Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "", e);
        }

        return ctx;
    }

    private boolean forceAuthorizePackages() {
        return Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FORCE_AUTHORIZE_SUBSTRATUM_PACKAGES, 0, UserHandle.USER_CURRENT)
                == 1;
    }

    private boolean doSignaturesMatch(String packageName, Signature signature) {
        if (packageName != null) {
            try {
                PackageInfo pi = getPM().getPackageInfo(packageName,
                        PackageManager.GET_SIGNATURES, UserHandle.USER_SYSTEM);
                if (pi.signatures != null
                        && pi.signatures.length == 1
                        && signature.equals(pi.signatures[0])) {
                    return true;
                }
            } catch (RemoteException ignored) {
                return false;
            }
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private boolean isCallerAuthorized(int uid) {
        String callingPackage = getPackageManager().getPackagesForUid(uid)[0];

        for (String AUTHORIZED_CALLER : AUTHORIZED_CALLERS) {
            if (TextUtils.equals(callingPackage, AUTHORIZED_CALLER)) {
                for (Signature AUTHORIZED_SIGNATURE : AUTHORIZED_SIGNATURES) {
                    if (doSignaturesMatch(callingPackage, AUTHORIZED_SIGNATURE)) {
                        log("\'" + callingPackage
                                + "\' is an authorized calling package...");
                        return true;
                    }
                }
            }
        }

        if (shouldCheckBuildType() && isDebugBuild()) {
            log("the ROM is a userdebug or eng build configured to allow all " +
                    "packages, validating calling package permissions...");
            return true;
        }

        if (forceAuthorizePackages()) {
            log("\'" + callingPackage + "\' is not an authorized calling " +
                    "package, but the user has explicitly allowed all calling" +
                    " packages, validating calling package permissions...");
            return true;
        }

        log("\'" + callingPackage + "\' is not an authorized calling package.");
        informCaller(false);
        return false;
    }

    private void informCaller(boolean result) {
        Intent intent = new Intent(INTENT_CALLER_AUTHORIZED);
        intent.putExtra("isCallerAuthorized", result);
        sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void delete(String mFileOrDirectory) {
        log("DeleteJob - deleting \'" + mFileOrDirectory + "\'...");

        File file = new File(mFileOrDirectory);
        if (file.exists()) {
            IOUtils.deleteRecursive(file);
        } else {
            log("DeleteJob - \'" + mFileOrDirectory + "\' is already deleted.");
        }
    }

    private static class Sound {
        String themePath;
        String cachePath;
        String soundName;
        String soundPath;
        int type;

        Sound(String themePath, String cachePath, String soundName, String soundPath) {
            this.themePath = themePath;
            this.cachePath = cachePath;
            this.soundName = soundName;
            this.soundPath = soundPath;
        }

        Sound(String themePath, String cachePath, String soundName, String soundPath, int type) {
            this.themePath = themePath;
            this.cachePath = cachePath;
            this.soundName = soundName;
            this.soundPath = soundPath;
            this.type = type;
        }
    }

    private class PackageInstallObserver extends IPackageInstallObserver2.Stub {
        public void onUserActionRequired(Intent intent) throws RemoteException {
            log("Installer - user action required callback");
            isWaiting = false;
        }

        public void onPackageInstalled(String packageName, int returnCode,
                                       String msg, Bundle extras) {
            log("Installer - successfully installed \'" + packageName + "\'!");
            isWaiting = false;
        }
    }

    private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(String packageName, int returnCode) {
            log("Remover - successfully removed \'" + packageName + "\'");
            isWaiting = false;
        }
    }

    private class LocaleChanger extends BroadcastReceiver implements Runnable {
        private boolean mIsRegistered;
        private Context mContext;
        private Handler mHandler;
        private Locale mCurrentLocale;
        private int mFirstDelay, mSecondDelay;

        public LocaleChanger(Context context, int firstDelay, int secondDelay) {
            mContext = context;
            mHandler = new Handler(Looper.getMainLooper());
            mFirstDelay = firstDelay;
            mSecondDelay = secondDelay;
        }

        @Override
        public void run() {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);

            mContext.startActivity(i);
            mHandler.postDelayed(this::spoofLocale, mFirstDelay);
        }

        private void register() {
            if (mIsRegistered) {
                return;
            }

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

            mContext.registerReceiver(LocaleChanger.this, filter);
            mIsRegistered = true;
        }

        private void unregister() {
            if (!mIsRegistered) {
                return;
            }

            mContext.unregisterReceiver(LocaleChanger.this);
            mIsRegistered = false;
        }

        @SuppressWarnings("deprecation")
        private void spoofLocale() {
            Configuration config;

            log("LocaleChanger - spoofing locale for configuration change shim...");

            try {
                register();

                config = ActivityManagerNative.getDefault().getConfiguration();
                mCurrentLocale = config.locale;

                Locale toSpoof = Locale.JAPAN;
                if (mCurrentLocale == Locale.JAPAN) {
                    toSpoof = Locale.CHINA;
                }

                config.setLocale(toSpoof);
                config.userSetLocale = true;
                ActivityManagerNative.getDefault().updateConfiguration(config);
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        }

        private void restoreLocale() {
            Configuration config;
            log("LocaleChanger - restoring original locale for configuration change shim...");

            try {
                unregister();

                config = ActivityManagerNative.getDefault().getConfiguration();
                config.setLocale(mCurrentLocale);
                config.userSetLocale = true;

                ActivityManagerNative.getDefault().updateConfiguration(config);
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                return;
            }
            informCompletion(COMMAND_VALUE_JOB_COMPLETE);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.postDelayed(this::restoreLocale, mSecondDelay);
        }
    }
}
