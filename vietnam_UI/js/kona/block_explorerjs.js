/** Query block from hyperledger fabric network
 *
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 */

"use strict";


/** 
 * Constructor of protocol manager
 * initialize the server communication module and 
 * the blockchain communication module.
 * It also initializes the document container contract information
 */

function BlockExplorer() {}


/**
 * This function will be called to get a payment token from access controller to initiate a document download.
 */

BlockExplorer.prototype.getBlockInfo = function(blockNumber, callback) {
    var that = this;
    window.console.log("In getBlockInfo" + blockNumber);

	if(blockNumber == null) {alert("Set block number to view information"); return;}
    var requestArray = [];
   requestArray[PARAM_BLOCK_NUMBER] =  blockNumber;
   
   sendPostRequestToServer(requestArray, serverBaseUrl, getBlockQueryAPI, function(result) {
       if (result !== null) {
           window.console.log("Get block response " + result.status + " message " + result.message);
		   callback(that, result.data);
       } else {
           window.console.log("Error in operation download document");
       }
   });
};



/**
 * This function will be called to get a payment token from access controller to initiate a document download.
 */

BlockExplorer.prototype.getLastBlockInfo = function(that, blockInfo) {
	if(blockInfo === null || blockInfo === undefined) {
		console.log("BlockInfo null");	
		return;
	}
	console.log("BlockInfo " + JSON.stringify(blockInfo));
	const lastBlock = blockInfo.lastBlock;
	that.getBlockInfo(Number(lastBlock) - 1, that.renderBlockInfo);
};




/**
 * This function will be called to get a payment token from access controller to initiate a document download.
 */

BlockExplorer.prototype.renderBlockInfo = function(that, blockInfo) {
	if(blockInfo === null || blockInfo === undefined) {
		console.log("BlockInfo null");	
		that.showErrorPopUp("Block information not found");
		return;
	}
	console.log("BlockInfo " + JSON.stringify(blockInfo));
		
	const channelName = document.getElementById("channel-name");
	channelName.innerHTML = blockInfo.channelName;

	const channelId = document.getElementById("channel-id");
	channelId.innerHTML = blockInfo.channelId;

	const lastBlock = document.getElementById("last-block");
	lastBlock.innerHTML = blockInfo.lastBlock - 1;

	const currentBlock = document.getElementById("current-block");
	currentBlock.innerHTML = blockInfo.blockNumber;
	
	const searchInput = document.getElementById("search-block-input");
	searchInput.value = blockInfo.blockNumber;

	const blockHash = document.getElementById("block-hash");
	blockHash.innerHTML = blockInfo.dataHash;

	const transactionCount = document.getElementById("transaction-count");
	transactionCount.innerHTML = blockInfo.txCount;

	const nonce = document.getElementById("nonce");
	nonce.innerHTML = blockInfo.nonce;

	const transactionStatus = document.getElementById("transaction-status");
	transactionStatus.innerHTML = (blockInfo.txStatus == "200")?"Success (200)":"Failed (" + blockInfo.txStatus + ")";

	const endorsementCount = document.getElementById("endorsement-count");
	endorsementCount.innerHTML = blockInfo.endorsementCount;

	const chaincodeId = document.getElementById("chaincode-id");
	chaincodeId.innerHTML = blockInfo.chainCodeIdName;

	const time = document.getElementById("time");
	time.innerHTML = blockInfo.time;
};

BlockExplorer.prototype.enableNextButton = function() {
//document.getElementById("next-block-search").
}

BlockExplorer.prototype.disableNextButton = function() {
	//document.getElementById("next-block-search").
}

BlockExplorer.prototype.enablePreviousButton = function() {
	//document.getElementById("previous-block-search").
}

BlockExplorer.prototype.disablePreviousButton = function() {
	//document.getElementById("previous-block-search").
}

BlockExplorer.prototype.searchNextBlock = function() {
	const blockNumber = this.getCurrentBlockNumber();
	if(blockNumber !== null && blockNumber !== undefined) {
	  console.log("Block number "+ blockNumber);
	  this.getBlockInfo(Number(blockNumber) + 1, this.renderBlockInfo);
	}
}

BlockExplorer.prototype.searchPreviousBlock = function() {
	const blockNumber = this.getCurrentBlockNumber();
	if(blockNumber !== null && blockNumber !== undefined) {
	  console.log("Block number "+ blockNumber);
	  this.getBlockInfo(Number(blockNumber) - 1, this.renderBlockInfo);
	}
}

BlockExplorer.prototype.getCurrentBlockNumber = function() {
	 const searchInput = document.getElementById("search-block-input");
	   const blockNumber = searchInput.value;
	   console.log("Block number "+ blockNumber);
	   if(blockNumber == null) {
		   alert("Input a block number");
		   return;
	   }
	   console.log("Block number "+ blockNumber);
	   return blockNumber;
}



/**
 * Binds buttons click event to upload and download document
 */

BlockExplorer.prototype.bindButtons = function() {
    var that = this;

    //-------------------------------------
   document.getElementById("search-block-btn").addEventListener("click", function() {
	   console.log("Block number");
	   const blockNumber = that.getCurrentBlockNumber();
	   console.log("Block number "+ blockNumber);
	   if(blockNumber !== null) {
		console.log("Block number "+ blockNumber);
		that.getBlockInfo(blockNumber, that.renderBlockInfo);
	   }
    });
	that.getBlockInfo(0, that.getLastBlockInfo);
	
	document.getElementById("previous-block-search").addEventListener("click", function(){
		that.searchPreviousBlock();
	});
	
	document.getElementById("next-block-search").addEventListener("click", function(){
		that.searchNextBlock();
	});

	document.getElementById("popup-button").addEventListener("click", function() {
		that.popupContainer.style.display = "none"
	});

};

BlockExplorer.prototype.onReady = function() {
    console.log("Onload");
    this.popupContainer = document.getElementById("popup-container");
    this.redeemButton = document.getElementById("redeem-button");
    this.popupButton = document.getElementById("popup-button");
    this.popupImage = document.getElementById("popup-img");
    this.popupLabelResult = document.getElementById("popup-label-result");
    this.popupLabelMsg = document.getElementById("popup-label-msg");
    this.bindButtons();
};

BlockExplorer.prototype.showSuccessPopUp = function(message) {
    console.log("Popup success.");
    this.popupContainer.style.display = "block";
    this.popupImage.src = "./resources/img_success.png";
    this.popupLabelResult.innerText = 'SUCCESS';
    this.popupLabelMsg.innerText = message;
};

BlockExplorer.prototype.showErrorPopUp = function(message) {
    console.log("Popup failed.");
    this.popupContainer.style.display = "block";
    this.popupImage.src = "./resources/img_error.png";
    this.popupLabelResult.innerText = 'ERROR';
    this.popupLabelMsg.innerText = message;
};


var blockExplorer = new BlockExplorer(); 

$(document).ready(function() {
    console.log("Starting blockExplorer page");

    window.setTimeout(function() {
        blockExplorer.onReady();
    }, 500);
});
