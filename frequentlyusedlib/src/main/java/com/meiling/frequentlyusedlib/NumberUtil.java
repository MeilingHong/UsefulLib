package com.meiling.frequentlyusedlib;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/7/24.
 */

public class NumberUtil {
    private static final String numberRegex = "^[0-9]+$";

    //TODO check string is number or not
    public static boolean isNumber(String numberString){
        try{
            if(numberString==null ||
                    numberString.length()<1 ||
                    numberString.replace(" ","").length()<1 ||
                    numberString.isEmpty()){
                return false;
            }
            Pattern pattern = Pattern.compile(numberRegex);
            Matcher matcher = pattern.matcher(numberString);
            return matcher.matches();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    private static final String twoBitDecimalFormat = "####.##";
    //TODO 2 decimal
    public String get2DecimalString(double number){//由于其他类型都能够转化为double，则不进行更多的设置
        DecimalFormat decimalFormat = new DecimalFormat(twoBitDecimalFormat);
        String result = decimalFormat.format(number);
        if(result.indexOf(".")>=0){
            if(1==(result.length()-result.indexOf("."))){
                return result+"00";
            }else if(2==(result.length()-result.indexOf("."))){
                return result+"0";
            }else{
                return result;
            }
        }else{
            return result+".00";
        }
    }

    //TODO 指定保留的小数位数
    public static String getDecimalString(double number,int bitNumber){
        String format = "######";
        for (int i = 0; i < bitNumber; i++) {
            if (i == 0) {
                format += ".#";
            } else {
                format += "#";
            }
        }
        DecimalFormat decimalFormat = new DecimalFormat(format);
        return decimalFormat.format(number);
    }
}
