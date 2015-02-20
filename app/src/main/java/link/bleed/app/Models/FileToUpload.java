package link.bleed.app.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by bleed on 05-02-2015.
 */
public class FileToUpload implements Parcelable {

    private final File file;
    private final String fileName;
    private final String contentType;
    private static final String NEW_LINE = "\r\n";
    private final String paramName;

    public FileToUpload(String path, String fileName,String paramName, String contentType) {
        this.file = new File(path);
        this.fileName = fileName;
        this.paramName =paramName;
        this.contentType = contentType;
    }

    private FileToUpload(Parcel in) {
        file = new File(in.readString());
        paramName = in.readString();
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
        parcel.writeString(paramName);
        parcel.writeString(contentType);
        parcel.writeString(fileName);
    }

    public byte[] getMultipartHeader() throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();

        builder.append("Content-Disposition: form-data; name=\"")
                .append(paramName)
                .append("\"; filename=\"")
                .append(fileName)
                .append("\"")
                .append(NEW_LINE);

        if (contentType != null) {
            builder.append("Content-Type: ")
                    .append(contentType)
                    .append(NEW_LINE);
        }

        builder.append(NEW_LINE);

        return builder.toString().getBytes("UTF-8");
    }

    public long length() {
        return file.length();
    }
}
