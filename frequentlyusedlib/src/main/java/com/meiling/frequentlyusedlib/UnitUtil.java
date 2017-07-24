package com.meiling.frequentlyusedlib;

import android.content.Context;

/**
 * Created by Administrator on 2017/7/24.
 */

public class UnitUtil {
    public static float spToPx(Context context,float scalePixel){
        if(scalePixel<=0){
            return 0;
        }
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (scalePixel * fontScale + 0.5f);
    }

    public static float pxToSp(Context context,float pixel){
        if(pixel<=0){
            return 0;
        }
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (pixel / fontScale + 0.5f);
    }

    public static float dpToPx(Context context,float scalePixel){
        if(scalePixel<=0){
            return 0;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (scalePixel * scale + 0.5f);
    }

    public static float pxToDp(Context context,float pixel){
        if(pixel<=0){
            return 0;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pixel / scale + 0.5f);
    }
}
