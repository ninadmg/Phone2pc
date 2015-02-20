package link.bleed.app.Network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import link.bleed.app.Constants;
import link.bleed.app.Models.FileToUpload;
import link.bleed.app.Models.NameValue;
import link.bleed.app.R;
import link.bleed.app.Utils.ImageResizer;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;


public class PostService extends IntentService {

    private static final String ACTION_TEXT = "link.bleed.p2c.action.TEXT";
    private static final String ACTION_IMAGE = "link.bleed.p2c.action.IMAGE";


    private static final String QRCODE = "link.bleed.p2c.extra.QRcode";
    private static final String EXTRA_URL = "link.bleed.p2c.extra.URL";

    HubConnection conn;
    HubProxy hubProxy;
    Logger logger;
    Intent mIntent;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notification;
    private PowerManager.WakeLock wakeLock;


    public static void startActionText(Context context, String param1, String param2) {
        Intent intent = new Intent(context, PostService.class);
        intent.setAction(ACTION_TEXT);
        intent.putExtra(QRCODE, param1);
        intent.putExtra(EXTRA_URL, param2);
        context.startService(intent);
    }


    public static void startActionImage(Context context, String param1, String param2) {
        Intent intent = new Intent(context, PostService.class);
        intent.setAction(ACTION_IMAGE);
        intent.putExtra(QRCODE, param1);
        intent.putExtra(EXTRA_URL, param2);
        context.startService(intent);
    }

    public PostService() {
        super("PostService");
        logger = new Logger() {

            @Override
            public void log(String message, LogLevel level) {
//                Log.e("bleed",message);
            }
        };

        conn = new HubConnection(Constants.URL+"bleed","",true,logger);
//        conn = new HubConnection(" http://192.168.2.101/TakeMeThere/bleed","",true,logger);

    }

    @Override
    protected void onHandleIntent( Intent intent) {
        if (intent != null) {
            mIntent = intent;
            makeConnection();

        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(this);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bleed");
        wakeLock.acquire();

    }

    private void makeConnection()
    {
        hubProxy = conn.createHubProxy("bleed");
        conn.start().done(new Action<Void>() {
            @Override
            public void run(Void obj) throws Exception {
                sendShare();
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                Log.e("bleed",error.getMessage());
            }
        });

    }

    private void sendShare()
    {
        String action = mIntent.getAction();
        if (ACTION_TEXT.equals(action)) {
            final String param1 = mIntent.getStringExtra(QRCODE);
            final String param2 = mIntent.getStringExtra(EXTRA_URL);
            handleActionText(param1, param2);
        } else if (ACTION_IMAGE.equals(action)) {
            final String param1 = mIntent.getStringExtra(QRCODE);
            final String param2 = mIntent.getStringExtra(EXTRA_URL);
            handleActionImage(param1, param2);
        }
    }
    private void handleActionText(String param1, String param2) {

        Pattern pattern = Pattern.compile("/((([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[-;:&=\\+\\$,\\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)((?:\\/[\\+~%\\" +
                "/._]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[\\w]*))?)/");
        if(!pattern.matcher(param2).matches()) {
            hubProxy.invoke("Share", param1, "text/uri", param2);
        }
        else
        {
            hubProxy.invoke("Share", param1, "text/plain", param2);
        }
        wakeLock.release();
    }

    private void handleActionImage(final String param1, final String ImageUrl) {

        FileToUpload file = new FileToUpload(ImageUrl,"imgi","content","Image");
        hubProxy.invoke(String.class,"GetFileKey",file.length()).done(new Action<String>() {
            @Override
            public void run(String obj) throws Exception {

                UploadThubnail(param1, ImageUrl, obj);
                uploadImage(param1, ImageUrl, obj);

            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                Log.e("ninadlog","error invoke return "+error.getMessage());
            }
        });
    }


    private void uploadImage(final String qrCode, String ImageUrl, final String storeKey)
    {

        ArrayList<NameValue> requestHeaders = new ArrayList<NameValue>();
        FileToUpload file = new FileToUpload(ImageUrl,"imgi","content","Image");
        ArrayList<FileToUpload> filesToUpload = new ArrayList<FileToUpload>();
        filesToUpload.add(file);
        ArrayList<NameValue> requestParameters = new ArrayList<NameValue>();
        requestParameters.add(new NameValue("clientId", conn.getConnectionId()));
        requestParameters.add(new NameValue("isLite","false"));
        requestParameters.add(new NameValue("storeKey",storeKey));

        try {
            new ImageUpload().handleFileUpload("1001",Constants.URL+"home/payload","POST"
                    ,filesToUpload,requestHeaders,requestParameters,new UploadProgressListener(){

                @Override
                public void uploadStarted() {
                    createNotification();
                }

                @Override
                public void uploadUpdate(int percentage) {
                    updateNotificationProgress(percentage);
                }

                @Override
                public void uploadComplete() {
                    updateNotificationCompleted();
                    completeImageShare(storeKey,qrCode);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void UploadThubnail(final String qrCode, String ImageUrl, final String storeKey)
    {
        ArrayList<NameValue> requestHeaders = new ArrayList<NameValue>();
        FileToUpload file = new FileToUpload(ImageResizer.getResizedImage(ImageUrl,true),"imgi","content","Image");
        ArrayList<FileToUpload> filesToUpload = new ArrayList<FileToUpload>();
        filesToUpload.add(file);
        ArrayList<NameValue> requestParameters = new ArrayList<NameValue>();
        requestParameters.add(new NameValue("clientId", conn.getConnectionId()));
        requestParameters.add(new NameValue("isLite","true"));
        requestParameters.add(new NameValue("storeKey",storeKey));


        try {
            new ImageUpload().handleFileUpload("1002" , Constants.URL + "home/payload" , "POST"
                    , filesToUpload, requestHeaders, requestParameters, new UploadProgressListener() {
                @Override
                public void uploadStarted() {

                }

                @Override
                public void uploadUpdate(int percentage) {

                }

                @Override
                public void uploadComplete() {
                    hubProxy.invoke("Share",qrCode,"liteimage/jpeg",storeKey).onError(new ErrorCallback() {
                        @Override
                        public void onError(Throwable error) {
                            Log.e("ninadlog","error share return "+error.getMessage());
                        }
                    });
//                    completeImageShare(storeKey,qrCode);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void completeImageShare(String fileKey,String param1)
    {
        hubProxy.invoke("Share",param1,"image/jpeg",fileKey).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                Log.e("ninadlog","error share return "+error.getMessage());
            }
        });
        if(wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

     private void createNotification() {
        notification.setContentTitle("Bleed").setContentText("Uploading")
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.ic_stat_notifi).
                setProgress(100, 0, true).setOngoing(true);

        startForeground(1,notification.build() );
        notificationManager.notify(1,notification.build());
    }

    private void updateNotificationProgress(final int progress) {
        notification.setContentTitle("Bleed").setContentText("Uploading")
                .setSmallIcon(R.drawable.ic_stat_notifi).setProgress(100, progress, false)
                .setOngoing(true);

        startForeground(1, notification.build());
        notificationManager.notify(1,notification.build());
    }

    private void updateNotificationCompleted() {
        stopForeground(true);
        if(wakeLock.isHeld()) {
            wakeLock.release();
        }
        notificationManager.cancel(1);

    }




}
