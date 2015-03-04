package link.bleed.app.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Hashtable;

/**
 * Created by ninad on 23-02-2015.
 */
public class ImageMap implements Parcelable{

    Hashtable<String ,String> compressedAddressTable = new Hashtable<String ,String>();
    Hashtable<String ,String> shareCodeTable = new Hashtable<String ,String>();
    private static ImageMap ourInstance = new ImageMap();
    public static final String UPLODING = "uploading";
    public static ImageMap getInstance() {
        return ourInstance;
    }

    private ImageMap() {
    }

    public  String getCompressedAddress(String fileAddress) {
        return compressedAddressTable.get(fileAddress);
    }

    public void setCompressedAddress(String fileAddress, String compressedAddress) {
        compressedAddressTable.put(fileAddress,compressedAddress);
    }

    public String getShareCode(String fileAddress) {
        String code = shareCodeTable.get(fileAddress);
        if(code!=null && code.equals(UPLODING))
        {
            code=null;
        }

        return code;
    }

    public void setShareCode(String fileAddress, String shareCode) {
        String code = shareCodeTable.get(fileAddress);
        if(code!=null && code.equals(UPLODING))
        {
            shareCodeTable.remove(fileAddress);
        }

        shareCodeTable.put(fileAddress,shareCode);
    }

    public void setUploadStarted(String fileAddress)
    {
        shareCodeTable.put(fileAddress,UPLODING);
    }

    public boolean isUploading(String fileAddress)
    {
        String code = shareCodeTable.get(fileAddress);

        return (code!=null && code.equals(UPLODING));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
