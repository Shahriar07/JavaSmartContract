package com.konasl.documenthandler.protocol.services.download;

import com.konasl.documenthandler.auth.AuthUser;
import com.konasl.documenthandler.constants.NetworkConstants;
import com.konasl.documenthandler.protocol.ResponseCodeEnum;
import com.konasl.documenthandler.protocol.RestResponse;
import com.konasl.documenthandler.util.Utility;
import org.hyperledger.fabric.gateway.*;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class DownloadServiceImpl implements DownloadService
{

    @Autowired
    private AuthUser authUser;

    @Override
    public RestResponse downloadDocument(String fileName, String documentKey) {
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

        // Get file metadata from contract
        // Get chunk of files
            int chunkCount =  getChunkCountFromTheBlockChain(fileName, documentKey, contract);
            if (chunkCount < 1) {
                return new RestResponse(ResponseCodeEnum.DOCUMENT_NOT_EXIST);
            }
            Path temporaryPath = Paths.get("..", "download");
        // prepare actual file
           File file =  prepareFileFromChunks(temporaryPath,fileName, chunkCount, documentKey, contract);
           if (file != null ) {
              // String generatedFileHash = Utility.prepareSha256HashofFile(file);

               // return actual file
               long endTime = System.currentTimeMillis();
               System.out.println("End time :" + endTime + " Duration " + (endTime - startTime));
               return new RestResponse(ResponseCodeEnum.SUCCESS, file);
           }
            return new RestResponse(ResponseCodeEnum.FAILURE);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error : " + e.getMessage());
            return new RestResponse(ResponseCodeEnum.FAILURE, e.getMessage());
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
        private static File prepareFileFromChunks(Path temporaryPath, String fileName, int chunkCount, String documentKey, Contract contract) throws IOException, FileNotFoundException, GatewayException, TimeoutException, InterruptedException {

            if (fileName == null || contract == null || chunkCount < 1) {
                System.out.println("File data validation failed");
                return null;
            }
            System.out.println("newFileDirectoryPath " + temporaryPath + " fileName " + fileName);
            Path createdPath = null;
            // create a new directory if not exist
            if (!Files.exists(temporaryPath)) {
                System.out.println("Generate newFileDirectory " + temporaryPath);
                createdPath = Files.createDirectory(temporaryPath);
                if (createdPath == null) {
                    System.out.println("New directory Creation failed");
                    return null;
                }
            }
            if (Files.exists(temporaryPath)) {
                System.out.println("Directory exists ");
                Path newFilePath = temporaryPath.resolve(fileName);
                File originalFile = new File(newFilePath.toString());
                if (!originalFile.exists()) {

                    boolean isCreated = originalFile.createNewFile();
                    if (!isCreated) {
                        System.out.println("New File Creation failed !isCreated");
                        return null;
                    }
                }
                if (originalFile.exists()) {
                    try {
                        // use a loop
                        // read bytes from the chunk files
                        // and write bytes to the new file
                        for (int count = 0; count < chunkCount; count++) {
                            String chunkFileName = count + "__" + fileName;
                            // Download the document
                            long responseStartTime = System.currentTimeMillis();
                            byte[] response = contract.submitTransaction(NetworkConstants.QUERY_CHUNK_FUNCTION_NAME, fileName, documentKey, ""+count);
                            //byte[] response2 = hexStringToByteArray(responseString);
                            long writeStartTime = System.currentTimeMillis();
                            System.out.println("Response received for " + chunkFileName + " length " + response.length + " response duration " + (writeStartTime - responseStartTime));
                            if (response.length > 0) {
                                String decodedString = new String(response, UTF_8);
                                byte[] chunkBytes = hexStringToByteArray(decodedString);
                                writeBytesToFile(chunkBytes, originalFile, count != 0); // First time will be false to create a new file, after that append the file
                                long writeEndTime = System.currentTimeMillis();
                                System.out.println("Write duration time : " + (writeEndTime-writeStartTime));
                            }
                        }
                        // return the original file
                        return originalFile;
                    } catch (GatewayRuntimeException e) {
                        e.printStackTrace();
                        System.out.println("Error : " + e.getMessage());
                        //	throw e;
                    }
                }
            }
            System.out.println("New File Creation failed newGeneratedFile");
            return null;

        }



        /**
         * Upload file metadata to the blockchain
         * Contract prepares an token to upload the chunk
         *
         * @param fileName
         * @param documentKey
         * @param contract
         * @return chunkCount of uploaded document
         * @throws InterruptedException
         * @throws TimeoutException
         * @throws ContractException
         */
        private int getChunkCountFromTheBlockChain(String fileName, String documentKey, Contract contract) throws InterruptedException, TimeoutException, ContractException {
            //validate input data
            if (fileName == null || "".equals(fileName)) {
                System.out.println("Invalid File Name");
                return 0;
            }
            if (documentKey == null || "".equals(documentKey)) {
                System.out.println("Invalid File token");
                return 0;
            }
            if (contract == null) {
                System.out.println("Invalid contract ");
                return 0;
            }

            //Send metadata to the blockchain
            byte[] response = contract.submitTransaction(NetworkConstants.QUERY_CHUNK_COUNT_FUNCTION_NAME, fileName, documentKey);
            if (response.length == 0) {
                return 0;
            }
            int chunkCount = Integer.parseInt(new String(response, UTF_8));
            System.out.println("Chunk Count : " + chunkCount);
            return chunkCount;
        }
}
