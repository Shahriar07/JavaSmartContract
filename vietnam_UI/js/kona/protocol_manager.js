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
	this.verifyFiles = [];
    this.docKey = "";
    this.uploadFileHash = "";
	this.verifyFileHash = "";
}


/** 
 * Constructor of protocol manager
 * initialize the server communication module
 */

function ProtocolManager() {
    this.paramObjCreator = new ParamObjCreator();
    this.uploadDocInfo = new UploadDocumentInfo();
}


/**
 * validates the file
 */

ProtocolManager.prototype.validateFile = function(_file) {
    return (_file === null || _file === undefined || typeof _file.name === undefined) ? false : true;
};



/************************************* Upload Protocol ************************************/

/**
 * This is the entry point of the document upload protocol
 * It performs the upload procedure of a document
 * 
 * @param _file {file}: actual document is being uploaded.
 */

ProtocolManager.prototype.uploadDocument = function(_file) {

    if (!this.validateFile(_file)) {
        return;
    }

    var that = this;
    window.console.log("In uploadFile");

    // Get document Key
    var docKey = document.getElementById("docKey").value;
    if(docKey === null || docKey === undefined){
        alert("Document key is empty");
        return;
    }
    window.console.log("Document Key " + docKey);
	that.showLoader();
	that.sendDocumentToServer(_file, docKey, function(result) {
		that.hideLoader();
		if (result !== null) {
			window.console.log("Upload response " + result.status + " Message " + JSON.stringify(result.data) );
			that.showSuccessPopUp("Document Upload " + result.message + "\nFile Size : " + _file.size + " Bytes\nChunk Count : " + result.data.chunkCount + "\nUpload Time : " + (result.data.totalTime/1000) + " seconds");
//				alert("Document Upload " + result.message + "\nChunk Count : " + result.data.chunkCount + "\nUpload Time : " + (result.data.totalTime/1000) + " seconds" );
		} else {
		    window.console.log("Error in operation sendDocumentToServer");
			that.showErrorPopUp("Document Upload failed");
		}
    	});
};


/**
 * sendDocumentToServer function uploades the actual document with required information to the server
 * @param _document {file} : actual document to upload
 * @param _docKey {string} : unique identification of a document
 * @return object with response code and response message otherwise null if failed
 */
