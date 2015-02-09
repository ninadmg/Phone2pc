package in.ninad.p2c;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * Created by ninad on 05-02-2015.
 */
public class intro extends FragmentActivity {

    private static final int NUM_PAGES = 4;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    pagerItem[] pagerItems = new pagerItem[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPages();
        mPager = (ViewPager) findViewById(R.id.container);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    private void initPages()
    {
        pagerItems[0]= new pagerItem(R.drawable.ic_launcher,"What is Bleed useful for?","Bleed helps you share text, urls and images from one of your device to other devices. If you are browsing on your mobile or have a link which you need to open on a desktop, Bleed can help.");
        pagerItems[1]= new pagerItem(R.drawable.screen2,"How to use Bleed?","Touch on the link or share the link from menu-> share option and choose Bleed. This will now open a QRCode scanner");
        pagerItems[2]= new pagerItem(R.drawable.screen3,"Scan the code","Open the webpage http://Bleed.link on your other device on which you want to open the link in. That page will display a QRCode image. Scan this code with your Bleed App.");
        pagerItems[3]= new pagerItem(R.drawable.screen4,"Awesomeness","If you shared a url, the webpage that displayed the QRCode will now get redirected to that url, else the received content will be displayed in the website.");

    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position)
            {
                case 0: return  slidePageFirstFragment.newInstance(pagerItems[position]);
                default: return  SlidePageFragment.newInstance(pagerItems[position]);
            }

        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
