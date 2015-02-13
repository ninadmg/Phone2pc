package link.bleed.app;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Created by ninad on 11-02-2015.
 */
public class ScannerFragment extends Fragment implements ZBarScannerView.ResultHandler{

    private ZBarScannerView mScannerView;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mScannerView = new ZBarScannerView(getActivity());

        return mScannerView;
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
    public void handleResult(Result result) {
        if(getActivity() instanceof ZBarScannerView.ResultHandler)
        {
            ((ZBarScannerView.ResultHandler) getActivity()).handleResult(result);
        }
    }
}
