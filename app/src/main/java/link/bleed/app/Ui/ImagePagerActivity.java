package link.bleed.app.Ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.viewpagerindicator.LinePageIndicator;

import java.util.ArrayList;

import link.bleed.app.Models.ImageMap;
import link.bleed.app.Network.PostService;
import link.bleed.app.R;
import link.bleed.app.Utils.ImageResizer;
import link.bleed.app.Utils.LogUtils;
import link.bleed.app.Utils.Utilities;

/**
 * Created by ninad on 23-02-2015.
 */
public class ImagePagerActivity extends ActionBarActivity {

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private PostService mService;
    boolean mBound = false;
    private ImageMap map = ImageMap.getInstance();
    private ArrayList<Uri> imageUris;
    private String qrcode;
    public static final String IMAGGELIST = "link.bleed.p2c.extra.imagelist";
    public static final String QRCODE = "link.bleed.p2c.extra.qrcode";
    private LinePageIndicator pageIndicator;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPager = (ViewPager) findViewById(R.id.container);
        pageIndicator = (LinePageIndicator) findViewById(R.id.titles);
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
        Intent intent = new Intent(this, PostService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        imageUris = getIntent().getParcelableArrayListExtra(IMAGGELIST);
        qrcode = getIntent().getStringExtra(QRCODE);
    }

    private void setupadapter()
    {
        mPagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(),imageUris,qrcode);
        mPager.setAdapter(mPagerAdapter);
        Listner();
        setupIndicator(pageIndicator);

        Sendimage(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

    private void Listner()
    {
        pageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
                                           @Override
                                           public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                           }

                                           @Override
                                           public void onPageSelected(int position) {
                                               Sendimage(position);
                                           }

                                           @Override
                                           public void onPageScrollStateChanged(int state) {

                                           }
                                       }

        );
    }

    private void setupIndicator(LinePageIndicator pageIndicator)
    {
        final float density = getResources().getDisplayMetrics().density;
        pageIndicator.setSelectedColor(getResources().getColor(R.color.primary_color));
        pageIndicator.setViewPager(mPager);
        pageIndicator.setStrokeWidth(4 * density);
        pageIndicator.setLineWidth(30 * density);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            PostService.LocalBinder binder = (PostService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            setupadapter();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private String getCompressedPath(String path)
    {

        String compressedpath = map.getCompressedAddress(path);
        if(compressedpath==null)
        {
            compressedpath = ImageResizer.getResizedImage(path);
            map.setCompressedAddress(path,compressedpath);
        }
        return compressedpath;
    }


    private void Sendimage(int pos)
    {
        String imagepath = Utilities.getPath(this,imageUris.get(pos));
        String compressedpath = getCompressedPath(imagepath);
        setImageInWeb(compressedpath);
    }

    private void setImageInWeb(String compressedpath)
    {
        if(!map.isUploading(compressedpath)) {
            String sharecode = map.getShareCode(compressedpath);
            LogUtils.LOGD("hubcall", "completed setImageInWeb "+sharecode );
            LogUtils.LOGD("hubcall", "completed compressedpath "+compressedpath );
            if (sharecode == null) {
                mService.handleActionImage(qrcode, compressedpath);
            } else {
                mService.handleActionImageShare(qrcode, sharecode);
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mPager",mPager.getCurrentItem());
//        outState.putParcelable("pageIndicator",pageIndicator.onSaveInstanceState());

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPager.setCurrentItem(savedInstanceState.getInt("mPager"));
//        pageIndicator.onRestoreInstanceState(savedInstanceState.getParcelable("pageIndicator"));

    }
}
