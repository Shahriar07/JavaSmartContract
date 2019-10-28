package org.konasl;


import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.konasl.ledgerapi.State;
import org.konasl.ledgerapi.impl.DocumentStateListImpl;
import org.konasl.util.Utility;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;

import static org.konasl.util.Utility.byteArrayToString;
import static org.konasl.util.Utility.hexStringToByteArray;


/**
 * Define document smart contract by extending Fabric Contract class
 */

@Contract(name = "org.konasl.documentcontainer", info = @Info(title = "Document Container Contract", description = "", version = "0.0.1", license = @License(name = "SPDX-License-Identifier: ", url = ""), contact = @Contact(email = "info_ksl@konasl.com", name = "KSL Admin", url = "https://konasl.com")))
@Default
public class DocumentContract implements ContractInterface {

    // use the classname for the logger, this way you can refactor
    private final static Logger LOG = Logger.getLogger(DocumentContract.class.getName());

    @Override
    public Context createContext(ChaincodeStub stub) {
        return new DocumentHolderContext(stub);
    }

    public DocumentContract() {
    }

    /**
     * Instantiate to perform any setup of the ledger that might be required.
     *
     * @param {Context} ctx the transaction context
     */
    @Transaction
    public void instantiate(DocumentHolderContext ctx) {
        // No implementation required with this konasl
        // It could be where data migration is performed, if necessary
        LOG.info("No data migration to perform");
    }

    /**
     * Upload the metadata for a new document
     *
     * @param {Context} ctx the transaction context
     * @param {String}  fileName, document name
     * @param {String}  fileHash, Sha-256 hash (hex data) of file data
     * @param {String}  documentKey, unique identifier of file data
     * @param {String}  chunkKeyPrefix, Key format of every file chunk "fileName_docKey_chunkCount"
     * @param {int}     chunkCount, Number of chunks of the file
     */

