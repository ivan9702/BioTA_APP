package com.startek.biota.app.utils;

import android.util.Base64;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public final class EncodeHelper {
    private static final byte[] DATA1;
    private static final byte[] DATA2;
    private static final int DATASIZE = 16;

    private class Ascii
    {
        public static final byte ACK = (byte) 6;
        public static final byte BEL = (byte) 7;
        public static final byte BS = (byte) 8;
        public static final byte CAN = (byte) 24;
        public static final byte CR = (byte) 13;
        public static final byte DC1 = (byte) 17;
        public static final byte DC2 = (byte) 18;
        public static final byte DC3 = (byte) 19;
        public static final byte DC4 = (byte) 20;
        public static final byte DEL = Byte.MAX_VALUE;
        public static final byte DLE = (byte) 16;
        public static final byte EM = (byte) 25;
        public static final byte ENQ = (byte) 5;
        public static final byte EOT = (byte) 4;
        public static final byte ESC = (byte) 27;
        public static final byte ETB = (byte) 23;
        public static final byte ETX = (byte) 3;
        public static final byte FF = (byte) 12;
        public static final byte FS = (byte) 28;
        public static final byte GS = (byte) 29;
        public static final byte HT = (byte) 9;
        public static final byte LF = (byte) 10;
        //@Beta
        public static final int MAX = 127;
        //@Beta
        public static final int MIN = 0;
        public static final byte NAK = (byte) 21;
        public static final byte NL = (byte) 10;
        public static final byte NUL = (byte) 0;
        public static final byte RS = (byte) 30;
        public static final byte SI = (byte) 15;
        public static final byte SO = (byte) 14;
        public static final byte SOH = (byte) 1;
        public static final byte SP = (byte) 32;
        public static final byte SPACE = (byte) 32;
        public static final byte STX = (byte) 2;
        public static final byte SUB = (byte) 26;
        public static final byte SYN = (byte) 22;
        public static final byte US = (byte) 31;
        public static final byte VT = (byte) 11;
        public static final byte XOFF = (byte) 19;
        public static final byte XON = (byte) 17;
    }

    static {
        DATA1 = new byte[]{(byte) -121, (byte) -6, Ascii.EM, (byte) -44, (byte) 0, (byte) -84, (byte) 48, (byte) -79, (byte) 48, (byte) -69, (byte) -3, (byte) 10, (byte) -36, Ascii.DEL, (byte) -117, Ascii.XON};
        DATA2 = new byte[]{(byte) -52, (byte) 109, Ascii.SPACE, (byte) -48, (byte) 119, (byte) 91, (byte) -24, (byte) 0, (byte) 33, (byte) 52, (byte) -95, (byte) -49, (byte) -94, (byte) 97, (byte) -21, Ascii.EM};
    }

    private static final byte[] merge() {
        byte[] data = new byte[DATASIZE];
        for (int i = 0; i < DATASIZE; i++) {
            data[i] = (byte) (DATA1[i] ^ DATA2[i]);
        }
        return data;
    }

    private static Key getKey() {
        return new SecretKeySpec(merge(), "AES");
    }

    public static byte[] encode(byte[] src) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(1, getKey());
            return cipher.doFinal(src);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (InvalidKeyException e3) {
            e3.printStackTrace();
        } catch (IllegalBlockSizeException e4) {
            e4.printStackTrace();
        } catch (BadPaddingException e5) {
            e5.printStackTrace();
        }
        return null;
    }

    public static byte[] decode(byte[] src) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(2, getKey());
            return cipher.doFinal(src);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e2) {
            e2.printStackTrace();
        } catch (NoSuchAlgorithmException e3) {
            e3.printStackTrace();
        } catch (NoSuchPaddingException e4) {
            e4.printStackTrace();
        } catch (InvalidKeyException e5) {
            e5.printStackTrace();
        }
        return null;
    }

    public static String encodeFromString(String src) {
        return Base64.encodeToString(encode(src.getBytes()), 0);
    }

    public static String decodeToString(String src) {
        return new String(decode(Base64.decode(src, 0)));
    }

    public static String encodeFromInt(int src) {
        return Base64.encodeToString(encode(Integer.toString(src).getBytes()), 0);
    }

    public static int decodeToInt(String src) {
        return Integer.parseInt(new String(decode(Base64.decode(src, 0))));
    }

    public static String encodeFromLong(long src) {
        return Base64.encodeToString(encode(Long.toString(src).getBytes()), 0);
    }

    public static long decodeToLong(String src) {
        return Long.parseLong(new String(decode(Base64.decode(src, 0))));
    }

    public static String encodeFromFloat(float src) {
        return Base64.encodeToString(encode(Float.toString(src).getBytes()), 0);
    }

    public static float decodeToFloat(String src) {
        return Float.parseFloat(new String(decode(Base64.decode(src, 0))));
    }

    public static String encodeFromDouble(double src) {
        return Base64.encodeToString(encode(Double.toString(src).getBytes()), 0);
    }

    public static double decodeToFDouble(String src) {
        return Double.parseDouble(new String(decode(Base64.decode(src, 0))));
    }
}
