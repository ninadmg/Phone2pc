package link.bleed.app.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ninad on 04-03-2015.
 */
public class SharePrefUtils {

    public static String getClientId(Context context)
    {

        return context.getSharedPreferences("bleed", Context.MODE_PRIVATE).getString("Client","");
    }

    public static void setClientID(Context context,String Id)
    {
        SharedPreferences pref = context.getSharedPreferences("bleed", Context.MODE_PRIVATE);
        pref.edit().putString("Client",Id).apply();
    }

}