    @Transaction
    public String uploadMetadata(DocumentHolderContext ctx, String fileName, String fileHash, String documentKey, String chunkKeyPrefix, int chunkCount) {

        System.out.println(ctx);
        // Generate the token
        String token = null;
        long tokenStart = System.currentTimeMillis();
        try {
            token = generateUploadToken(fileName, documentKey, fileHash, chunkCount);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        long tokenEnd = System.currentTimeMillis();
        System.out.println("Generate Upload token duration : " + (tokenEnd - tokenStart));
        // Generate the chunkKeyList
//        ArrayList<String> chunkKeyList = generateChunkKeyList(fileName, documentKey, chunkCount);

        long chunkKeyEnd = System.currentTimeMillis();
        System.out.println("Generate chunk Key : " + (chunkKeyEnd - tokenEnd));
        // create an instance of the document
        DocumentContainer document = DocumentContainer.createInstance(fileName, fileHash, documentKey, chunkCount, token, chunkKeyPrefix);

        System.out.println(document);
        // Add the document to the list of all similar documents in the ledger
        // world state
        DocumentStateListImpl stateOperator = new DocumentStateListImpl(ctx, documentKey, DocumentContainer::deserialize );
        stateOperator.addState(document);
        long addDocumentEnd = System.currentTimeMillis();
        System.out.println("Store document metadata : " + (addDocumentEnd - chunkKeyEnd));
        // Return a serialized document container to the caller of smart contract
        return document.getToken();
    }

    /**
     * Generates an upload token to initiate the upload operation
     * This token will be used later to identify the document
     *
     * @param fileName
     * @param fileHash
     * @param chunkCount
     * @return
     * @throws NoSuchAlgorithmException
     */
    private String generateUploadToken(String fileName, String docKey, String fileHash, int chunkCount) throws NoSuchAlgorithmException {
        return Utility.prepareSha256Hash(fileName + docKey + fileHash + chunkCount);
    }

    /**
     * Add chunk to a document
     *
     * @param {Context} ctx the transaction context
     * @param {String}  fileName Name of the document
     * @param {String} documentBytes hex string of the chunk bytes
     * @param {String}  chunkKeyPrefix chunk Key prefix
     * @param {String}  docKey,  unique identifier of document
     * @param {Integer} chunk number of the document
     */

    @Transaction
    public boolean uploadDocumentChunk(DocumentHolderContext ctx, String documentBytes, String chunkKeyPrefix, String docKey, int chunkNumber) {

        // retrieves the document metadata
        long getMetadataStart = System.currentTimeMillis();
        DocumentContainer document = getDocumentMetadata(ctx, docKey);
        if (document == null) {
            System.out.println("Document not found with the filename and upload token");
            return false;
        }
        long getMetadataEnd = System.currentTimeMillis();
        System.out.println("Get document metadata : " + (getMetadataEnd - getMetadataStart));
        // prepare the chunk key for the provided chunk number
        String chunkKey = Utility.prepareChunkKey(chunkKeyPrefix, chunkNumber);


        // insert the chunk value to the state
        ctx.getStub().putState(chunkKey, hexStringToByteArray(documentBytes));
        long putChunkEnd = System.currentTimeMillis();
        System.out.println("Put document chunkNumber " + chunkNumber + " duration : " + (putChunkEnd - getMetadataEnd));
        return true;
    }


    /**
     * Retrieves the document metadata
     *
     * @param {Context} ctx the transaction context
     * @param {String}  document name
     * @param {String}  document token
     */
    @Transaction
    public DocumentContainer getDocumentMetadata(DocumentHolderContext ctx, String docKey) {

        String key = State.makeKey(new String[]{docKey});
        System.out.println("LedgerKey " + key);

        // retrieve the document metadata from the state
        DocumentStateListImpl stateOperator = new DocumentStateListImpl(ctx, docKey, DocumentContainer::deserialize );
        DocumentContainer document = (DocumentContainer) stateOperator.getState(key);
        return document;
    }

    /**
     * Retrieves the document hash
     *
     * @param {Context} ctx the transaction context
     * @param {String}  document name
     * @param {String}  document token
     */
    @Transaction
    public String getDocumentHash(DocumentHolderContext ctx, String fileName, String docKey) {

        String key = State.makeKey(new String[]{docKey});
        System.out.println("LedgerKey " + key);

        // retrieve the document metadata from the state
        DocumentStateListImpl stateOperator = new DocumentStateListImpl(ctx, docKey, DocumentContainer::deserialize );
        DocumentContainer document = (DocumentContainer) stateOperator.getState(key);
        if (document != null){
            System.out.println("Document chunk count " + document.getChunkCount());
            return document.getDocumentHash();
        }

        System.out.println("Document not found for filename " + fileName + " and docKey " + docKey);
        return null;
    }


    /**
     * Retrieves the document metadata
     *
     * @param {Context} ctx the transaction context
     * @param {String}  document name
     * @param {String}  document token
     */
    @Transaction
    public int getDocumentChunkCount(DocumentHolderContext ctx, String docKey) {

        String key = State.makeKey(new String[]{ docKey});
        System.out.println("LedgerKey " + key);

        // retrieve the document metadata from the state
        DocumentStateListImpl stateOperator = new DocumentStateListImpl(ctx, docKey, DocumentContainer::deserialize );
        DocumentContainer document = (DocumentContainer) stateOperator.getState(key);
        if (document != null){
            System.out.println("Document chunk count " + document.getChunkCount());
            return document.getChunkCount();
        }

        System.out.println("Document not found for docKey " + docKey);
        return 0;
    }

    /**
     * Retrieves document chunk bytes
     * @param ctx
     * @param fileName
     * @param docKey
     * @param chunkKeyPrefix
     * @param chunkNumber
     * @return
     */
    @Transaction
    public String getDocumentChunk(DocumentHolderContext ctx, String fileName, String docKey, String chunkKeyPrefix, int chunkNumber) {
        long getChunkStart = System.currentTimeMillis();
        // retrieves the document metadata
        DocumentContainer document = getDocumentMetadata(ctx, docKey);
        if (document == null) {
            System.out.println("Document not found with the filename and upload token");
            return null;
        }

        // prepare the chunk key for the provided chunk number
        String chunkKey = Utility.prepareChunkKey(chunkKeyPrefix, chunkNumber);
        if (chunkKey == null) return null;

        // get the chunk value from the state
        byte[] bytes = ctx.getStub().getState(chunkKey);
        long getChunkEnd = System.currentTimeMillis();
        System.out.println("get document chunkNumber " + chunkNumber + " duration : " + (getChunkEnd - getChunkStart));
        // hex string of the byte array
        return  byteArrayToString(bytes);
    }
}
