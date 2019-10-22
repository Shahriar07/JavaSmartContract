package com.konasl.documenthandler.util;

import com.konasl.documenthandler.constants.Constants;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.konasl.documenthandler.constants.Constants.HASH_ALGORITHM;
import static com.konasl.documenthandler.constants.Constants.HASH_UPDATE_BUFFER_SIZE;
import static java.lang.String.format;

/**
 * Performs utility operation
 *
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/10/2019 18:53
 */

@Component
public class Utility {

    /**
     * Common utility function to print data
     *
     * @param format
     * @param args
     */
    public static void out(String format, Object... args) {
        System.err.flush();
        System.out.flush();

        System.out.println(format(format, args));
        System.err.flush();
        System.out.flush();

    }

    /**
     * Converts byte array to hex string
     *
     * @param bytes
     * @return
     */
    public static String byteArrayToString(byte[] bytes) {

        char [] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char [] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     *  Convert hex string to byte array
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Write input byte array to the provided file
     *
     * @param fileBytes
     * @param file
     * @param append
     */
    public static void writeBytesToFile(byte[] fileBytes, File file, boolean append) {
        OutputStream opStream = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            opStream = new FileOutputStream(file, append);
            opStream.write(fileBytes);
            opStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (opStream != null) opStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void readStringDataFromFile(File file) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null)
                System.out.println(st);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs sha-256 hash
     *
     * @param document takes the multipart document to perform sha-256 hash
     * @return the array of bytes as hex string of the hash value "9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08"
     * @throws NoSuchAlgorithmException if no support found for sha-256 hash algorithm
     * @throws IOException              if failed to generate inputstream from multipart file
     */

    public static String prepareSha256HashofMultipartFile(MultipartFile document) throws NoSuchAlgorithmException, IOException {
        long documentSize = document.getSize();

        // use the document size as buffer size if the size is smaller than 10kb
        int bufferSize = (int) (documentSize > HASH_UPDATE_BUFFER_SIZE ? HASH_UPDATE_BUFFER_SIZE : documentSize);
        byte[] buffer = new byte[bufferSize];
        int count;
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        BufferedInputStream bis = new BufferedInputStream(document.getInputStream());
        while ((count = bis.read(buffer)) > 0) {
            digest.update(buffer, 0, count);
        }
        bis.close();
        return DatatypeConverter.printHexBinary(digest.digest());
    }


    /**
     * Performs sha-256 hash of a file content
     *
     * @param document takes the file to perform sha-256 hash
     * @return the array of bytes as hex string of the hash value "9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08"
     * @throws NoSuchAlgorithmException if no support found for sha-256 hash algorithm
     * @throws IOException              if failed to generate inputstream from multipart file
     */

    public static String prepareSha256HashofFile(File document) throws NoSuchAlgorithmException, IOException {
        if(document == null) return null;
        long documentSize = document.length();

        // use the document size as buffer size if the size is smaller than 10kb
        int bufferSize = (int) (documentSize > HASH_UPDATE_BUFFER_SIZE ? HASH_UPDATE_BUFFER_SIZE : documentSize);
        byte[] buffer = new byte[bufferSize];
        int count;
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(document));
        while ((count = bis.read(buffer)) > 0) {
            digest.update(buffer, 0, count);
        }
        bis.close();
        return DatatypeConverter.printHexBinary(digest.digest());
    }

    /**
     * Prepare http Servlet response from a file to enable downloading the file
     *
     * @param file
     * @param response
     * @return
     * @throws IOException
     */
    public HttpServletResponse prepareResponseFromFile(File file, HttpServletResponse response) throws IOException {
        if(file == null) {
            response.setStatus(Constants.FILE_DOWNLOAD_ERROR);
            return response;
        }
        String fileName = file.getName();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", String.format("inline;filename=\"" + fileName + "\""));
        response.setContentLength((int) file.length());
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
            System.out.println("Raw Data Download response sent");
        } catch (IOException e) {
            System.out.println("Error while download raw data file {}" + fileName);
        } finally {
            if (inputStream != null)
                inputStream.close();
            System.out.println("InputStream of file: {} Closed" + fileName);

        }
        if (file.exists()) file.delete();
        return response;
    }
}
