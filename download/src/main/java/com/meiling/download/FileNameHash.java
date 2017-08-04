package com.meiling.download;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2017/8/4.
 */

public class FileNameHash {
    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static MessageDigest sha1Digest = null;

    static {
        try {
            sha1Digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String getSHA1String(String str) {
        return getSHA1String(str.getBytes());
    }

    public static String getSHA1String(byte[] bytes) {
        sha1Digest.update(bytes);
        return bytesToHex(sha1Digest.digest());
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
}
