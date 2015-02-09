package in.ninad.p2c;

import java.io.Serializable;

/**
 * Created by ninad on 05-02-2015.
 */
public class pagerItem implements Serializable{

    public int imageResource;
    public String title;
    public String desc;

    public pagerItem(int imageResource, String title, String desc) {
        this.imageResource = imageResource;
        this.title = title;
        this.desc = desc;
    }
}
