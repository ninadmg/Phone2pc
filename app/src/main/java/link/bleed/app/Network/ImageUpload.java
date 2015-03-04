package link.bleed.app.Network;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import link.bleed.app.Models.FileToUpload;
import link.bleed.app.Models.NameValue;
import link.bleed.app.Utils.LogUtils;

/**
 * Created by ninad on 20-02-2015.
 */
public class ImageUpload {


    private static final int BUFFER_SIZE = 4096;
    private static final String NEW_LINE = "\r\n";
    private static final String TWO_HYPHENS = "--";
    UploadProgressListener UploadProgress;
    public void handleFileUpload(final String uploadId, final String url, final String method,
                                  final ArrayList<FileToUpload> filesToUpload,
                                  final ArrayList<NameValue> requestHeaders,
                                  final ArrayList<NameValue> requestParameters,UploadProgressListener uploadProgressListener) throws IOException {
        this.UploadProgress = uploadProgressListener;
        final String boundary = getBoundary();
        final byte[] boundaryBytes = getBoundaryBytes(boundary);

        HttpURLConnection conn = null;
        OutputStream requestStream = null;
        InputStream responseStream = null;

        try {
            conn = getMultipartHttpURLConnection(url, method, boundary, filesToUpload.size());

            setRequestHeaders(conn, requestHeaders);

            requestStream = conn.getOutputStream();
            setRequestParameters(requestStream, requestParameters, boundaryBytes);

            uploadFiles(uploadId, requestStream, filesToUpload, boundaryBytes);

            final byte[] trailer = getTrailerBytes(boundary);
            requestStream.write(trailer, 0, trailer.length);
            final int serverResponseCode = conn.getResponseCode();

            if (serverResponseCode / 100 == 2) {
                responseStream = conn.getInputStream();
            } else { // getErrorStream if the response code is not 2xx
                responseStream = conn.getErrorStream();
            }
            final String serverResponseMessage = getResponseBodyAsString(responseStream);
            LogUtils.LOGD("hubcall", "completed serverResponseMessage is " + serverResponseMessage);


        } finally {
            closeOutputStream(requestStream);
            closeInputStream(responseStream);
            closeConnection(conn);
            UploadProgress.uploadComplete();
        }
    }

    private String getResponseBodyAsString(final InputStream inputStream) {
        StringBuilder outString = new StringBuilder();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                outString.append(line);
            }
        } catch (Exception exc) {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception readerExc) {
            }
        }

        return outString.toString();
    }

    private String getBoundary() {
        final StringBuilder builder = new StringBuilder();

        builder.append("---------------------------").append(System.currentTimeMillis());

        return builder.toString();
    }

    private byte[] getBoundaryBytes(final String boundary) throws UnsupportedEncodingException {
        final StringBuilder builder = new StringBuilder();

        builder.append(NEW_LINE).append(TWO_HYPHENS).append(boundary).append(NEW_LINE);

        return builder.toString().getBytes("US-ASCII");
    }

    private byte[] getTrailerBytes(final String boundary) throws UnsupportedEncodingException {
        final StringBuilder builder = new StringBuilder();

        builder.append(NEW_LINE).append(TWO_HYPHENS).append(boundary).append(TWO_HYPHENS).append(NEW_LINE);

        return builder.toString().getBytes("US-ASCII");
    }

    private HttpURLConnection getMultipartHttpURLConnection(final String url, final String method,
                                                            final String boundary, int totalFiles) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setChunkedStreamingMode(0);
        conn.setRequestMethod(method);
        if (totalFiles <= 1) {
            conn.setRequestProperty("Connection", "close");
        } else {
            conn.setRequestProperty("Connection", "Keep-Alive");
        }
        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        return conn;
    }

    private void setRequestHeaders(final HttpURLConnection conn, final ArrayList<NameValue> requestHeaders) {
        if (!requestHeaders.isEmpty()) {
            for (final NameValue param : requestHeaders) {
                conn.setRequestProperty(param.getName(), param.getValue());
            }
        }
    }

    private void setRequestParameters(final OutputStream requestStream, final ArrayList<NameValue> requestParameters,
                                      final byte[] boundaryBytes) throws IOException, UnsupportedEncodingException {
        if (!requestParameters.isEmpty()) {

            for (final NameValue parameter : requestParameters) {
                requestStream.write(boundaryBytes, 0, boundaryBytes.length);
                byte[] formItemBytes = parameter.getBytes();
                requestStream.write(formItemBytes, 0, formItemBytes.length);
            }
        }
    }

    public
    void
    uploadFiles(final String uploadId, final OutputStream requestStream,
                final ArrayList<FileToUpload> filesToUpload, final byte[] boundaryBytes)
            throws UnsupportedEncodingException,
            IOException,
            FileNotFoundException {
        UploadProgress.uploadStarted();
        final long totalBytes = getTotalBytes(filesToUpload);
        long uploadedBytes = 0;

        for (FileToUpload file : filesToUpload) {
            requestStream.write(boundaryBytes, 0, boundaryBytes.length);
            byte[] headerBytes = file.getMultipartHeader();
            requestStream.write(headerBytes, 0, headerBytes.length);

            final InputStream stream = file.getStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            try {
                while ((bytesRead = stream.read(buffer, 0, buffer.length)) > 0) {
                    requestStream.write(buffer, 0, bytesRead);
                    uploadedBytes += bytesRead;
                    UploadProgress.uploadUpdate((int)((uploadedBytes*100)/totalBytes)) ;
                }
            } finally {
                closeInputStream(stream);

            }
        }
    }

    private long getTotalBytes(final ArrayList<FileToUpload> filesToUpload) {
        long total = 0;

        for (FileToUpload file : filesToUpload) {
            total += file.length();
        }

        return total;
    }



    private void closeOutputStream(final OutputStream stream) {
        if (stream != null) {
            try {
                stream.flush();
                stream.close();
            } catch (Exception exc) {
            }
        }
    }

    private void closeConnection(final HttpURLConnection connection) {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception exc) {
            }
        }
    }


    private void closeInputStream(final InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

}
