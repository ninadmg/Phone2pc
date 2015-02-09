package link.bleed.app;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by bleed on 05-02-2015.
 */
public class FileToUpload implements Parcelable {

    private final File file;
    private final String fileName;
    private final String contentType;

    public FileToUpload(String path, String fileName, String contentType) {
        this.file = new File(path);
        this.fileName = fileName;
        this.contentType = contentType;
    }

    private FileToUpload(Parcel in) {
        file = new File(in.readString());
        contentType = in.readString();
        fileName = in.readString();
    }

    public final InputStream getStream() throws FileNotFoundException {
        return new FileInputStream(file);
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
