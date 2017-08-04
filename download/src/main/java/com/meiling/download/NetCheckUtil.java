package com.meiling.download;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

/**
 * Created by Administrator on 2017/8/4.
 */

public class NetCheckUtil {
    public static final int TYPE_NET_WORK_DISABLED = 1;
    public static final int TYPE_WIFI = 2;
    public static final int TYPE_MOBILE = 3;
    public static final int TYPE_OTHER = 4;

    public static int checkNetworkType(Context mContext) {
        try {
            final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo mobNetInfoActivity = connectivityManager
                    .getActiveNetworkInfo();
            if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {
                // 注意一：
                // NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，
                // 但是有些电信机器，仍可以正常联网，
                // 所以当成net网络处理依然尝试连接网络。
                // （然后在socket中捕捉异常，进行二次判断与用户提示）。
                return TYPE_NET_WORK_DISABLED;
            } else {
                // NetworkInfo不为null开始判断是网络类型
                int netType = mobNetInfoActivity.getType();
                if (netType == ConnectivityManager.TYPE_WIFI) {
                    // wifi net处理
                    return TYPE_WIFI;
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                    return TYPE_MOBILE;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return TYPE_OTHER;
        }
        return TYPE_OTHER;
    }

    public static boolean isWiFi(Context mContext){
        if(TYPE_WIFI==checkNetworkType(mContext)){
            return true;
        }else{
            return false;
        }
    }

    public static boolean isMobile(Context mContext){
        if(TYPE_MOBILE==checkNetworkType(mContext)){
            return true;
        }else{
            return false;
        }
    }
}
