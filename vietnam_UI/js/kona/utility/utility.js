/*
  hexStringToCharacter converts the hex string to ascii character string.
  @param hexString: hex string containing the characters.

  @returns character string.
*/

function hexStringToCharacter (hexString) {
  var hex = hexString.toString();//force conversion
  var str = '';
  for (var i = 0; (i < hex.length && hex.substr(i, 2) !== '00'); i += 2) {
    var intValue = parseInt(hex.substr(i, 2), 16);
    if(intValue < 32 || intValue > 126)
      continue;
    str += String.fromCharCode(intValue);
  }
  return str;
}


/*
  intArrayToCharacter converts the integer array to ascii character string.
  @param intArray: Integer array containing the characters.

  @returns character string.
*/

function intArrayToCharacter (intArray) {
  //var hex = hexString.toString();//force conversion
  var str = '';
  for ( var i = 0; i < intArray.length; i++) {
    if(intArray[i] < 32 || intArray[i] > 126)
      continue;
    str += String.fromCharCode(intArray[i]);    
  }
  return str;
}

function toHexString(byteArray) {
  return Array.from(byteArray, function(byte) {
    return ('0' + (byte & 0xFF).toString(16)).slice(-2);
  }).join('')
}


function hexToAscii(inputStr) {
  var hex  = inputStr.toString();
  var Uint8Array = new Array();
  for (var n = 0; n < hex.length; n += 2) {
    Uint8Array.push(parseInt(hex.substr(n, 2), 16));
  }
  return Uint8Array;
}


function asciiToHex(inputArray) {
  var byteArray = inputArray;
  var hexStr = "";

  for ( var i = 0; i < byteArray.length; i++) {

    if(byteArray[i] < 16) {
      hexStr += '0' + (byteArray[i] & 0xFF).toString(16);
    } else {
      hexStr += (byteArray[i] & 0xFF).toString(16);
    }
    
  }
  return hexStr;
}


// Checks if the supplied parameter is a callback function
function isTypeCallback (callback) {
    return (typeof callback === "function");
};