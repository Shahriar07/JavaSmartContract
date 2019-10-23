"use strict";

/**
  *generateDocumentKey function generates a unique hash for any document.
  *@param project_ID: contract address of the project for which the document is being uploaded.
  *@param file_name: document file name that is being uploaded.
  *@returns {string} document key.
  */
function generateDocumentKey(project_ID, file_name, document_owner_address, document_container) {

  var bitArray = sjcl.hash.sha256.hash(project_ID + file_name + document_owner_address + document_container);  
  var digest_sha256 = sjcl.codec.hex.fromBits(bitArray);
  return digest_sha256;
  // return web3.sha3(project_ID + file_name + document_owner_address + new Date());
}



/**
  * generateAESKey function generates a unique AES 128 key.
  * @return 16bytes AES key.
*/

function generateAESKey() {
  var key_128 = new Array();

  for (var i = 0; i < 16; i++) {
      key_128.push(Math.round(Math.random() * 255));
  }
  return new Uint8Array(key_128);
}

/*
  generateHashOfFileContent generates hash of the given file content.
  @param fileData: file content that is used in the hash algorithm.

  Library is used from https://github.com/Caligatio/jsSHA
  @returns hash of the whole file content.
*/

function generateHashOfFileContent(fileData){
  console.log(" fileData length " + fileData.length );
  var shaObj = new jsSHA("SHA-256", "HEX");
  // use the document size as buffer size if the size is smaller than 100kb
  var BUFFER_SIZE = 102400;

  var documentSize = fileData.length;
  var start = 0;
  
  var bufferSize = documentSize > BUFFER_SIZE ? BUFFER_SIZE: documentSize;

  while (start < documentSize){
    var end = start + bufferSize;
    var array = fileData.subarray(start, end);
    shaObj.update(asciiToHex(array));
    start = end;
    bufferSize = documentSize - start > BUFFER_SIZE ? BUFFER_SIZE : documentSize - start;
  }

  var hash = shaObj.getHash("HEX");
  console.log("Sha256 "+hash);
  return hash;
}


function generateSignature(document_hash, document_owner_address) {

  // eth.sign() fucntion takes hex value as parameter. Thus, this hash must be converted to hex.

  var document_hash_in_hex = web3.toHex(String(document_hash).substring(2));
    /* Generate Signature
    @param userAccount: user's account address
    @param fileContentHash: hash of file content
    @param signHandler: a callback function that executes after signature generation. 
  */

  var document_signature = web3.eth.sign(document_owner_address, document_hash_in_hex);
  return document_signature;
}
