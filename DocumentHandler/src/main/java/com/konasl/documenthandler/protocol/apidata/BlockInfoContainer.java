package com.konasl.documenthandler.protocol.apidata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the information of a hyperledger network block
 *
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/25/2019 18:43
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockInfoContainer {
    long lastBlock;
    String channelName;
    String dataHash;
    long blockNumber;
    String channelId;
    String nonce;
    int txCount;
    String txStatus;
    int endorsementCount;
    String chainCodeIdName;
    String time;
}
