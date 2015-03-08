package link.bleed.app.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ninad on 19-02-2015.
 */
public class ImageResizer {

    public static String getResizedImage(String ImagePath)
    {
        return getResizedImage(ImagePath,false);
    }


    private static Bitmap resizeBitmap(int targetW, int targetH, String photoPath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.max(photoW/targetW, photoH/targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

    public static String getResizedImage(String ImagePath,boolean thumb)
    {
        File imageFile = new File(ImagePath);
        File path;
        Bitmap bitmap;
        if(thumb)
        {
            path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Bleed/Thumb/");
            bitmap = resizeBitmap(1024, 768, ImagePath);
        }
        else {
            path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Bleed/Images/");
            bitmap = resizeBitmap(1024, 768, ImagePath);
        }
        String ResizedPath =imageFile.getName().replaceFirst("[.][^.]+$", "")+".jpeg";
        File ResizedimageFile = new File(path,ResizedPath);


        try {
            path.mkdirs();
            ResizedimageFile.createNewFile();
            FileOutputStream ostream = new FileOutputStream(ResizedimageFile);
            if (thumb)
           {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 5, ostream);
           }else
            {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, ostream);
            }
            ostream.close();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        return ResizedimageFile.getAbsolutePath();
    }

}
