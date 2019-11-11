/**
 * Downloads a document from the mock server using the temporary session.
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 */

"use strict";

function downloadFromMockStorage(requestArray, fileName, callback) {
     console.log("downloadDocumentAPI ::"+ downloadDocumentAPI + " Request Array " + requestArray);

	sendRequestToServerForFileDownload(requestArray, serverBaseUrl, downloadDocumentAPI,"GET", function (response) {
		if (response === null || response === "undefined") {
				window.console.error("downloadFromMockStorage Error in response");
				callback(null);
		    return;
		} else {
			callback(fileName);
			window.console.log(response);
			saveDocumentFromBrowser(response.data, fileName);
		    //  generateHash(response.data, function(generatedHash) {
		    // 	if (generatedHash == hash) {
		    // 	  window.console.log("Document verification success");
		    // 	  callback(response.data, fileName);
		    // 	} else {
		    // 	  window.console.error("Document verification failed");
		    // 	  callback(null);
		    // 	}
		    	
		    //  }) ;
		    
		}
	});
}


function verifyFromMockStorage(requestArray, docHash, callback) {
	console.log("downloadDocumentAPI ::"+ verifyDocumentAPI + " Request Array " + requestArray);

   sendRequestToServer(requestArray, serverBaseUrl, verifyDocumentAPI, "POST", callback);
}


/**
 * Prepare hash from downloaded document
 * @param {data} : downloaded data from external storage
 * @param {string} : original hash, provided from blockchain
 * @return {boolean} : true if generated hash matches with the provided hash
 */ 

function generateHash(data, callBack) {
    window.console.log("generateHash doc size " + data.length);
    var reader = new FileReader();
    reader.onload = function() {
     	var arrayBuffer = reader.result;
     	var bytes = new Uint8Array(arrayBuffer);
     	var generatedHash = generateHashOfFileContent(bytes);
     	window.console.log(" Generated " + generatedHash);
 	callBack(generatedHash);
	return;
    }
    var blob = new Blob([data], {type: "application/octet-stream" });
    reader.readAsArrayBuffer(blob);
}


/**
 * Downloads the document in browser
 * 
 * Download works on chrome browser
 * 
 * @param {data} : downloaded data from external storage
 * @param {string} : file name of the downloaded document
 */
function saveDocumentFromBrowser(data, fileName) {
	window.console.log("Download file " + fileName + " data " + data);
    //Save data from browser
	// TODO : Need to handle large file differently, Works only on small files ( < 600MB) 
    //var blob = new Blob([new Uint8Array(data.Body)], { type: data.ContentType });
    // var blob = new Blob([new Uint8Array(data)], { type: "application/octet-stream" });  
    var blob = new Blob([data], {type: "application/octet-stream" });
    var link = document.createElement('a');
    var url= window.URL || window.webkitURL;
    link.href = url.createObjectURL(blob);
    link.download = fileName;
    link.click();
//    link.setAttribute('href', 'data:text/plain;charset=utf-8,' + blob);
//    link.setAttribute('download', fileName);
//    link.style.display = 'none';
//    link.download = fileName;
//    document.body.appendChild(link);
//    link.click();
//    document.body.removeChild(link);
}
