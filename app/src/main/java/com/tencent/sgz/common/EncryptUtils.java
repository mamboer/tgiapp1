package com.tencent.sgz.common;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by levin on 6/18/14.
 */
public class EncryptUtils {
    private static final String SECRET_CODE = "::SECRET::";

    public static String encodeBase64(String txt) {
        return Base64.encodeToString(txt.getBytes(), Base64.DEFAULT);
    }

    public static String decodeBase64(String txt) {
        return new String(Base64.decode(txt, Base64.DEFAULT));
    }

    public static String encodeMD5(byte[] toencode) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(toencode);
            return encodeHex(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String encodeMD5(String toencode) {
        return encodeMD5(toencode.getBytes());
    }

    public static String encodeHex(byte[] toencode) {
        StringBuilder sb = new StringBuilder(toencode.length * 2);
        for(byte b: toencode){
            sb.append(Integer.toHexString((b & 0xf0) >>> 4));
            sb.append(Integer.toHexString(b & 0x0f));
        }
        return sb.toString().toUpperCase();
    }
}
