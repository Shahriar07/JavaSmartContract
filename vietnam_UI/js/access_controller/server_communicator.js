/**
 * server_communicator is a common implementation to send HTTP requests to the server
 *
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 */


"use strict";

/** 
 * sends post request to the server
 * @param {array} key value pair of request data
 * @param {string} base url included the port number
 * @param {string} api name to execure the request
 * @param {callback} callback to receive the responseCode
 */
 
function sendPostRequestToServer( requestArray, baseUrl, api, callback) {
    window.console.log("In sendPostRequestToServer");
	// validate request data
	if(requestArray === null) {
			window.console.error("requestArray data invalid");
			return null;
	}
	return sendRequestToServer(requestArray, baseUrl, api, "POST", callback);
}


/** 
 * sends GET request to the server
 * @param {array} key value pair of request data
 * @param {string} base url included the port number
 * @param {string} api name to execure the request
 * @param {callback} callback to receive the responseCode
 */
function sendGetRequestToServer( requestArray, baseUrl, api, callback) {
    window.console.log("In sendPostRequestToServer");
	// validate request data
	if(requestArray === null) {
			window.console.error("requestArray data invalid");
			return null;
	}
	return sendRequestToServer(requestArray, baseUrl, api, "GET", callback);
}

/**
 * common request function to send requests to server
 */

function sendRequestToServer( requestArray, baseUrl, api, type, callback) {
    window.console.log("In sendRequestToServer");
	// validate request data
	if(baseUrl === null || api === null || requestArray === undefined 
		|| baseUrl === undefined || api === undefined || callback === null || callback === undefined) {
			window.console.error("Request data invalid");
			return null;
	}
	
	if(type !== "POST" && type !== "GET") {
		window.console.error("Request method not supported");
		return null;
	}
    var requestData = new FormData();
	for (var key in requestArray) {
		requestData.append(key, requestArray[key]);
	}

    var request = new XMLHttpRequest();
    request.open(type, baseUrl + api, true);
    request.send(requestData);
    request.onload = function() {
        if (request.status != HTTP_OK) {
            window.console.log("Server error " + request.status);
			if (isTypeCallback(callback))
				callback(null);
            return;
        }
        var responseObject = JSON.parse(request.responseText);
        if (responseObject.responseCode == RESPONSE_SUCCESS) {
			window.console.log("Operation " + responseObject.responseMessage + " : " + responseObject.data );
			if (isTypeCallback(callback))
				callback({status : RESPONSE_SUCCESS, message: responseObject.responseMessage, data : responseObject.data});
        } else {
				window.console.log("Error in operation " + responseObject.responseMessage + " : " + responseObject.data );
				if (isTypeCallback(callback))
					callback({status : responseObject.responseCode, message: responseObject.responseMessage, data : responseObject.data});
        }
    };
    request.onerror = function() {
        if (request.status != HTTP_OK) {
            window.console.log("Server error " + request.status);
			if (isTypeCallback(callback))
				callback(null);
            return;
        }
    };		
}

function sendRequestToServerForFileDownload( requestArray, baseUrl, api, type, callback) {
    window.console.log("In sendRequestToServerForFileDownload");
	// validate request data
	if(baseUrl === null || api === null || requestArray === undefined 
		|| baseUrl === undefined || api === undefined || callback === null || callback === undefined) {
			window.console.error("Request data invalid");
			return null;
	}
	
	if(type !== "POST" && type !== "GET") {
		window.console.error("Request method not supported");
		return null;
    }

    if(type === "GET")
    api += processGetRequest(requestArray);

    var requestData = new FormData();
	for (var key in requestArray) {
        window.console.log("Key " + key + " Value " + requestArray[key]);
		requestData.append(key, requestArray[key]);
    }
    


    var request = new XMLHttpRequest();
    
    request.responseType = 'blob';
    request.open(type, baseUrl + api, true);
    request.send(requestData);
    request.onload = function() {
        if (request.status != HTTP_OK) {
			console.log("Download File :"+ JSON.stringify(request));
			window.console.log("Server error message: " + request.response + " code: " + request.status);
			if (isTypeCallback(callback))
				callback(null);
            return;
		}
		
		var sizeOfObject = memorySizeOf(request.response);		 
		console.log("Download File :"+ JSON.stringify(request));
		console.log("Download File Type :"+ request.responseType);
		//  var responseObject = JSON.data(request.responseText);
		 if (isTypeCallback(callback))
		 callback({data : request.response, type:  request.responseType, length: sizeOfObject});
    };
    request.onerror = function() {
        if (request.status != HTTP_OK) {
            window.console.log("Server error " + request.status);
			if (isTypeCallback(callback))
				callback(null);
            return;
        }
    };	
}

function  processGetRequest(requestArray) {
    var inputData = "?";
    for (var key in requestArray) {
        window.console.log("Key " + key + " Value " + requestArray[key]);
        inputData += key + "=" + requestArray[key] + "&";
    }
    return inputData.substring(0, inputData.length -1);
}


function memorySizeOf(obj) {
    var bytes = 0;

    function sizeOf(obj) {
        if(obj !== null && obj !== undefined) {
            switch(typeof obj) {
            case 'number':
                bytes += 8;
                break;
            case 'string':
                bytes += obj.length * 2;
                break;
            case 'boolean':
                bytes += 4;
                break;
            case 'object':
                var objClass = Object.prototype.toString.call(obj).slice(8, -1);
                if(objClass === 'Object' || objClass === 'Array') {
                    for(var key in obj) {
                        if(!obj.hasOwnProperty(key)) continue;
                        sizeOf(obj[key]);
                    }
                } else bytes += obj.toString().length * 2;
                break;
            }
        }
        return bytes;
    };

    function formatByteSize(bytes) {
        if(bytes < 1024) return bytes + " bytes";
        else if(bytes < 1048576) return(bytes / 1024).toFixed(3) + " KiB";
        else if(bytes < 1073741824) return(bytes / 1048576).toFixed(3) + " MiB";
        else return(bytes / 1073741824).toFixed(3) + " GiB";
    };

    var size = sizeOf(obj);
    console.log("Size bytes :"+ size);
    console.log("Size :"+ formatByteSize(size));
    return size;
};
