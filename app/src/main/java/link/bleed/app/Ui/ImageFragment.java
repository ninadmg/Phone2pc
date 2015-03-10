package link.bleed.app.Ui;


import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import link.bleed.app.Models.ImageMap;
import link.bleed.app.Models.UploadObserver;
import link.bleed.app.R;
import link.bleed.app.Utils.ImageResizer;
import link.bleed.app.Utils.LogUtils;
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
    ProgressBar progressBar;
    UploadObserver observer;
    String compressedpath;
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
        View view = inflater.inflate(
                R.layout.image_pager_cell, container, false);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Uri imageuri = Uri.parse(getArguments().getString(IMAGE_URL));
        String imagepath = Utilities.getPath(getActivity(),imageuri);
        qrcode = getArguments().getString(QRCODE);

         compressedpath = getCompressedPath(imagepath);
        imageView.setImageBitmap(BitmapFactory.decodeFile(compressedpath));

    }

    private void addObservable(final String compressedpath) {

            observer = new UploadObserver() {
                @Override
                public void uploadCompleted(String imagepath) {
                    if (compressedpath.equals(imagepath))
                    {
                        removeLoading();
                    }
                }
            };
            map.setObservers(observer);

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if(checkisUpoading(compressedpath))
        {
            showloading();
            addObservable(compressedpath);
        }
        else
        {
            removeLoading();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        map.removeObserver(observer);
        observer=null;
    }

    private void removeLoading()
    {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    progressBar.setVisibility(View.GONE);

                }
            });
        }catch (Exception e){
            LogUtils.LOGD("ninad", "removeLoading "+e.getMessage());
        }
    }

    private void showloading() {

        progressBar.setVisibility(View.VISIBLE);
    }

    private boolean checkisUpoading(String compressedpath)
    {
        return (map.getShareCode(compressedpath)==null|| map.isUploading(compressedpath));

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
