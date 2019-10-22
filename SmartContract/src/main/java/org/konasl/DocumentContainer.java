/*
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.konasl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;
import org.json.JSONArray;
import org.konasl.ledgerapi.State;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@DataType()
public class DocumentContainer extends State {

    @Property()
    private String fileName = "";

    @Property()
    private String documentHash = "";

    @Property()
    private int chunkCount;

    @Property()
    private String token = "";
    
    @Property()
    private String docKey = "";

    @Property()
    private ArrayList<String> chunkKeyList;

    public DocumentContainer() {
        super();
    }

    public DocumentContainer setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
    
    public DocumentContainer setDocKey(String docKey) {
        this.docKey = docKey;
        return this;
    }

    public DocumentContainer setDocumentHash(String hash) {
        this.documentHash = hash;
        return this;
    }

    public DocumentContainer setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
        return this;
    }

    public DocumentContainer setToken(String token) {
        this.token = token;
        return this;
    }

    public DocumentContainer setChunkKeyList(List<String> keyList) {
        this.chunkKeyList = new ArrayList<>(keyList);
        return this;
    }


    public DocumentContainer setKey() {
        this.key = State.makeKey(new String[] { this.fileName, this.docKey });
        return this;
    }

    public String getToken() {
        return token;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public String getDocumentHash() {
        return documentHash;
    }
    
    public String getDocKey() {
        return docKey;
    }

    public String getFileName() {
        return fileName;
    }

    public ArrayList<String> getChunkKeyList() {
        return chunkKeyList;
    }

    @Override
    public String toString() {
        return "DocumentContainer::"  + this.key + "   " + this.fileName + "   " + this.getDocKey() + "   " + this.getToken() + " " + this.getChunkCount() + " " + this.getDocumentHash() + " " +this.getChunkKeyList();
    }

    /**
     * Deserialize a state data to document container
     *
     * @param {Buffer} data to form back into the object
     */
    public static DocumentContainer deserialize(byte[] data) {
    	if(data.length < 1) return null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        System.out.println("Byte data is " + new String(data, UTF_8));
        JSONObject json = new JSONObject(new String(data, UTF_8));
        try {
            String fileName = json.getString("fileName");
            String documentHash = json.getString("documentHash");
            int chunkCount = json.getInt("chunkCount");
            String token = json.getString("token");
            String docKey = json.getString("docKey");
            JSONArray keyArray = json.getJSONArray("chunkKeyList");
            
            ArrayList<String> array = new ArrayList<>();
            for(int i = 0; i< keyArray.length(); i++) {
            	array.add((String) keyArray.get(i));
            }
            return createInstance(fileName, documentHash, docKey, chunkCount, token, array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] serialize(DocumentContainer document) {
        return State.serialize(document);
    }

    /**
     * Factory method to create a document container object
     */
    public static DocumentContainer createInstance(String fileName, String documentHash, String docKey, int chunkCount,
                                                   String token, List<String> chunkKeyList) {
        return new DocumentContainer().setFileName(fileName).setDocumentHash(documentHash).setDocKey(docKey).setChunkCount(chunkCount)
                .setToken(token).setChunkKeyList(chunkKeyList).setKey();
    }

}
