package com.konasl.documenthandler.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Contains the constant values for the fabric network
 *
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/10/2019 15:16
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class NetworkConstants {

    public static final String CHANNEL_NAME = "konachannel";
    // Chain code name in install instruction
    public static final String CHAIN_CODE_NAME = "documenthandler";
    // Chain code name in contract
    public static final String CHAIN_CODE_CONTRACT_NAME = "org.konasl.documentcontainer";
    public static final String DOWNLOAD_EVENT_NAME = "GetDocument";
    public static final String UPLOAD_EVENT_NAME = "AddDocument";

    // contract method name
    public static final String UPLOAD_METADATA_FUNCTION_NAME = "uploadMetadata";
    public static final String UPLOAD_DOC_CHUNK_FUNCTION_NAME = "uploadDocumentChunk";
    public static final String QUERY_CHUNK_COUNT_FUNCTION_NAME = "getDocumentChunkCount";
    public static final String QUERY_CHUNK_FUNCTION_NAME = "getDocumentChunk";


    @Value("${com.konasl.documenthandler.upload_chunk_size}")
    private int FILE_CHUNK_SIZE;
    public int getFileChunkSize() {
        System.out.println("Chunk Size " + FILE_CHUNK_SIZE);
        return FILE_CHUNK_SIZE;
    }

    // Takes almost 6 seconds to upload 5k data to 1 peer and 1 orderer
//    public static final int FILE_CHUNK_SIZE = 5120;                     // File Chunk Size to split a large file
    // Takes almost 20 seconds to upload 10k data to 1 peer and 1 orderer
//    public static final int FILE_CHUNK_SIZE = 10240;                     // File Chunk Size to split a large file


    public static final int EVENT_LISTENER_TIME = 600;                  // Waiting time to wait for an event
    public static final int THREAD_SLEEP_TIME_FOR_EVENT = 1000;         // In milliseconds


    // User private key file name, will be stored in keystore directory
//    public static final String KEY_FILE_NAME = "a958af32f7cca0ebb1bc9e49450d49ad8d05580531c9939d72471a93562c8e1a_sk";
    @Value("${com.konasl.documenthandler.client_key}")
    private String KEY_FILE_NAME;

    public String getKeyFileName() {
        return KEY_FILE_NAME;
    }
}
