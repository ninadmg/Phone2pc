package in.ninad.p2c;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class MainActivity extends Activity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);

    }


    @Override
    protected void onStart() {
        super.onStart();

    }



    private void handleSendVideo(Intent intent) {

    }

    private void handleSendImage(Intent intent) {

    }

    private void handleSendText(String url,String qrcode) {
        PostService.startActionText(MainActivity.this,qrcode,url);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
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
        // as you specify a parent activity in AndroidManifest.xml.
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
                handleSendImage(intent); // Handle single image being sent
            }
            else if(type.startsWith("video/")){
                handleSendVideo(intent);
            }
        }
        if(intent.getAction().equals(Intent.ACTION_VIEW))
        {
               handleSendText( intent.getData().toString(),result.getText());
        }
//        Toast.makeText(this,result.getText(),Toast.LENGTH_SHORT).show();
    }
}
