```
package com.gmlive.inside;

import android.text.TextUtils;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//public class main {
//    private static String a;
//    private static String b;
//
//    public static String decode2(String str, String str2, String str3) {
//        SecretKeySpec secretKeySpec = new SecretKeySpec(str3.getBytes(), "AES");
//        try {
//            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            instance.init(1, secretKeySpec, new IvParameterSpec(str2.getBytes()));
//            return Base64.encodeToString(instance.doFinal(str.getBytes("utf-8")), 0);
//        } catch (Throwable th) {
////            k.c(th.getMessage());
//            return null;
//        }
//    }
//
//    public static String b(String str, String str2, String str3) {
////        if (TextUtils.isEmpty(str)) {
////            return null;
////        }
//        try {
//            byte[] decode = Base64.decode(str, 0);
//            SecretKeySpec secretKeySpec = new SecretKeySpec(str3.getBytes(), "AES");
//            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            instance.init(2, secretKeySpec, new IvParameterSpec(str2.getBytes()));
//            return new String(instance.doFinal(decode));
//        } catch (Throwable th) {
////            k.c(th.getMessage());
//            return null;
//        }
//    }
//
//    public static String a(byte[] bArr, String str) {
//        try {
//            SecretKeySpec secretKeySpec = new SecretKeySpec(str.getBytes(), "AES");
////            if (TextUtils.isEmpty(b)) {
////                b = a("AES/CBC/PKCS7Padding");
////            }
//            Cipher instance = Cipher.getInstance(b);
//            instance.init(1, secretKeySpec);
//            return Base64.encodeToString(instance.doFinal(bArr), 0);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    @Deprecated
//    public static String decode(String str, String str2) {
//        if (TextUtils.isEmpty(str)) {
//            return null;
//        }
//        try {
//            byte[] decode = Base64.decode(str, 0);
//            SecretKeySpec secretKeySpec = new SecretKeySpec(str2.getBytes(), "AES");
//            if (TextUtils.isEmpty(a)) {
//                a = a("AES/CBC/PKCS5Padding");
//            }
//            Cipher instance = Cipher.getInstance(a);
//            instance.init(2, secretKeySpec);
//            return new String(instance.doFinal(decode));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public static String a(String str) {
//        int[] iArr = new int[str.length()];
//        iArr[4] = 6;
//        iArr[5] = 1;
//        iArr[6] = 1;
//        return new String(a(str.getBytes(), iArr));
//    }
//
//    public static byte[] a(byte[] bArr, int[] iArr) {
//        if (bArr == null || bArr.length == 0 || iArr == null || iArr.length == 0) {
//            return bArr;
//        }
//        byte[] bArr2 = new byte[bArr.length];
//        for (int i = 0; i < bArr.length; i++) {
//            bArr2[i] = (byte) (bArr[i] ^ iArr[i % iArr.length]);
//        }
//        return bArr2;
//    }
//}
public class main {

    static String f3438a = "";

    public static String m4501a(String str, String str2) {
        try {
            byte[] decode = Base64.decode(str, 0);
            SecretKeySpec secretKeySpec = new SecretKeySpec(str2.getBytes(), "AES");
            if (f3438a.equals("")) {
                f3438a = m4500a("AES/CBC/PKCS5Padding");
            }
            Cipher instance = Cipher.getInstance(f3438a);
            instance.init(2, secretKeySpec);
            return new String(instance.doFinal(decode));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] m4504a(byte[] bArr, int[] iArr) {
        if (bArr == null || bArr.length == 0 || iArr == null || iArr.length == 0) {
            return bArr;
        }
        byte[] bArr2 = new byte[bArr.length];
        for (int i = 0; i < bArr.length; i++) {
            bArr2[i] = (byte) (bArr[i] ^ iArr[i % iArr.length]);
        }
        return bArr2;
    }

    public static String m4500a(String str) {
        int[] iArr = new int[str.length()];
        iArr[4] = 6;
        iArr[5] = 1;
        iArr[6] = 1;
        return new String(m4504a(str.getBytes(), iArr));
    }
}
```