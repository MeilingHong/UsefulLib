package com.meiling.download;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2017/8/2.
 */

public class UpdateUtil {

    private static final String PREFERENCES_FILE = "base_setting";
    private static final String UPDATE_TIME = "update_time";

    public static String getUpdateTime(Context activity,String url) {
        return activity.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).getString(UPDATE_TIME+"_"+url,"");
    }

    public static void setUpdateTime(Context activity,String url,String userName) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(PREFERENCES_FILE,Context.MODE_PRIVATE).edit();
        editor.putString(UPDATE_TIME+"_"+url, userName);
        editor.commit();
    }

    public static void updateCheckTime(Context activity,String netUrl) {
        setUpdateTime(activity,netUrl,System.currentTimeMillis() + "");
    }

    public static boolean isCheckedUpdateToday(Context context,String netUrl) {
        boolean bool;
        String time = getUpdateTime(context,netUrl);
        if (time == null || "".equals(time)) {
            bool = false;
        } else {
            long longTime = Long.parseLong(time);
            if ((System.currentTimeMillis() - longTime) < 24 * 60 * 60 * 1000) {
                //今天已经检查过更新了
                bool = true;
            } else {
                //今天尚未检查更新
                bool = false;
            }
        }
        return bool;
    }
}
