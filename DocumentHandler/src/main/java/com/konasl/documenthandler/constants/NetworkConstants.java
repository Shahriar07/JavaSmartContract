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

    @Value("${com.konasl.documenthandler.channelname}")
    private String CHANNEL_NAME;
    public String getChannelName() {return CHANNEL_NAME;}

    // Chain code name in install instruction
    public static final String CHAIN_CODE_NAME = "documenthandler";
    // Chain code name in contract
    public static final String CHAIN_CODE_CONTRACT_NAME = "org.konasl.documentcontainer";

    // contract method name
    public static final String UPLOAD_METADATA_FUNCTION_NAME = "uploadMetadata";
    public static final String UPLOAD_DOC_CHUNK_FUNCTION_NAME = "uploadDocumentChunk";
    public static final String QUERY_METADATA_FUNCTION_NAME = "getDocumentMetadata";
    public static final String DOWNLOAD_CHUNK_FUNCTION_NAME = "getDocumentChunk";
    public static final String QUERY_DOC_HASH_FUNCTION_NAME = "getDocumentHash";


    @Value("${com.konasl.documenthandler.upload_chunk_size}")
    private int FILE_CHUNK_SIZE;
    public int getFileChunkSize() {
        return FILE_CHUNK_SIZE;
    }

    @Value("${com.konasl.documenthandler.client_key_path}")
    private String CLIENT_KEY_PATH;
    public String getClientKeyPath() {
        return CLIENT_KEY_PATH;
    }

    @Value("${com.konasl.documenthandler.client_wallet_path}")
    private String CLIENT_WALLET_PATH;
    public String getClientWalletPath() {
        return CLIENT_WALLET_PATH;
    }

    @Value("${com.konasl.documenthandler.network_conf_path}")
    private String NETWORK_CONF_PATH;
    public String getNetworkConfPath() {
        return NETWORK_CONF_PATH;
    }

    public static final int EVENT_LISTENER_TIME = 600;                  // Waiting time to wait for an event
    public static final int THREAD_SLEEP_TIME_FOR_EVENT = 1000;         // In milliseconds


    // User private key file name, will be stored in keystore directory
//    public static final String KEY_FILE_NAME = "a958af32f7cca0ebb1bc9e49450d49ad8d05580531c9939d72471a93562c8e1a_sk";
    @Value("${com.konasl.documenthandler.client_key}")
    private String KEY_FILE_NAME;
    public String getKeyFilePath() {
        return KEY_FILE_NAME;
    }

    @Value("${com.konasl.documenthandler.cert_file_path}")
    private String CERT_FILE_NAME;
    public String getCertFilePath() {
        return CERT_FILE_NAME;
    }

    @Value("${com.konasl.documenthandler.mspid}")
    private String MSPID;
    public String getMspId() {
        return MSPID;
    }

    @Value("${com.konasl.documenthandler.identityLabel}")
    private String IDENTITY_LABEL;
    public String getIdentityLabel() {
        return IDENTITY_LABEL;
    }

    @Value("${com.konasl.documenthandler.max_thread}")
    private int MAX_THREAD_COUNT;
    public int getMaxThreadCount() {
        return MAX_THREAD_COUNT;
    }
}
