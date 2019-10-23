/**
 * ParamObjCreator creates common parameter object to use in server communication
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 */


"use strict";

function ParamObjCreator() {
    window.console.log("in ParamObjCreator constructor");
}


/**
 * generates common request parameter object to communicate with Access Controller server
 *
 * @param _docKey {string}        unique identifier for the document in server.
 * @param _docHash {string}       sha-256 hash of the document.
 * @param _signerAddress {string} ethereum address of the signer, require to verify signature.
 * @param _docSignature {string}  signature of the document.
 * @return object                 generated parameter object.
 */
ParamObjCreator.prototype.createParamObj = function(_docKey) {
    window.console.log("in ParamObjCreator createParamObj");
    var requestArray = [];
    requestArray[PARAM_DOC_KEY] = _docKey;
    return requestArray;
};