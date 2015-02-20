package link.bleed.app.Network;

/**
 * Created by ninad on 20-02-2015.
 */
public interface UploadProgressListener {

    public void uploadStarted();

    public void uploadUpdate(int percentage);

    public void uploadComplete();

}
