package com.konasl.documenthandler.protocol.services.upload;

import com.konasl.documenthandler.auth.AuthUser;
import com.konasl.documenthandler.constants.NetworkConstants;
import com.konasl.documenthandler.protocol.ResponseCodeEnum;
import com.konasl.documenthandler.protocol.RestResponse;
import com.konasl.documenthandler.protocol.apidata.UploadResponse;
import com.konasl.documenthandler.protocol.services.download.DownloadServiceImpl;
import com.konasl.documenthandler.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.GatewayException;
import org.hyperledger.fabric.gateway.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static com.konasl.documenthandler.util.Utility.byteArrayToString;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService{

    private Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);

    @Autowired
    private AuthUser authUser;

    @Autowired
    private NetworkConstants networkConstants;

    @Autowired
    private Utility utility;

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
            logger.error("Argument type mismatch. Add the file path as agrument to upload a document");
            return new RestResponse(ResponseCodeEnum.DOCUMENT_DATA_VERIFICATION_FAILED);
        }
        long startTime = System.currentTimeMillis();
        logger.info("Start time : " + startTime);
        Network network = authUser.authUserandGenerateNetwork();
        if (network == null) {
            logger.error("Network generation failed");
            return new RestResponse(ResponseCodeEnum.BLOCKCHAIN_NETWORK_CONNECTION_FAILED);
        }

        try {
            // Get addressability to document contract
            logger.info("Use org.konasl.documentcontainer smart contract.");
            Contract contract = network.getContract(NetworkConstants.CHAIN_CODE_NAME, NetworkConstants.CHAIN_CODE_CONTRACT_NAME);

            // Insert document to fabric network
            logger.info("Submit upload document transaction.");
            RestResponse uploadResponse = uploadFileInChunks(file, networkConstants.getFileChunkSize(), contract, documentKey); // file, maxchunk size in bytes, contract

            if (!ResponseCodeEnum.SUCCESS.getCode().equals(uploadResponse.getResponseCode())) {
                logger.error("Upload failed : " + uploadResponse.getResponseMessage());
            }

            long endTime = System.currentTimeMillis();
            logger.info("End time :" + endTime + " Duration " + (endTime-startTime));
            return uploadResponse;

        } catch (GatewayException | IOException | TimeoutException | InterruptedException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new RestResponse(ResponseCodeEnum.FAILURE, e.getMessage());
        }
    }

    /**
     * Upload thread, to upload the document chunks to the blockchain network
     */
    class UploadThread implements Runnable {
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
            logger.info("startTime : " + startTime + " chunk " + counter);
            byte[] response = new byte[0];
            try {
                response = contract.submitTransaction(NetworkConstants.UPLOAD_DOC_CHUNK_FUNCTION_NAME, chunkString, chunkKeyPrefix, documentKey, ""+counter);
            } catch (ContractException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            logger.info(fileName + "_" + counter + " transaction response : " + new String(response, UTF_8) + " Duration " + (endTime-startTime));
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
            logger.error("File data validation failed");
            return new RestResponse(ResponseCodeEnum.FAILURE, ResponseCodeEnum.DOCUMENT_DATA_VERIFICATION_FAILED);
        }
        long allStartTime = System.currentTimeMillis();
        InputStream inputStream = file.getInputStream();
        long length = file.getSize();

        String fileName = file.getOriginalFilename();
        logger.info("File size : " + length + ", File Name : " + fileName);
        String fileHash = Utility.prepareSha256HashofMultipartFile(file);
        logger.info("File hash : " + fileHash);
        long chunkCount = (length / chunkSize) + 1;

        String chunkKeyPrefix = utility.prepareChunkKeyPrefix(fileName, documentKey);

        // input the metadata to the network
        String uploadToken = uploadFileMetadataToTheBlockchain(fileName, chunkCount, fileHash, documentKey, chunkKeyPrefix, contract);
        if (uploadToken == null) {
            return new RestResponse(ResponseCodeEnum.FAILURE, ResponseCodeEnum.DOCUMENT_DATA_VERIFICATION_FAILED);
        }

        ExecutorService executor = Executors.newFixedThreadPool(networkConstants.getMaxThreadCount());
        long start = 0;
        long counter = 0;
        while (start < length) {
            long end = Math.min(length, start + chunkSize);
            byte[] buffer = new byte[(int) (end - start)];
            long readBufferSize = inputStream.read(buffer);
            if (readBufferSize > 0) {
                String chunkString = byteArrayToString(buffer);

                // parallel execution to upload document
                executor.execute(new UploadThread(contract, fileName, chunkString, documentKey, chunkKeyPrefix, ""+counter));

                // Serial execution to upload document
//                long startTime = System.currentTimeMillis();
//                byte[] response = contract.submitTransaction(NetworkConstants.UPLOAD_DOC_CHUNK_FUNCTION_NAME, fileName, chunkString, chunkKeyPrefix, documentKey, ""+counter);
//                long endTime = System.currentTimeMillis();
//                logger.info(fileName + "_" + counter + " transaction response : " + new String(response, UTF_8) + " Duration " + (endTime-startTime));
            }
            start += chunkSize;
            ++counter;
        }
        logger.info("Chunk Count " + counter + " chunkSize " + chunkSize);
        logger.info("Thread started  " + (System.currentTimeMillis() - allStartTime));
        executor.shutdown();
        while (!executor.isTerminated()) {
            //if (getFinishedUploadTask() == counter) break;
            //logger.info("Counter " + counter + " Finished " + finishedUploadTask);
        }
        long totalTime = System.currentTimeMillis() - allStartTime;

        logger.info(" Upload Duration " + totalTime + " for chunks : " + counter);
        inputStream.close();
        return new RestResponse(ResponseCodeEnum.SUCCESS, new UploadResponse(chunkCount, fileHash, totalTime));
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
            logger.error("Invalid File Name");
            return null;
        }
        if (fileHash == null || "".equals(fileHash)) {
            logger.error("Invalid File Hash");
            return null;
        }
        if (contract == null || chunkCount < 1) {
            logger.error("Invalid contract or chunk count");
            return null;
        }

        //Send metadata to the blockchain
        byte[] response = contract.submitTransaction(NetworkConstants.UPLOAD_METADATA_FUNCTION_NAME, fileName, fileHash, documentKey, chunkKeyPrefix, ""+chunkCount);
        if (response.length == 0) {
            return null;
        }
        String token = new String(response, UTF_8);
        logger.info("Token value : " + token);
        return token;
    }
}
