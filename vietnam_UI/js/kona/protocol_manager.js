/** Protocol manager initializes the upload and download protocol
 * it also controls the flow of document upload and download process
 *
 * Requires /js/kona/common/crypto_utility.js
 *     to perform cryptographic operations (AES enc/dec, hash generation)
 *
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 */

"use strict";

// Sets the default values of the uploading document information
function UploadDocumentInfo() {
    this.files = [];
    this.docKey = "";
    this.aesKey = "";
    this.Uint8DocKey = [];
    this.fileHash = "";
    this.documentSignature = "";
    this.hexAesKey = "";
    this.Uint8InitialToken = [];
}


/** 
 * Constructor of protocol manager
 * initialize the server communication module and 
 * the blockchain communication module.
 * It also initializes the document container contract information
 */

function ProtocolManager() {
    this.paramObjCreator = new ParamObjCreator();
    this.uploadDocInfo = new UploadDocumentInfo();
}


/**
 * sets a new document container contract to upload or download a document
 * @param _contract {object} new smart contract object with abi, address and data
 */
ProtocolManager.prototype.setDocumentContainer = function(_documentContainer) {
    if (_documentContainer !== null && _documentContainer !== undefined && typeof _documentContainer.address == 'string') {
        this.Contract = _documentContainer;
    }
};

/**
 * validates the file
 */

ProtocolManager.prototype.validateFile = function(_file) {
    return (_file === null || _file === undefined || typeof _file.name === undefined) ? false : true;
};



/************************************* Upload Protocol ************************************/

/**
 * This is the entry point of the document upload protocol
 * It initiates the upload procedure of a document,
 * prepare the unique identifier(document Key) of the uploading document, 
 * calculates document hash,
 * prepare signature with document hash,
 * send request to the server for payment token 
 * 
 * @param _file {file}: actual document is being uploaded.
 * @param userAddress {string} : blockchain address of the document uploader
 * @param documentContainerAddress {string} : smart contract address which holds the document information in blockchain.
 */

ProtocolManager.prototype.uploadDocument = function(_file) {

    if (!this.validateFile(_file)) {
        return;
    }

    var that = this;
    window.console.log("In uploadFile");

    // Get document Key
    var docKey = document.getElementById("docKey").value;
    if(docKey == null){
        alert("Document key is empty");
        return;
    }
    window.console.log("Document Key " + docKey);
    // Read the document content and generate document hash
    var reader = new FileReader();
    reader.onload = function(event) {
        var contentInBuffer = event.target.result;
        var array = new Uint8Array(contentInBuffer);
        window.console.log(" doc size " + array.length);
        that.uploadDocInfo.fileHash = generateHashOfFileContent(array);
        window.console.log('fileHash: ' + that.uploadDocInfo.fileHash);

        that.sendDocumentToServer(_file, docKey, function(result) {
            if (result !== null) {
                window.console.log("Upload response " + result.status + " Message " + result.data );
            } else {
                window.console.log("Error in operation sendDocumentToServer");
            }
        });
    };
    reader.readAsArrayBuffer(_file);
};


/**
 * sendDocumentToServer function uploades the actual document with required information to Auxiliary server
 * @param _document {file} : actual document to upload
 * @param _docKey {string} : unique identification of a document
 * @return object with response code and response message otherwise null if failed
 */
ProtocolManager.prototype.sendDocumentToServer = function(_document, _docKey, callback) {

    window.console.log(_document);
    window.console.log("docKey " + _docKey);

    // Prepare the request array
	var requestArray = this.paramObjCreator.createParamObj(_docKey);
	requestArray[PARAM_DOCUMENT] =  _document;
	
	sendPostRequestToServer(requestArray, serverBaseUrl, uploadDocumentAPI, function (response) {
        if (response === null || response === "undefined") {
			window.console.error("sendDocumentToServer Error in response");
			callback(response);
            return;
        }
		callback(response);
	});
};

/************************************* Upload Protocol Ends ************************************/


