package com.meiling.logforfile;

import android.content.Context;
import android.util.Log;

/**
 * Created by Administrator on 16:32.
 *
 * @package com.meiling.logforfile
 * @auther By MeilingHong
 * @emall marisareimu123@gmail.com
 * @date 2018-01-29   16:32
 */

public class LogUitl {
    private static final String TAG = LogUitl.class.getName();//


    public static void v(String msg){
        Log.v(TAG,msg);
    }

    public static void d(String msg){
        Log.d(TAG,msg);
    }

    public static void i(String msg){
        Log.i(TAG,msg);
    }

    public static void w(String msg){
        Log.w(TAG,msg);
    }

    public static void e(String msg){
        Log.e(TAG,msg);
    }

    public static void e(Context context,String msg, boolean saveIntoLogFile){
        Log.e(TAG,msg);

        if(saveIntoLogFile){
            //  将信息加入队列里，开启一个单线程线程池去专门来进行日志信息的保存
            SaveLogUtil.getInstances().addIntoLogFile(context,msg);
        }
    }
}
