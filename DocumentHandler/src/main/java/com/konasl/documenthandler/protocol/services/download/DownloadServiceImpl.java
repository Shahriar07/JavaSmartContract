package com.konasl.documenthandler.protocol.services.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.konasl.documenthandler.auth.AuthUser;
import com.konasl.documenthandler.constants.NetworkConstants;
import com.konasl.documenthandler.protocol.ResponseCodeEnum;
import com.konasl.documenthandler.protocol.RestResponse;
import com.konasl.documenthandler.protocol.apidata.DocumentContainer;
import com.konasl.documenthandler.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static com.konasl.documenthandler.util.Utility.hexStringToByteArray;
import static com.konasl.documenthandler.util.Utility.writeBytesToFile;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/14/2019 12:30
 */

@Slf4j
@Service
public class DownloadServiceImpl implements DownloadService
{

    private Logger logger = LoggerFactory.getLogger(DownloadServiceImpl.class);

    @Autowired
    private AuthUser authUser;

    @Autowired
    Utility utility;

    @Override
    public RestResponse downloadDocument(String fileName, String documentKey) {
        long startTime = System.currentTimeMillis();
        logger.info("Start time : " + startTime);
        Network network = authUser.authUserandGenerateNetwork();
        if (network == null) {
            logger.error("Network generation failed");
            return new RestResponse(ResponseCodeEnum.BLOCKCHAIN_NETWORK_CONNECTION_FAILED);
        }

        try {
            // Get addressability to document contract
            logger.error("Use org.konasl.documentcontainer smart contract.");
            Contract contract = network.getContract(NetworkConstants.CHAIN_CODE_NAME, NetworkConstants.CHAIN_CODE_CONTRACT_NAME);

        // Get file metadata from contract
        // Get chunk of files
            String documentInfo =  getMetadataFromTheBlockChain(documentKey, contract);
            if (documentInfo == null) {
                logger.error("Document metadata not found");
                return new RestResponse(ResponseCodeEnum.DOCUMENT_NOT_EXIST);
            }
            DocumentContainer documentContainer = new ObjectMapper().readValue(documentInfo, DocumentContainer.class);
            if (documentContainer == null || documentContainer.getChunkCount() < 1) {
                return new RestResponse(ResponseCodeEnum.DOCUMENT_NOT_EXIST);
            }
            Path temporaryPath = Paths.get("..", "download");
        // prepare actual file
           File file =  prepareFileFromChunks(temporaryPath,documentContainer.getFileName(), documentContainer.getChunkCount(), documentKey, contract);
           if (file != null ) {
              // String generatedFileHash = Utility.prepareSha256HashofFile(file);

               // return actual file
               long endTime = System.currentTimeMillis();
               logger.info("End time :" + endTime + " Duration " + (endTime - startTime));
               return new RestResponse(ResponseCodeEnum.SUCCESS, file);
           }
            return new RestResponse(ResponseCodeEnum.FAILURE);
        } catch (Exception e){
            e.printStackTrace();
            logger.error("Error : " + e.getMessage());
            return new RestResponse(ResponseCodeEnum.FAILURE, e.getMessage());
        }
    }