ProtocolManager.prototype.sendDocumentToServer = function(_document, _docKey, callback) {

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
 * This function will be called to download a document
 *
 */

ProtocolManager.prototype.downloadDocument = function() {
    var that = this;
    window.console.log("In downloadDocument");

    // Get document Key
    var docKey = document.getElementById("download-docKey").value;
    // Get document name
    var docName = document.getElementById("download-docName").value;
	if(docKey === null || docKey === undefined){
        alert("Document key is empty");
        return;
    }
	
	// Prepare the request array
	var requestArray = this.paramObjCreator.createParamObj(docKey);
	that.showLoader();
	sendPostRequestToServer(requestArray, serverBaseUrl, getDocumentNameAPI, function (response) {
		
		if (response === null || response === "undefined") {
			that.hideLoader();
			window.console.error("sendDocumentToServer Error in response");
			that.showErrorPopUp("Document does not exist");
			return;
		}
		// Document name received from the smart contract
		if (response.status === RESPONSE_SUCCESS) {
			console.log("Retrieved document name : " + response.data);
			var requestArray = that.paramObjCreator.createParamObj(docKey);
			requestArray[PARAM_FILE_NAME] =  docName;
    		downloadFromMockStorage(requestArray, response.data, function(result) {
			that.hideLoader();
				if (result !== null) {
					window.console.log("download success of " + result);
				} else {
					that.showErrorPopUp("Document download failed");
					window.console.log("Error in operation download document");
				}
			});
		} else {
			that.hideLoader();
			that.showErrorPopUp("Document download failed");
			window.console.log("Error in operation download document " + response.message);
		}
	});
};

/************************************* Download Protocol End ************************************/

/**
 * This function will be called to get a payment token from access controller to initiate a document download.
 */

ProtocolManager.prototype.verifyDocument = function(_file) {
    var that = this;
    window.console.log("In verifyDocument");
    if (!this.validateFile(_file)) {
        return;
    }
   // Get document Key
   var docKey = document.getElementById("verify-docKey").value;
	window.console.log("In verifyDocument " + docKey);
	// Read the document content and generate document hash
    var reader = new FileReader();
    reader.onload = function(event) {
        that.showLoader();
        window.console.log(" doc size " + _file.size);
        var contentInBuffer = event.target.result;
        var array = new Uint8Array(contentInBuffer);
        window.console.log(" doc size " + array.length);

        const fileHash = generateHashOfFileContent(array);
        window.console.log('fileHash: ' + fileHash);
	if(_file.name === null){that.hideLoader();alert("Select a document to verify"); return;}
	if(docKey === null || docKey ==="" ) {that.hideLoader();alert("Document key is empty"); return;}
	if(fileHash == null) {that.hideLoader();alert("Select a document to verify"); return;}
        var requestArray = that.paramObjCreator.createParamObj(docKey);
	   requestArray[PARAM_FILE_NAME] =  _file.name;
	   requestArray[PARAM_FILE_HASH] =  fileHash;
	   verifyFromMockStorage(requestArray, fileHash, function(result) {
		that.hideLoader();
	       if (result !== null) {
			if(result.status===RESPONSE_SUCCESS) {
			   that.showSuccessPopUp("Document verification success\n\n");
			} else {
			   that.showErrorPopUp("Document verification failed");
			}

		   window.console.log("Verification response " + result.status + " message " + result.message);
		   //alert("Document verification " + (result.status===RESPONSE_SUCCESS?"success":"failed"));
	       } else {
		   window.console.log("Error in operation download document");
		   that.showErrorPopUp("Document verification failed");
	       }
	   });
    };
    reader.readAsArrayBuffer(_file);
};

ProtocolManager.prototype.showLoader = function() {
	document.getElementById("loaderContainer").style.display='block';
};

ProtocolManager.prototype.hideLoader = function() {
	document.getElementById("loaderContainer").style.display='none';
};

/**
 * Binds buttons click event to upload and download document
 */

ProtocolManager.prototype.bindButtons = function() {
    var that = this;
    that.resetInputFields();
    //-------------------------------------
    // FileSelect for upload
    document.getElementById('upload-file').addEventListener('change', function(evt) {
        that.uploadDocInfo.files = evt.target.files; // FileList object
		const uploadDocNameInput = document.getElementById('upload-document-name');
		if(that.uploadDocInfo.files[0] !== null && that.uploadDocInfo.files[0] !== undefined){
			const fileName = that.uploadDocInfo.files[0].name;
			window.console.log("size " + that.uploadDocInfo.files[0].size);
			window.console.log("name " + fileName);
			uploadDocNameInput.value = fileName;
		}
		else {
			window.console.log("File not selected");
			uploadDocNameInput.value = "";
		}
    }, false);
	
	// File select for verify
	document.getElementById('verify-file').addEventListener('change', function(evt) {
        that.uploadDocInfo.verifyFiles = evt.target.files; // FileList object
		const docNameInput = document.getElementById('verify-document-name');
		if(that.uploadDocInfo.verifyFiles[0] !== null && that.uploadDocInfo.verifyFiles[0] !== undefined){
			const fileName = that.uploadDocInfo.verifyFiles[0].name;
			window.console.log("size " + that.uploadDocInfo.verifyFiles[0].size);
			window.console.log("name " + fileName);
			docNameInput.value = fileName;
		}
		else {
			window.console.log("File not selected");
			docNameInput.value = "";
		}
    }, false);
	
	
    document.getElementById("upload-document").addEventListener("click", function() {
        if(that.uploadDocInfo == null) alert("Select a document");
        that.uploadDocument(that.uploadDocInfo.files[0]);
    });

    document.getElementById("download-document").addEventListener("click", function() {
        that.downloadDocument();
    });

    document.getElementById("request-verify").addEventListener("click", function() {
        if(that.uploadDocInfo.verifyFiles[0] == null) alert("Select a document");
        that.verifyDocument(that.uploadDocInfo.verifyFiles[0]);
    });
	
	
    document.getElementById("custom-upload-btn").addEventListener("click", function() {
		console.log("custom-upload button clicked");
		document.getElementById("upload-file").click();
    });

    document.getElementById("custom-verify-btn").addEventListener("click", function() {
		console.log("custom-verify button clicked");
		document.getElementById("verify-file").click();
    });

    document.getElementById("popup-button").addEventListener("click", function() {
        that.popupContainer.style.display = "none"
    });
};


ProtocolManager.prototype.showSuccessPopUp = function(message) {
    console.log("Popup success.");
    this.popupContainer.style.display = "block";
    this.popupImage.src = "./resources/img_success.png";
    this.popupLabelResult.innerText = 'SUCCESS';
    this.popupLabelMsg.innerText = message;
};

ProtocolManager.prototype.showErrorPopUp = function(message) {
    console.log("Popup failed.");
    this.popupContainer.style.display = "block";
    this.popupImage.src = "./resources/img_error.png";
    this.popupLabelResult.innerText = 'ERROR';
    this.popupLabelMsg.innerText = message;
};

ProtocolManager.prototype.resetInputFields = function() {
    document.getElementById('verify-document-name').value = "";
    document.getElementById('upload-document-name').value = "";
    document.getElementById("docKey").value = Math.floor(Math.random() * 100000) ;
    document.getElementById("verify-docKey").value = "";
    document.getElementById("download-docKey").value = "";
    document.getElementById("download-docName").value = "";
};

ProtocolManager.prototype.onReady = function() {
    
    console.log("Onload");
    this.popupContainer = document.getElementById("popup-container");
    this.redeemButton = document.getElementById("redeem-button");
    this.popupButton = document.getElementById("popup-button");
    this.popupImage = document.getElementById("popup-img");
    this.popupLabelResult = document.getElementById("popup-label-result");
    this.popupLabelMsg = document.getElementById("popup-label-msg");
    this.bindButtons();
};

var protocolManager = new ProtocolManager();

$(document).ready(function() {
    console.log("Starting protocolManager page");

    window.setTimeout(function() {
        protocolManager.onReady();
    }, 500);
});
