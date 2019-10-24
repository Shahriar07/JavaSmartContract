package com.konasl.documenthandler.protocol;

/**
 * RestResponse is the base response for external api call
 *
 * @author H. M. Shahriar
 */

@Data

public class RestResponse {

    private String responseCode;
    private String responseMessage;
    private Object data;

    public RestResponse(ResponseCodeEnum responseEnum){
        this.responseCode = responseEnum.getCode();
        this.responseMessage = responseEnum.getMessage();
    }

    public RestResponse(ResponseCodeEnum responseEnum, Object data){
        this.responseCode = responseEnum.getCode();
        this.responseMessage = responseEnum.getMessage();
        this.data = data;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public Object getData() {
        return data;
    }
}
