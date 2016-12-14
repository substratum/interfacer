package masquerade.substratum.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class UninstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
            Uri packageName = intent.getData();
            if (packageName.toString().substring(8).equals("projekt.substratum")) {
//                new performUninstalls().execute("");
            }
        }
    }
/*
    public class performUninstalls extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            // Substratum was uninstalled, uninstall all remaining overlays
            Log.d("Masquerade",
                    "Substratum was uninstalled, so all remaining overlays will be removed...");
            String[] state4 = {Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/.substratum/current_overlays.xml", "4"};
            String[] state5 = {Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/.substratum/current_overlays.xml", "5"};
            List<String> state5overlays = ReadOverlaysFile.main(state5);
            ArrayList<String> all_overlays = new ArrayList<>(ReadOverlaysFile.main(state4));
            all_overlays.addAll(state5overlays);
            for (int i = 0; i < all_overlays.size(); i++) {
                Log.d("Masquerade", "Uninstalling overlay: " + all_overlays.get(i));
                Root.runCommand("pm uninstall " + all_overlays.get(i));
            }
            return null;
        }
    }
    */
}