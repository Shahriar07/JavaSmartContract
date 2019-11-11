package com.konasl.documenthandler.protocol.apidata;

import org.springframework.stereotype.Component;

/**
 * Holds the chunk count and document hash as response data
 * after a successful document upload operation
 *
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/18/2019 18:52
 */

public class UploadResponse {
    private long chunkCount;
    private String documentHash;
    private long totalTime;

    public UploadResponse(long chunkCount, String documentHash, long totalTime) {
        this.chunkCount = chunkCount;
        this.documentHash = documentHash;
	this.totalTime = totalTime;
    }

    public String getDocumentHash() {
        return documentHash;
    }

    public long getChunkCount() {
        return chunkCount;
    }

    public long getTotalTime() {
        return totalTime;
    }
}
