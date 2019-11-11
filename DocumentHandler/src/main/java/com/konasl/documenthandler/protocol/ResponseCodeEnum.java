package com.konasl.documenthandler.protocol;

/**
 * Different API in the server receives and process different requests,
 * so there will be many types of access violation or error occurs in the system.
 * ResponseCodeEnum will store the error types with a success/error code and a message
 *
 * @author  H. M. Shahriar
 */


public enum ResponseCodeEnum {

    // New Failure cases will be added here
    SUCCESS                                     ("0000", "Success"),
    FAILURE                                     ("0001", "Failure"),
    DOCUMENT_DATA_VERIFICATION_FAILED           ("0002", "Document data verification failed"),
    BLOCKCHAIN_NETWORK_CONNECTION_FAILED        ("0003", "Blockchain network connection failed"),
    UPLOAD_TOKEN_GENERATION_FAILED              ("0004", "Upload token generation failed"),
    DOCUMENT_HASH_VERIFICATION_FAILED           ("0005", "Document hash verification failed"),
    DOCUMENT_NOT_EXIST                          ("0006", "Document does not exist"),
    BLOCK_INFO_GENERATION_FAILED                ("0007", "Block information parsing failed");

    private final String code;
    private final String message;

    ResponseCodeEnum(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ResponseCodeEnum findByCode(String code){
        for (ResponseCodeEnum responseCodeEnum: values()){
            if (responseCodeEnum.getCode().equalsIgnoreCase(code)){
                return responseCodeEnum;
            }
        }
        return null;
    }
}
