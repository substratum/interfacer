/*
 * Copyright (c) 2017 Project Substratum
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * Also add information on how to contact you by electronic and paper mail.
 *
 */

package masquerade.substratum.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

public class SoundUtils {
    private static final String SYSTEM_MEDIA_PATH = "/system/media/audio";
    private static final String SYSTEM_ALARMS_PATH =
            SYSTEM_MEDIA_PATH + File.separator + "alarms";
    private static final String SYSTEM_RINGTONES_PATH =
            SYSTEM_MEDIA_PATH + File.separator + "ringtones";
    private static final String SYSTEM_NOTIFICATIONS_PATH =
            SYSTEM_MEDIA_PATH + File.separator + "notifications";
    private static final String MEDIA_CONTENT_URI = "content://media/internal/audio/media";

    private static void updateGlobalSettings(ContentResolver resolver, String uri, String val) {
        Settings.Global.putStringForUser(resolver, uri, val, UserHandle.USER_SYSTEM);
    }

    public static boolean setUISounds(ContentResolver resolver, String sound_name, String
            location) {
        if (allowedUISound(sound_name)) {
            updateGlobalSettings(resolver, sound_name, location);
            return true;
        }
        return false;
    }

    public static void setDefaultUISounds(ContentResolver resolver, String sound_name,
                                          String sound_file) {
        updateGlobalSettings(resolver, sound_name, "/system/media/audio/ui/" + sound_file);
    }

    // This string array contains all the SystemUI acceptable sound files
    private static Boolean allowedUISound(String targetValue) {
        String[] allowed_themable = {
                "lock_sound",
                "unlock_sound",
                "low_battery_sound"
        };
        return Arrays.asList(allowed_themable).contains(targetValue);
    }

    private static String getDefaultAudiblePath(int type) {
        final String name;
        final String path;
        switch (type) {
            case RingtoneManager.TYPE_ALARM:
                name = SystemProperties.get("ro.config.alarm_alert");
                path = name != null ? SYSTEM_ALARMS_PATH + File.separator + name : null;
                break;
            case RingtoneManager.TYPE_NOTIFICATION:
                name = SystemProperties.get("ro.config.notification_sound");
                path = name != null ? SYSTEM_NOTIFICATIONS_PATH + File.separator + name : null;
                break;
            case RingtoneManager.TYPE_RINGTONE:
                name = SystemProperties.get("ro.config.ringtone");
                path = name != null ? SYSTEM_RINGTONES_PATH + File.separator + name : null;
                break;
            default:
                path = null;
                break;
        }
        return path;
    }

    public static boolean setAudible(Context context, File ringtone, File ringtoneCache, int type,
                                     String name) {
        final String path = ringtone.getAbsolutePath();
        final String mimeType = name.endsWith(".ogg") ? "application/ogg" : "application/mp3";
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, path);
        values.put(MediaStore.MediaColumns.TITLE, name);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.SIZE, ringtoneCache.length());
        values.put(MediaStore.Audio.Media.IS_RINGTONE, type == RingtoneManager.TYPE_RINGTONE);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION,
                type == RingtoneManager.TYPE_NOTIFICATION);
        values.put(MediaStore.Audio.Media.IS_ALARM, type == RingtoneManager.TYPE_ALARM);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
        Uri newUri = null;
        Cursor c = context.getContentResolver().query(uri,
                new String[]{
                        MediaStore.MediaColumns._ID
                },
                MediaStore.MediaColumns.DATA + "='" + path + "'",
                null, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            long id = c.getLong(0);
            c.close();
            newUri = Uri.withAppendedPath(Uri.parse(MEDIA_CONTENT_URI), "" + id);
            context.getContentResolver().update(uri, values,
                    MediaStore.MediaColumns._ID + "=" + id, null);
        }
        if (newUri == null)
            newUri = context.getContentResolver().insert(uri, values);
        try {
            RingtoneManager.setActualDefaultRingtoneUri(context, type, newUri);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean setUIAudible(Context context, File localized_ringtone,
                                       File ringtone_file, int type, String name) {
        final String path = ringtone_file.getAbsolutePath();

        final String path_clone = "/system/media/audio/ui/" + name + ".ogg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, path);
        values.put(MediaStore.MediaColumns.TITLE, name);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/ogg");
        values.put(MediaStore.MediaColumns.SIZE, localized_ringtone.length());
        values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
        Uri newUri = null;
        Cursor c = context.getContentResolver().query(uri,
                new String[]{
                        MediaStore.MediaColumns._ID
                },
                MediaStore.MediaColumns.DATA + "='" + path_clone + "'",
                null, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            long id = c.getLong(0);
            Log.e("ContentResolver", id + "");
            c.close();
            newUri = Uri.withAppendedPath(Uri.parse(MEDIA_CONTENT_URI), "" + id);
            try {
                context.getContentResolver().update(uri, values,
                        MediaStore.MediaColumns._ID + "=" + id, null);
            } catch (Exception e) {
                Log.d("SoundsHandler", "The content provider does not need to be updated.");
            }
        }
        if (newUri == null)
            newUri = context.getContentResolver().insert(uri, values);
        try {
            RingtoneManager.setActualDefaultRingtoneUri(context, type, newUri);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean setDefaultAudible(Context context, int type) {
        final String audiblePath = getDefaultAudiblePath(type);
        if (audiblePath != null) {
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(audiblePath);
            Cursor c = context.getContentResolver().query(uri,
                    new String[]{
                            MediaStore.MediaColumns._ID
                    },
                    MediaStore.MediaColumns.DATA + "='" + audiblePath + "'",
                    null, null);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                long id = c.getLong(0);
                c.close();
                uri = Uri.withAppendedPath(
                        Uri.parse(MEDIA_CONTENT_URI), "" + id);
            }
            if (uri != null)
                RingtoneManager.setActualDefaultRingtoneUri(context, type, uri);
        } else {
            return false;
        }
        return true;
    }
}
