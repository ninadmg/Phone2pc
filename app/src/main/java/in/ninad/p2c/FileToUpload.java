package in.ninad.p2c;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by ninad on 05-02-2015.
 */
public class FileToUpload implements Parcelable {

    private final File file;
    private final String fileName;
    private final String contentType;

    public FileToUpload(File file, String fileName, String contentType) {
        this.file = file;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    private FileToUpload(Parcel in) {
        file = new File(in.readString());
        contentType = in.readString();
        fileName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(file.getAbsolutePath());
        parcel.writeString(contentType);
        parcel.writeString(fileName);
    }

    public long length() {
        return file.length();
    }
}
