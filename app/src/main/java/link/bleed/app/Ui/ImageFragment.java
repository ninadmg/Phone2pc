package link.bleed.app.Ui;


import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import link.bleed.app.Models.ImageMap;
import link.bleed.app.Utils.ImageResizer;
import link.bleed.app.Utils.Utilities;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends Fragment {

    private static final String IMAGE_URL = "link.bleed.p2c.ImageFragment.URL";
    private static final String QRCODE = "link.bleed.p2c.ImageFragment.qrcode";
    private ImageView imageView;
    private final ImageMap map = ImageMap.getInstance();
    private String qrcode;
    public ImageFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(Uri imageurl,String qrcode)
    {
        ImageFragment imageFragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URL,imageurl.toString());
        args.putString(QRCODE,qrcode);
        imageFragment.setArguments(args);
        return imageFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         imageView = new ImageView(getActivity());

        return imageView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Uri imageuri = Uri.parse(getArguments().getString(IMAGE_URL));
        String imagepath = Utilities.getPath(getActivity(),imageuri);
        qrcode = getArguments().getString(QRCODE);

        String compressedpath = getCompressedPath(imagepath);

        imageView.setImageBitmap(BitmapFactory.decodeFile(compressedpath));
    }

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


}
