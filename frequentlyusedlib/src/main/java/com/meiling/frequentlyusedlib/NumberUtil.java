package com.meiling.frequentlyusedlib;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 16:32.
 *
 * @package com.meiling.frequentlyusedlib
 * @auther By MeilingHong
 * @emall marisareimu123@gmail.com
 * @date 2018-01-29   16:32
 */

public class NumberUtil {
    private static final String numberRegex = "^[0-9]+$";

    //  Check string is number or not / 检查字符串是否为纯数字串
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
    //  A decimal number that retains a fixed number of two digits. /  固定保留两位小数
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

    //  Holds the decimal number of a specified number of digits /  指定保留的小数位数，若最后为0，小数位数将减少
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
