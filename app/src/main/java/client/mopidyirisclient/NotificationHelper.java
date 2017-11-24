package client.mopidyirisclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.*;

import tech.gusavila92.websocketclient.WebSocketClient;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by jiri on 22.11.17.
 */

public class NotificationHelper {
    private static final String WEBSOCKET_TAG = "Websocket";
    private static final String BROWSER_NOTIFICATION = "browser_notification";
    private static MainActivity activity;

    public static MainActivity getActivity() {
        return activity;
    }

    public static void setActivity(MainActivity activity) {
        NotificationHelper.activity = activity;
    }
    public static void startWebsocket() {
        URI wsURI;
        try {
            wsURI = new URI(activity.getIrisUrl().replace("http","ws").concat("/ws"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        WebSocketClient webSocketClient = new WebSocketClient(wsURI) {
            @Override
            public void onOpen() {
                Log.d(WEBSOCKET_TAG, "Connection estabilished");
            }

            @Override
            public void onTextReceived(String message) {
                try {
                    JSONObject response = new JSONObject(message);
                    if (shouldInvokeNotification(response)) {
                        createNotification(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBinaryReceived(byte[] data) {

            }

            @Override
            public void onPingReceived(byte[] data) {

            }

            @Override
            public void onPongReceived(byte[] data) {

            }

            @Override
            public void onException(Exception e) {
                Log.e(WEBSOCKET_TAG, "An error has occured");
            }

            @Override
            public void onCloseReceived() {
                Log.d(WEBSOCKET_TAG, "Connection closed");
            }
        };

        webSocketClient.setConnectTimeout(100000);
        webSocketClient.setReadTimeout(600000);
        webSocketClient.addHeader("Origin", activity.getIrisUrl());
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    private static boolean shouldInvokeNotification(JSONObject response) throws JSONException {
        String type = response.getString("type");
        if (BROWSER_NOTIFICATION.equals(type)) {
            return true;
        }
        return false;
    }

    private static void createNotification(JSONObject message) throws JSONException {
        String title = message.getString("title");
        String body = message.getString("body");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(activity)
                        .setSmallIcon(R.drawable.ic_info_black_24dp)
                        .setContentTitle(title)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setTicker(title)
                        .setContentText(body);

        // Sets an ID for the notification
        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) activity.getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
