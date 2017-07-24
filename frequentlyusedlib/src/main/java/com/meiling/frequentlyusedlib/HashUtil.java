package com.meiling.frequentlyusedlib;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2017/7/24.
 */

public class HashUtil {
    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static MessageDigest messageDigest = null;

    private static MessageDigest commonDigest = null;

    private static MessageDigest sha1Digest = null;

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            sha1Digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String getMD5String(String str) {
        return getMD5String(str.getBytes());
    }

    private static final String ENCODE = "utf-8";
    public static final String TYPE_SHA1 = "SHA-1";
    public static final String TYPE_SHA256 = "SHA-256";
    public static final String TYPE_SHA512 = "SHA-512";

    public static String getSignatureString(String source,String type){
        try {
            commonDigest = MessageDigest.getInstance(type);
            return getTypeString(source.getBytes(ENCODE));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getTypeString(byte[] bytes) {
        commonDigest.update(bytes);
        return bytesToHex(commonDigest.digest());
    }

    public static String getSHA1String(String str) {
        return getSHA1String(str.getBytes());
    }

    public static String getSHA1String(byte[] bytes) {
        sha1Digest.update(bytes);
        return bytesToHex(sha1Digest.digest());
    }

    public static String getMD5String(byte[] bytes) {
        messageDigest.update(bytes);
        return bytesToHex(messageDigest.digest());
    }

    public static String bytesToHex(byte bytes[]) {
        return bytesToHex(bytes, 0, bytes.length);
    }

    public static String bytesToHex(byte bytes[], int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + end; i++) {
            sb.append(byteToHex(bytes[i]));
        }
        return sb.toString();
    }

    public static String byteToHex(byte bt) {
        return HEX_DIGITS[(bt & 0xf0) >> 4] + "" + HEX_DIGITS[bt & 0xf];
    }


    public static String toBase64(String enco){
        String result = "";
        try {
            result = Base64.encodeToString(enco.getBytes("utf-8"),Base64.URL_SAFE).replace("\n","");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
