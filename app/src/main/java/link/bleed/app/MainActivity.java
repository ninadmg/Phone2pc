package link.bleed.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class MainActivity extends ActionBarActivity implements ZXingScannerView.ResultHandler{

    FrameLayout frameLayout;

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

        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame,new ScannerFragment(),"scan")
                .commit();

    }



    private void handleSendVideo(Intent intent) {

    }

    private void handleSendImage(String url,String qrcode) {
        if(url==null)
        {
            Toast.makeText(this,"Please try again",Toast.LENGTH_SHORT).show();
            Crashlytics.log("Path returns null");
        }
        PostService.startActionImage(MainActivity.this,qrcode,url);
        finish();
    }

    private void handleSendText(String url,String qrcode) {
        PostService.startActionText(MainActivity.this,qrcode,url);
        finish();
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
        if(intent.getAction().equals(Intent.ACTION_SEND))
        {

            String type = intent.getType();
            if ("text/plain".equals(type)) {
                handleSendText(intent.getClipData().getItemAt(0).getText().toString(),result.getText()); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(Utilities.getPath(this,intent.getClipData().getItemAt(0).getUri()),result.getText()); // Handle single image being sent
            }
            else if(type.startsWith("video/")){
                handleSendVideo(intent);
            }
        }
        if(intent.getAction().equals(Intent.ACTION_VIEW))
        {
               handleSendText( intent.getData().toString(),result.getText());
        }

    }


}
