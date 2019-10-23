package com.konasl.documenthandler.protocol.services.upload;

import com.konasl.documenthandler.auth.AuthUser;
import com.konasl.documenthandler.constants.NetworkConstants;
import com.konasl.documenthandler.protocol.ResponseCodeEnum;
import com.konasl.documenthandler.protocol.RestResponse;
import com.konasl.documenthandler.protocol.apidata.UploadResponse;
import com.konasl.documenthandler.util.Utility;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.GatewayException;
import org.hyperledger.fabric.gateway.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static com.konasl.documenthandler.util.Utility.byteArrayToString;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class UploadServiceImpl implements UploadService{

    @Autowired
    private AuthUser authUser;

    @Autowired
    NetworkConstants networkConstants;

    @Autowired
    Utility utility;

    private long finishedUploadTask = 0;
    private final Object lock = new Object();

    public void incrementFinished() {
        synchronized (lock) {
            finishedUploadTask++;
            System.out.println("Finished " + finishedUploadTask);
        }
    }

    public long resetFinished() {
        synchronized (lock) {
            long temp = finishedUploadTask;
            finishedUploadTask = 0;
            return temp;
        }
    }
    public long getFinishedUploadTask() {
        synchronized (lock) {
            long temp = finishedUploadTask;
            return temp;
        }
    }


    /**
     * Upload a document to the blockchain
     *
     * @param file document to upload
     * @param documentKey unique key to identify the document
     * @return
     */
    @Override
    public RestResponse uploadDocument(MultipartFile file, String documentKey) {

        if (file == null) {
            System.out.println("Argument type mismatch. Add the file path as agrument to upload a document");
            return new RestResponse(ResponseCodeEnum.DOCUMENT_DATA_VERIFICATION_FAILED);
        }
        long startTime = System.currentTimeMillis();
        System.out.println("Start time : " + startTime);
        Network network = authUser.authUserandGenerateNetwork();
        if (network == null) {
            System.out.println("Network generation failed");
            return new RestResponse(ResponseCodeEnum.BLOCKCHAIN_NETWORK_CONNECTION_FAILED);
        }

        try {
            // Get addressability to document contract
            System.out.println("Use org.konasl.documentcontainer smart contract.");
            Contract contract = network.getContract(NetworkConstants.CHAIN_CODE_NAME, NetworkConstants.CHAIN_CODE_CONTRACT_NAME);

            // Insert document to fabric network
            System.out.println("Submit upload document transaction.");
            RestResponse uploadResponse = uploadFileInChunks(file, networkConstants.getFileChunkSize(), contract, documentKey); // file, maxchunk size in bytes, contract

            if (!ResponseCodeEnum.SUCCESS.getCode().equals(uploadResponse.getResponseCode())) {
                System.out.println("Upload failed : " + uploadResponse.getResponseMessage());
            }

            long endTime = System.currentTimeMillis();
            System.out.println("End time :" + endTime + " Duration " + (endTime-startTime));
            return uploadResponse;

        } catch (GatewayException | IOException | TimeoutException | InterruptedException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new RestResponse(ResponseCodeEnum.FAILURE, e.getMessage());
        }
    }

    /**
     * Upload thread, to upload the document chunks to the blockchain network
     */
    class UploadThread extends Thread {
        Contract contract;
        String fileName;
        String chunkString;
        String counter;
        String documentKey;
        String chunkKeyPrefix;
        public UploadThread(Contract contract, String fileName, String chunkString, String documentKey, String chunkKeyPrefix, String counter) {
            this.contract = contract;
            this.fileName = fileName;
            this.chunkString = chunkString;
            this.counter = counter;
            this.documentKey = documentKey;
            this.chunkKeyPrefix = chunkKeyPrefix;
        }
        public void run() {
            long startTime = System.currentTimeMillis();
            System.out.println("startTime : " + startTime + " chunk " + counter);
            byte[] response = new byte[0];
            try {
                response = contract.submitTransaction(NetworkConstants.UPLOAD_DOC_CHUNK_FUNCTION_NAME, fileName, chunkString, chunkKeyPrefix, documentKey, ""+counter);
            } catch (ContractException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            System.out.println(fileName + "_" + counter + " transaction response : " + new String(response, UTF_8) + " Duration " + (endTime-startTime));
            incrementFinished();
        }

    }

    /**
     * Upload the file in chunks if the file size is larger than the chunk size
     * performs multiple transaction to the blockchain to upload the file
     *
     * @param file
     * @param chunkSize
     * @param contract
     * @return
     * @throws IOException
     * @throws GatewayException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    private RestResponse uploadFileInChunks(MultipartFile file, int chunkSize, Contract contract, String documentKey) throws IOException, GatewayException, TimeoutException, InterruptedException, NoSuchAlgorithmException {
        if (file == null || chunkSize < 1) {
            System.out.println("File data validation failed");
            return new RestResponse(ResponseCodeEnum.FAILURE, ResponseCodeEnum.DOCUMENT_DATA_VERIFICATION_FAILED);
        }
        long allStartTime = System.currentTimeMillis();
        InputStream inputStream = file.getInputStream();
        long length = file.getSize();

        String fileName = file.getOriginalFilename();
        System.out.println("File size : " + length + ", File Name : " + fileName);
        String fileHash = Utility.prepareSha256HashofMultipartFile(file);
        System.out.println("File hash : " + fileHash);
        long chunkCount = (length / chunkSize) + 1;

        String chunkKeyPrefix = utility.prepareChunkKeyPrefix(fileName, documentKey);

        // input the metadata to the network
        String uploadToken = uploadFileMetadataToTheBlockchain(fileName, chunkCount, fileHash, documentKey, chunkKeyPrefix, contract);
        if (uploadToken == null) {
            return new RestResponse(ResponseCodeEnum.FAILURE, ResponseCodeEnum.DOCUMENT_DATA_VERIFICATION_FAILED);
        }

        long start = 0;
        long counter = 0;
        ArrayList<UploadThread> uploadThreads = new ArrayList<>();// = new UploadThread(contract, fileName, chunkString, uploadToken, ""+counter);
        while (start < length) {
            long end = Math.min(length, start + chunkSize);
            byte[] buffer = new byte[(int) (end - start)];
            long readBufferSize = inputStream.read(buffer);
            if (readBufferSize > 0) {
                String chunkString = byteArrayToString(buffer);
                uploadThreads.add(new UploadThread(contract, fileName, chunkString, documentKey, chunkKeyPrefix, ""+counter));
//                long startTime = System.currentTimeMillis();
//                byte[] response = contract.submitTransaction(NetworkConstants.UPLOAD_DOC_CHUNK_FUNCTION_NAME, fileName, chunkString, chunkKeyPrefix, documentKey, ""+counter);
//                long endTime = System.currentTimeMillis();
//                System.out.println(fileName + "_" + counter + " transaction response : " + new String(response, UTF_8) + " Duration " + (endTime-startTime));
            }
            start += chunkSize;
            ++counter;
        }
        System.out.println("Chunk Count " + counter + " chunkSize " + chunkSize);
        for(UploadThread uploadThread : uploadThreads){
            uploadThread.start();
        }
        System.out.println("Thread started  " + (System.currentTimeMillis() - allStartTime));
        while (true) {
            if (getFinishedUploadTask() == counter) break;
            //System.out.println("Counter " + counter + " Finished " + finishedUploadTask);
        }
        resetFinished();
        long endTime = System.currentTimeMillis();
        System.out.println(" Upload Duration " + (endTime-allStartTime) + " for chunks : " + counter);
        inputStream.close();
        return new RestResponse(ResponseCodeEnum.SUCCESS, new UploadResponse(counter, fileHash));
    }

    /**
     * Upload file metadata to the blockchain
     * Contract prepares a token to upload the chunk
     *
     * @param fileName
     * @param chunkCount
     * @param fileHash
     * @param contract
     * @return Token to upload the file chunks to the contract
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ContractException
     */
    private String uploadFileMetadataToTheBlockchain(String fileName, long chunkCount, String fileHash, String documentKey, String chunkKeyPrefix, Contract contract) throws InterruptedException, TimeoutException, ContractException {
        //validate input data
        if (fileName == null || "".equals(fileName)) {
            System.out.println("Invalid File Name");
            return null;
        }
        if (fileHash == null || "".equals(fileHash)) {
            System.out.println("Invalid File Hash");
            return null;
        }
        if (contract == null || chunkCount < 1) {
            System.out.println("Invalid contract or chunk count");
            return null;
        }

        //Send metadata to the blockchain
        byte[] response = contract.submitTransaction(NetworkConstants.UPLOAD_METADATA_FUNCTION_NAME, fileName, fileHash, documentKey, chunkKeyPrefix, ""+chunkCount);
        if (response.length == 0) {
            return null;
        }
        String token = new String(response, UTF_8);
        System.out.println("Token value : " + token);
        return token;
    }
}