/************************************* Download Protocol ************************************/

/**
 * This function will be called to get a payment token from access controller to initiate a document download.
 * @param _docKey: unique identification of a document
 * @param _docHash: SHA-256 hash of the document content
 * @param _userAddress: document downloader blockchain address  
 */

ProtocolManager.prototype.downloadDocument = function() {
    var that = this;
    window.console.log("In downloadDocument");

    // Get document Key
    var docKey = document.getElementById("download-docKey").value;
    // Get document Key
    var docName = document.getElementById("download-docName").value;

	var requestArray = this.paramObjCreator.createParamObj(docKey);
    requestArray[PARAM_FILE_NAME] =  docName;
    
    downloadFromMockStorage(requestArray, docName, function(result) {
        if (result !== null) {
            window.console.log("download response " + result);
        } else {
            window.console.log("Error in operation download document");
        }
    });
};

/************************************* Download Protocol End ************************************/

/**
 * This function will be called to get a payment token from access controller to initiate a document download.
 */

ProtocolManager.prototype.verifyDocument = function() {
    var that = this;
    window.console.log("In verifyDocument");

   // Get document Key
   var docKey = document.getElementById("verify-docKey").value;
   // Get document Key
   var docName = document.getElementById("verify-docName").value;
    // Get document Hash
    var docHash = document.getElementById("verify-docHash").value;

	if(docName == null){alert("Input document name"); return;}
	if(docKey == null) {alert("Input document key"); return;}
	if(docHash == null) {alert("Input document hash"); return;}
	
   var requestArray = this.paramObjCreator.createParamObj(docKey);
   requestArray[PARAM_FILE_NAME] =  docName;
   requestArray[PARAM_FILE_HASH] =  docHash;
   
   verifyFromMockStorage(requestArray, docHash, function(result) {
       if (result !== null) {
           window.console.log("Verification response " + result);
           alert("Document verification " + (result?"Success":"Failed"));
       } else {
           window.console.log("Error in operation download document");
       }
   });
};



/**
 * Binds buttons click event to upload and download document
 */

ProtocolManager.prototype.bindButtons = function() {
    var that = this;
    //-------------------------------------
    //FileSelect
    document.getElementById('file').addEventListener('change', function(evt) {
        that.uploadDocInfo.files = evt.target.files; // FileList object

        window.console.log("size " + that.uploadDocInfo.files[0].size);
        window.console.log("name " + that.uploadDocInfo.files[0].name);
    }, false);
    document.getElementById("upload-document").addEventListener("click", function() {
        if(that.uploadDocInfo == null) alert("Select a document");
        that.uploadDocument(that.uploadDocInfo.files[0]);
    });

    document.getElementById("download-document").addEventListener("click", function() {
        that.downloadDocument();
    });

    document.getElementById("request-verify").addEventListener("click", function() {
        that.verifyDocument();
    });
	
	document.getElementById("home-btn").addEventListener("click", function() {
		console.log("home button clicked");
		var fileName = location.href.split("/").slice(-1); 
		console.log("home button clicked : " + fileName);
		if(fileName != "document_manager.html"){
			console.log("home button clicked before: " + fileName);
			window.open("document_manager.html", "_top");
			console.log("home button clicked after : " + fileName);
		}
    });

	document.getElementById("explorer-btn").addEventListener("click", function() {
		console.log("explorer button clicked");
		var fileName = location.href.split("/").slice(-1); 
		console.log("explorer button clicked : " + fileName);
		if(fileName != "block_explorer.html"){
			console.log("home button clicked before: " + fileName);
			window.open("block_explorer.html", "_top");
			console.log("home button clicked after : " + fileName);
		}
    });
};

ProtocolManager.prototype.onReady = function() {
    this.bindButtons();
};

var protocolManager = new ProtocolManager();

$(document).ready(function() {
    console.log("Starting protocolManager page");

    window.setTimeout(function() {
        protocolManager.onReady();
    }, 3000);
});