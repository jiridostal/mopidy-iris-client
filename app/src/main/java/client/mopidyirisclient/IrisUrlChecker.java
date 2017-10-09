package client.mopidyirisclient;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jiri on 10.10.17.
 */

class IrisUrlChecker extends AsyncTask<String, Void, Boolean> {
    private Boolean result = false;
    private final MainActivity activity;
    private final ProgressDialog dialog;
    public IrisUrlChecker(MainActivity activity) {
        this.activity = activity;
        dialog = new ProgressDialog(activity);
    }
    protected void onPreExecute() {
        this.dialog.setMessage("Looking for Iris on " + activity.getIrisUrl());
        this.dialog.show();
    }
    @Override
    protected void onPostExecute(final Boolean success) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (!result) {
            activity.findViewById(R.id.errorPanel).setVisibility(View.VISIBLE);
        }
        else {
            activity.findViewById(R.id.errorPanel).setVisibility(View.GONE);
            activity.getWeb().loadUrl(activity.getIrisUrl());
        }
    }
    @Override
    protected Boolean doInBackground(String... url) {
        URL connUrl;
        try {
            connUrl = new URL(activity.getIrisUrl());
            HttpURLConnection conn = (HttpURLConnection) connUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            Log.d("sad",String.valueOf(conn.getResponseCode()));
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                result = false;
            }
            else {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                String irisTitle = "<title>Iris</title>";
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.trim().equals(irisTitle)) {
                        result = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
