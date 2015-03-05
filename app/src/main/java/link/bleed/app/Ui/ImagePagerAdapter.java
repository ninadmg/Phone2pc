package link.bleed.app.Ui;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by ninad on 23-02-2015.
 */
public class ImagePagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Uri> imageUris ;
    private String qrcode;
    public ImagePagerAdapter(FragmentManager fm,ArrayList<Uri> imageUris,String qrcode) {
        super(fm);
        this.imageUris =imageUris;
        this.qrcode = qrcode;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.newInstance(imageUris.get(position),qrcode);
    }

    @Override
    public int getCount() {
        return imageUris.size();
    }
}
