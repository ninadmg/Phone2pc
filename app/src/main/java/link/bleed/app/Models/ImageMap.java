package link.bleed.app.Models;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by ninad on 23-02-2015.
 */
public class ImageMap {

    private Hashtable<String ,String> compressedAddressTable = new Hashtable<String ,String>();
    private Hashtable<String ,String> shareCodeTable = new Hashtable<String ,String>();
    private static ImageMap ourInstance = new ImageMap();
    private static final String UPLODING = "uploading";
    private String currentImage;
    public static ImageMap getInstance() {
        return ourInstance;
    }
    private ArrayList<UploadObserver> observers = new ArrayList<UploadObserver>();
    private ImageMap() {
    }

    public  String getCompressedAddress(String fileAddress) {
        return compressedAddressTable.get(fileAddress);
    }

    public void setObservers(UploadObserver observer)
    {
        observers.add(observer);
    }
    public void removeObserver(UploadObserver observer)
    {
        observers.remove(observer);
    }


    public void setCompressedAddress(String fileAddress, String compressedAddress) {
        compressedAddressTable.put(fileAddress, compressedAddress);
    }

    public void setCurrentImage(String currentImage)
    {
        this.currentImage = currentImage;
    }

    public boolean isCurrentImage(String currentImage)
    {
        return (this.currentImage==null)||(this.currentImage.equals(currentImage)) ;
    }

    public String getShareCode(String fileAddress) {
        String code = shareCodeTable.get(fileAddress);

        if(code!=null && code.equals(UPLODING))
        {
            code=null;
        }
        return code;
    }

    public void clearMap()
    {
        compressedAddressTable.clear();
        shareCodeTable.clear();
        observers.clear();
    }

    public void setShareCode(String fileAddress, String shareCode) {
        String code = shareCodeTable.get(fileAddress);
        if(code!=null && code.equals(UPLODING))
        {
            shareCodeTable.remove(fileAddress);
        }
        updateObserver(fileAddress);
        shareCodeTable.put(fileAddress,shareCode);
    }

    private void updateObserver(String fileAddress)
    {
        for(UploadObserver observer:observers)
        {
            observer.uploadCompleted(fileAddress);
        }
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


}
