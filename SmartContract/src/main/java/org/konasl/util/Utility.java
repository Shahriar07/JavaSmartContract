package org.konasl.util;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.konasl.constants.Constants.HASH_ALGORITHM;
import static org.konasl.constants.Constants.HASH_UPDATE_BUFFER_SIZE;

/**
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/11/2019 17:22
 */
public class Utility {

    /**
     * Performs sha-256 hash
     *
     * @param data takes the input data to perform sha-256 hash
     * @return the array of bytes as hex string of the hash value ("9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08")
     * @throws NoSuchAlgorithmException if no support found for sha-256 hash algorithm
     */

    public static String prepareSha256Hash(String data) throws NoSuchAlgorithmException {
        if (data == null || data.length() == 0) return null;
        byte[] buffer = data.getBytes();
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        digest.update(buffer, 0, data.length());
        return DatatypeConverter.printHexBinary(digest.digest());
    }


    /**
     * Convert a byte array to a hex string
     *
     * @param {byte[]} byte array
     */
    public static String byteArrayToString(byte[] bytes) {
		long start = System.currentTimeMillis();
        char [] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char [] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        long end = System.currentTimeMillis();
        System.out.println("byteArrayToString duration : " + (end-start));
        return new String(hexChars);
    }

    /**
     * Converts hex string to a byte array
     *
     * @param {byte[]} byte array
     */
    public static byte[] hexStringToByteArray(String s) {
  		long start = System.currentTimeMillis();
        int len = s.length();
        byte[] data = new byte[len / 2];
        for(int i=0; i < len; i +=2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        long end = System.currentTimeMillis();
        System.out.println("hexStringToByteArray duration : " + (end-start));
        return data;
    }
}