    /**
     * Verify the document hash from the smart contract
     *
     * @param fileName
     * @param documentKey
     * @param documentHash
     * @return
     */
    @Override
    public RestResponse verifyDocument(String fileName, String documentKey, String documentHash) {

        if (fileName == null || documentKey == null || documentHash == null || fileName.isEmpty()
            || documentKey.isEmpty() || documentHash.isEmpty())
        {
            logger.error("Input data verification failed");
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
            Contract contract = network.getContract(NetworkConstants.CHAIN_CODE_NAME, NetworkConstants.CHAIN_CODE_CONTRACT_NAME);

            byte[] response = contract.submitTransaction(NetworkConstants.QUERY_DOC_HASH_FUNCTION_NAME, fileName, documentKey);
            if (response.length > 0) {
                String decodedString = new String(response, UTF_8);
                if (documentHash.equalsIgnoreCase(decodedString)) {
                    logger.info("verify duration : " + (System.currentTimeMillis()-startTime));
                    return new RestResponse(ResponseCodeEnum.SUCCESS);
                }
                else {
                    logger.error("verify duration : " + (System.currentTimeMillis()-startTime));
                    return new RestResponse(ResponseCodeEnum.DOCUMENT_HASH_VERIFICATION_FAILED);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error : " + e.getMessage());
        }
        logger.info("verify duration : " + (System.currentTimeMillis()-startTime));
        return new RestResponse(ResponseCodeEnum.DOCUMENT_HASH_VERIFICATION_FAILED);
    }

    /**
     * retrieves the document name from the smart contract
     *
     * @param documentKey
     * @return
     */

    @Override
    public RestResponse getDocumentName(String documentKey) {
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

            // Get file metadata from contract
            // Get chunk of files
            String documentInfo = getMetadataFromTheBlockChain(documentKey, contract);
            if (documentInfo == null) {
                logger.error("Document metadata not found");
                return new RestResponse(ResponseCodeEnum.DOCUMENT_NOT_EXIST);
            }
            DocumentContainer documentContainer = new ObjectMapper().readValue(documentInfo, DocumentContainer.class);
            if (documentContainer == null || documentContainer.getChunkCount() < 1) {
                logger.error("Document metadata not found");
                return new RestResponse(ResponseCodeEnum.DOCUMENT_NOT_EXIST);
            }
            logger.info("verify duration : " + (System.currentTimeMillis()-startTime));
            return new RestResponse(ResponseCodeEnum.SUCCESS, documentContainer.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error : " + e.getMessage());
            return new RestResponse(ResponseCodeEnum.BLOCK_INFO_GENERATION_FAILED);
        }
    }

    /**
         * Retrieves the file chunks from the blockchain network
         * Merge the file chunks to prepare the original file
                *
         * @param temporaryPath
                * @param fileName
                * @param chunkCount
                * @param contract
                * @return
         * @throws IOException
                * @throws FileNotFoundException
                * @throws GatewayException
                * @throws TimeoutException
                * @throws InterruptedException
                */
        private File prepareFileFromChunks(Path temporaryPath, String fileName, int chunkCount, String documentKey, Contract contract) throws IOException, FileNotFoundException, GatewayException, TimeoutException, InterruptedException {

            if (fileName == null || contract == null || chunkCount < 1) {
                logger.error("File data validation failed");
                return null;
            }
            logger.info("newFileDirectoryPath " + temporaryPath + " fileName " + fileName);
            Path createdPath = null;
            // create a new directory if not exist
            if (!Files.exists(temporaryPath)) {
                logger.info("Generate newFileDirectory " + temporaryPath);
                createdPath = Files.createDirectory(temporaryPath);
                if (createdPath == null) {
                    logger.error("New directory Creation failed");
                    return null;
                }
            }
            if (Files.exists(temporaryPath)) {
                logger.info("Directory exists ");
                Path newFilePath = temporaryPath.resolve(fileName);
                File originalFile = new File(newFilePath.toString());
                if (!originalFile.exists()) {

                    boolean isCreated = originalFile.createNewFile();
                    if (!isCreated) {
                        logger.error("New File Creation failed !isCreated");
                        return null;
                    }
                }
                if (originalFile.exists()) {
                    try {
                        String chunkKeyPrefix = utility.prepareChunkKeyPrefix(fileName, documentKey);

                        // use a loop
                        // read bytes from the chunk files
                        // and write bytes to the new file
                        for (int count = 0; count < chunkCount; count++) {
                            String chunkFileName = count + "__" + fileName;
                            // Download the document
                            long responseStartTime = System.currentTimeMillis();
                            byte[] response = contract.submitTransaction(NetworkConstants.DOWNLOAD_CHUNK_FUNCTION_NAME, fileName, documentKey, chunkKeyPrefix, ""+count);
                            //byte[] response2 = hexStringToByteArray(responseString);
                            long writeStartTime = System.currentTimeMillis();
                            logger.info("Response received for " + chunkFileName + " length " + response.length + " response duration " + (writeStartTime - responseStartTime));
                            if (response.length > 0) {
                                String decodedString = new String(response, UTF_8);
                                byte[] chunkBytes = hexStringToByteArray(decodedString);
                                writeBytesToFile(chunkBytes, originalFile, count != 0); // First time will be false to create a new file, after that append the file
                                long writeEndTime = System.currentTimeMillis();
                                logger.info("Write duration time : " + (writeEndTime-writeStartTime));
                            }
                        }
                        // return the original file
                        return originalFile;
                    } catch (GatewayRuntimeException e) {
                        e.printStackTrace();
                        logger.error("Error : " + e.getMessage());
                        //	throw e;
                    }
                }
            }
            logger.info("New File Creation failed newGeneratedFile");
            return null;
        }


        /**
         * Retrieves the file metadata from the blockchain
         *
         * @param documentKey
         * @param contract
         * @return chunkCount of uploaded document
         * @throws InterruptedException
         * @throws TimeoutException
         * @throws ContractException
         */
        private String getMetadataFromTheBlockChain( String documentKey, Contract contract) throws InterruptedException, TimeoutException, ContractException {
            if (documentKey == null || "".equals(documentKey)) {
                logger.error("Invalid File token");
                return null;
            }
            if (contract == null) {
                logger.error("Invalid contract ");
                return null;
            }

            //Get chunkCount from the blockchain
            byte[] response = contract.submitTransaction(NetworkConstants.QUERY_METADATA_FUNCTION_NAME, documentKey);
            if (response.length == 0) {
                return null;
            }
            String documentInfo = new String(response, UTF_8);
            return documentInfo;
        }
}
