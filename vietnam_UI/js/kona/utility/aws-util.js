/**
 * Downloads a document from AWS using the temporary session.
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 */

"use strict";

function downloadFromAws(accessKeyId, secretAccessKey, sessionToken, bucketName, KeyValue, fileName, hash) {
    var s3 = new AWS.S3({
        accessKeyId: accessKeyId,
        secretAccessKey: secretAccessKey,
        sessionToken: sessionToken
    });
    s3.getObject({ Bucket: bucketName, Key: KeyValue },
        function(error, data) {
            if (error !== null) {
                window.console.log("Failed to retrieve an object: ");
                window.console.log(error);
            } else {
                window.console.log("Data retrived ");
                window.console.log("Data contentType ");
                window.console.log(data.ContentType);
                if (isHashVerified(data, hash)) {
                    window.console.log("Document hash verification success, downloading document");
                    saveDocumentFromBrowser(data, fileName);
                } else {
                    window.console.error("Document hash verification failed");
                }
            }
        }
    );
}

/**
 * Prepare hash from downloaded document and compare with the provided hash
 * @param {data} : downloaded data from external storage
 * @param {string} : original hash, provided from blockchain
 * @return {boolean} : true if generated hash matches with the provided hash
 */ 

function isHashVerified(data, providedHash) {
    window.console.log(" doc size " + data.Body.length);
    var generatedHash = generateHashOfFileContent(data.Body);
    window.console.log(" Generated Hash " + generatedHash + " Provided hash " + providedHash);
    return (generatedHash == providedHash) ? true : false;
}


/**
 * Downloads the document in browser
 * @param {data} : downloaded data from external storage
 * @param {string} : file name of the downloaded document
 */
function saveDocumentFromBrowser(data, fileName) {
    //Save data from browser

    var blob = new Blob([new Uint8Array(data.Body)],{type:data.ContentType});
    var link = document.createElement('a');
    var url = window.URL || window.webkitURL;
    link.href = url.createObjectURL(blob);
    link.download = fileName;
    link.click();
}