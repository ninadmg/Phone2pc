package link.bleed.app.Ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.util.ArrayList;

import link.bleed.app.Models.ImageMap;
import link.bleed.app.Network.PostService;
import link.bleed.app.R;
import link.bleed.app.Utils.ImageResizer;
import link.bleed.app.Utils.Utilities;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;


public class MainActivity extends ActionBarActivity implements ZBarScannerView.ResultHandler{

    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragmentholder);
        frameLayout = (FrameLayout) findViewById(R.id.frame);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        if(getSupportActionBar()!=null) {
            setSupportActionBar(toolbar);
        }

    }

    private void setupToolbar(Toolbar toolbar)
    {
        toolbar.setTitle(" "+getString(R.string.app_name));
        toolbar.setLogo(R.drawable.ic_launcher);
        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color));

    }

    @Override
    protected void onStart() {
        super.onStart();

        clearOldItems();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame,new ScannerFragment(),"scan")
                .commit();

    }

    private void clearOldItems()
    {
        ImageMap.getInstance().clearMap();
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Bleed/.Thumb/");
        path.delete();
        path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Bleed/.Images/");
        path.delete();
    }

    private void handleSendVideo(Intent intent) {

    }

    private void handleSendImage(String url,String qrcode) {
        if(url==null)
        {
            Toast.makeText(this,"Please try again",Toast.LENGTH_SHORT).show();
            Crashlytics.log("Path returns null");
        }


        url = ImageResizer.getResizedImage(url);
        PostService.startActionImage(MainActivity.this, qrcode, url);
        finish();
    }

    private void handleSendText(String url,String qrcode) {
        PostService.startActionText(MainActivity.this,qrcode,url);
        finish();
    }

    void handleSendMultipleImages(Intent intent,String qrcode) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            Intent actIntent = new Intent(MainActivity.this,ImagePagerActivity.class);
            actIntent.putExtra(ImagePagerActivity.QRCODE,qrcode);
            actIntent.putExtra(ImagePagerActivity.IMAGGELIST,imageUris);
            startActivity(actIntent);
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity link AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(Result result) {
        Intent intent  = getIntent();
        String type = intent.getType();
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_SEND))
        {
            if ("text/plain".equals(type)) {
                handleSendText(intent.getStringExtra(Intent.EXTRA_TEXT), result.getContents()); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(Utilities.getPath(this,  (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM)),result.getContents()); // Handle single image being sent
            }
            else if(type.startsWith("video/")){
                handleSendVideo(intent);
            }
        }
        else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent,result.getContents()); // Handle multiple images being sent
            }
        }
        if(action.equals(Intent.ACTION_VIEW))
        {
               handleSendText( intent.getData().toString(),result.getContents());
        }

    }


}
