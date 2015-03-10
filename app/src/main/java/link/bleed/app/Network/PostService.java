package link.bleed.app.Network;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import link.bleed.app.Constants;
import link.bleed.app.Models.FileToUpload;
import link.bleed.app.Models.ImageMap;
import link.bleed.app.Models.NameValue;
import link.bleed.app.R;
import link.bleed.app.Utils.ImageResizer;
import link.bleed.app.Utils.LogUtils;
import link.bleed.app.Utils.SharePrefUtils;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.LongPollingTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;


public class PostService extends Service {

    private static final String ACTION_TEXT = "link.bleed.p2c.action.text";
    private static final String ACTION_IMAGE = "link.bleed.p2c.action.image";
    private static final String ACTION_IMAGE_SHARE= "link.bleed.p2c.action.imageShare";

    private static final String QRCODE = "link.bleed.p2c.extra.QRcode";
    private static final String EXTRA_URL = "link.bleed.p2c.extra.URL";

    private long WAKELOCKTIMEOUT = 1000*60*2;

    private HubConnection conn;
    private HubProxy hubProxy;
    private Logger logger;
    private Intent mIntent;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notification;
    private PowerManager.WakeLock wakeLock;
    private final IBinder mBinder = new LocalBinder();

    public static void startActionText(Context context, String qrcode, String param2) {
        Intent intent = new Intent(context, PostService.class);
        intent.setAction(ACTION_TEXT);
        intent.putExtra(QRCODE, qrcode);
        intent.putExtra(EXTRA_URL, param2);
        context.startService(intent);
    }


    public static void startActionImage(Context context, String qrcode, String param2) {
        Intent intent = new Intent(context, PostService.class);
        intent.setAction(ACTION_IMAGE);
        intent.putExtra(QRCODE, qrcode);
        intent.putExtra(EXTRA_URL, param2);
        context.startService(intent);
    }

    public static void startActionImageShare(Context context, String qrcode, String Sharecode)
    {
        Intent intent = new Intent(context, PostService.class);
        intent.setAction(ACTION_IMAGE_SHARE);
        intent.putExtra(QRCODE, qrcode);
        intent.putExtra(EXTRA_URL, Sharecode);
        context.startService(intent);
    }

    public PostService() {
        super();
        logger = new Logger() {

            @Override
            public void log(String message, LogLevel level) {
//                Log.e("bleed",message);
            }
        };

        conn = new HubConnection(Constants.URL+"bleed","",true,logger);


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            mIntent = intent;
            makeConnection(new Connected() {
                @Override
                public void onConnected() {
                    sendShare();
                }
            });
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(this);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bleed");
        getWakelock();

    }

    private void getWakelock()
    {
        wakeLock.acquire(WAKELOCKTIMEOUT);

    }

    private void releaseWakelock()
    {
        if(wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
       public PostService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PostService.this;
        }
    }

    private void makeConnection(final Connected connected)
    {
        if(hubProxy==null)
        {
            LogUtils.LOGD("ninad","creating hub proxy");
            hubProxy = conn.createHubProxy("bleedv2");
        }
        if(conn.getState()!= ConnectionState.Connected) {
            LogUtils.LOGD("ninad","connecting client");
            ClientTransport transport=resolveTransport();
            conn.start(transport).done(new Action<Void>() {
                @Override
                public void run(Void obj) throws Exception {
                    registerClient(connected);

                }
            }).onError(new ErrorCallback() {
                @Override
                public void onError(Throwable error) {
                    Log.e("bleed", error.getMessage());
                }
            });
        }
        else
        {
            LogUtils.LOGD("ninad","register client");
            registerClient(connected);
        }
    }


    private void registerClient(final Connected connected)
    {
        String id = getClientId();
        hubProxy.invoke(String.class,"Register",id).done(new Action<String>() {
            @Override
            public void run(String obj) throws Exception {
                setClientId(obj);
                connected.onConnected();
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable error) {
                Log.e("ninadlog", "error invoke Register " + error.getMessage());
            }
        });;
    }

    private String getClientId()
    {
       return SharePrefUtils.getClientId(this);
    }

    private void setClientId(String id)
    {
        SharePrefUtils.setClientID(this,id);
    }


    private void sendShare()
    {
        String action = mIntent.getAction();
        if (ACTION_TEXT.equals(action)) {
            final String qrcode = mIntent.getStringExtra(QRCODE);
            final String param2 = mIntent.getStringExtra(EXTRA_URL);
            handleActionText(qrcode, param2);
        } else if (ACTION_IMAGE.equals(action)) {
            final String qrcode = mIntent.getStringExtra(QRCODE);
            final String param2 = mIntent.getStringExtra(EXTRA_URL);
            handleActionImage(qrcode, param2);
        }
        else if (ACTION_IMAGE_SHARE.equals(action))
        {
            final String qrcode = mIntent.getStringExtra(QRCODE);
            final String Sharecode = mIntent.getStringExtra(EXTRA_URL);
            handleActionImageShare(qrcode, Sharecode);
        }
    }

