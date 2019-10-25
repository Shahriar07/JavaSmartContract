package com.konasl.documenthandler.protocol.services.blockparser;

import com.konasl.documenthandler.auth.AuthUser;
import com.konasl.documenthandler.constants.NetworkConstants;
import com.konasl.documenthandler.protocol.apidata.BlockInfoContainer;
import com.konasl.documenthandler.util.Utility;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.InvalidProtocolBufferRuntimeException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE;


/**
 * Retrieve blocks from the hyperledger fabric network
 * parse required information from the block
 */

@Service
public class BlockInformationParser {

    @Autowired
    private AuthUser authUser;

    @Autowired
    NetworkConstants networkConstants;

    @Autowired
    Utility utility;

    public Channel getChannel() {
        Network network = authUser.authUserandGenerateNetwork();
        if (network == null) {
            System.out.println("Network generation failed");
            return null;
        }

        try {
            Channel channel = network.getChannel();
            if (channel == null) {
                System.out.println("KonaChannel not initialized");
                return null;
            }
            System.out.println("KonaChannel has initialized");

            final String channelName = channel.getName();
            return channel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Traverse every block in the channel from last block to first block
     *
     * @param channel
     * @throws InvalidArgumentException
     * @throws ProposalException
     * @throws IOException
     */
    public BlockInfoContainer parseBlockInfo(Channel channel, long reqBlockNumber) throws InvalidArgumentException, ProposalException, IOException {
        try {
            BlockInfoContainer blockInfoContainer = new BlockInfoContainer();

            // Set channel name
            BlockchainInfo channelInfo = channel.queryBlockchainInfo();
            blockInfoContainer.setChannelName(channel.getName());

            // Set last block number
            long height = channelInfo.getHeight();
            blockInfoContainer.setLastBlock(height);

            // get the block from the network
            BlockInfo returnedBlock = channel.queryBlockByNumber(reqBlockNumber);
            if (returnedBlock == null) {
                System.out.println("Block not found with number " + reqBlockNumber);
                return null;
            }

            // Set block information
            final long blockNumber = returnedBlock.getBlockNumber();
            blockInfoContainer.setBlockNumber(blockNumber);
            blockInfoContainer.setDataHash(Hex.encodeHexString(returnedBlock.getDataHash()));
            int txCount = returnedBlock.getTransactionCount();
            blockInfoContainer.setTxCount(txCount);

            // return the blockInfoContainer as there is no transaction
            if (txCount < 1) return blockInfoContainer;

            // parse the envelops to identify transactions and prepare transaction information
            for (BlockInfo.EnvelopeInfo envelopeInfo : returnedBlock.getEnvelopeInfos()) {
                blockInfoContainer.setChannelId(envelopeInfo.getChannelId());
                blockInfoContainer.setTime(envelopeInfo.getTimestamp().toString());
                blockInfoContainer.setNonce(Hex.encodeHexString(envelopeInfo.getNonce()));
                    if (envelopeInfo.getType() == TRANSACTION_ENVELOPE) {
                        BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) envelopeInfo;
                        for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo : transactionEnvelopeInfo.getTransactionActionInfos()) {
                            blockInfoContainer.setTxStatus(""+transactionActionInfo.getResponseStatus());
                            blockInfoContainer.setEndorsementCount(transactionActionInfo.getEndorsementsCount());
                            String chainCodeIDName = transactionActionInfo.getChaincodeIDName();
                            blockInfoContainer.setChainCodeIdName(chainCodeIDName);
                        }
                    }
                }
                return blockInfoContainer;
        } catch (InvalidProtocolBufferRuntimeException e) {
            throw e.getCause();
        }
    }
}
