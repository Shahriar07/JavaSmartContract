/**
 * Contains required information to communicate with access controller server
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 */
 
"use strict";

// Server URL
var serverBaseUrl = "http://10.88.233.118:9090";
//var serverBaseUrl = "http://103.23.42.218:9090";

// Upload APIs
var uploadDocumentAPI = "/api/document/upload";

// Download APIs
var downloadDocumentAPI = "/api/document/download";

// Verify API
var verifyDocumentAPI = "/api/document/verify";



// Server constants
const HTTP_OK = 200;
const RESPONSE_SUCCESS = "0000";

// API parameters
const PARAM_DOCUMENT = "document";
const PARAM_DOC_KEY = "documentKey";
const PARAM_FILE_NAME = "fileName";
const PARAM_FILE_HASH = "documentHash";