    public void handleActionImageShare(final String qrcode, final String sharecode) {
        if(hubProxy!=null) {
            hubProxy.invoke("Share", qrcode, "image/jpeg", sharecode);
            LogUtils.LOGD("hubcall", "sharecalled sharecode is " + sharecode);
        }else
        {

            makeConnection(new Connected() {
                @Override
                public void onConnected() {
                    handleActionImageShare(qrcode, sharecode);
                }
            });

        }
    }

    private void handleActionText(String param1, String param2) {

//        Pattern pattern = Pattern.compile("/((([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[-;:&=\\+\\$,\\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)((?:\\/[\\+~%\\" +
//                "/._]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[\\w]*))?)/");

        if(isValidUrl(param2)) {
            hubProxy.invoke("Share", param1, "text/uri", param2).done(new Action<Void>() {
                @Override
                public void run(Void obj) throws Exception {
                    LogUtils.LOGD("hubcall", "share done" );
                }
            }).onError(new ErrorCallback() {
                @Override
                public void onError(Throwable error) {
                    LogUtils.LOGD("hubcall", "share error" + error.getMessage());
                }
            });
        }
        else
        {
            hubProxy.invoke("Share", param1, "text/plain", param2);
        }
        releaseWakelock();
    }


    private boolean isValidUrl(String Url)
    {
        boolean isValid;
        try {
            new URL(Url);
            isValid=true;
        }catch (MalformedURLException e)
        {
            isValid=false;
        }
        return isValid;
    }

    public void handleActionImage(final String param1, final String ImageUrl) {

        if(hubProxy!=null) {
            FileToUpload file = new FileToUpload(ImageUrl, "imgi", "content", "Image");
            hubProxy.invoke(String.class, "GetFileKey", file.length()).done(new Action<String>() {
                @Override
                public void run(String obj) throws Exception {

                    ImageMap.getInstance().setUploadStarted(ImageUrl);
                    UploadThubnail(param1, ImageUrl, obj);
                    uploadImage(param1, ImageUrl, obj);


                }
            }).onError(new ErrorCallback() {
                @Override
                public void onError(Throwable error) {
                    Log.e("ninadlog", "error invoke return " + error.getMessage());
                }
            });
        }
        else
        {
            makeConnection(new Connected() {
                @Override
                public void onConnected() {
                    handleActionImage(param1,ImageUrl);
                }
            });

        }
    }


    private void uploadImage(final String qrCode,final String ImageUrl, final String storeKey)
    {


            ArrayList<NameValue> requestHeaders = new ArrayList<NameValue>();
            FileToUpload file = new FileToUpload(ImageUrl, "imgi", "content", "Image");
            ArrayList<FileToUpload> filesToUpload = new ArrayList<FileToUpload>();
            filesToUpload.add(file);
            ArrayList<NameValue> requestParameters = new ArrayList<NameValue>();
//            requestParameters.add(new NameValue("clientId", conn.getConnectionId()));
//            requestParameters.add(new NameValue("isLite", "false"));
//            requestParameters.add(new NameValue("storeKey", storeKey));
        LogUtils.LOGD("hubcall", "completed uploadImage is " + storeKey);
            try {
                new ImageUpload().handleFileUpload("1001", Constants.URL + "payload/"+getClientId()+"/"+storeKey+"/Large.jpg", "POST"
                        , filesToUpload, requestHeaders, requestParameters, new UploadProgressListener() {

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
                        LogUtils.LOGD("hubcall","completed Storekey is "+storeKey);
                        completeImageShare(storeKey, qrCode);
                        ImageMap.getInstance().setShareCode(ImageUrl, storeKey);
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
//        requestParameters.add(new NameValue("clientId", conn.getConnectionId()));
//        requestParameters.add(new NameValue("isLite","true"));
//        requestParameters.add(new NameValue("storeKey",storeKey));

        LogUtils.LOGD("hubcall", "completed UploadThubnail is " + storeKey);
        try {
            new ImageUpload().handleFileUpload("1002" , Constants.URL + "payload/"+getClientId()+"/"+storeKey+"/thumb.jpg" , "POST"
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
                    Log.e("ninadlog","liteimage share completed ");
//                    completeImageShare(storeKey,qrCode);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void completeImageShare(final String fileKey, final String param1)
    {

            LogUtils.LOGD("hubcall","sharecalled Storekey is "+fileKey);
            hubProxy.invoke("Share", param1, "image/jpeg", fileKey).onError(new ErrorCallback() {
                @Override
                public void onError(Throwable error) {
                    Log.e("ninadlog", "error share return " + error.getMessage());
                }
            });

        releaseWakelock();
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
        releaseWakelock();
        notificationManager.cancel(1);

    }

    private ClientTransport resolveTransport()
    {

        try
        {
            ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

//mobile

            NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();

//wifi
            NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();

            if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                return new LongPollingTransport(logger);
            } else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                //wifi
                return new ServerSentEventsTransport(logger);
            }
            else
                return new LongPollingTransport(logger);
        }
        catch (Exception ex)
        {
            return new ServerSentEventsTransport(logger);
        }

    }

    interface Connected
    {
      void onConnected();
    }

}
