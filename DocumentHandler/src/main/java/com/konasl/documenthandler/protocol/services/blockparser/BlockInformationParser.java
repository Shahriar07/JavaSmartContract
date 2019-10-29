package com.konasl.documenthandler.protocol.services.blockparser;

import com.konasl.documenthandler.auth.AuthUser;
import com.konasl.documenthandler.protocol.apidata.BlockInfoContainer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.InvalidProtocolBufferRuntimeException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE;


/**
 * Retrieve blocks from the hyperledger fabric network
 * parse required information from the block
 */

@Slf4j
@Service
public class BlockInformationParser {

    private Logger logger = LoggerFactory.getLogger(BlockInformationParser.class);

    @Autowired
    private AuthUser authUser;

    public Channel getChannel() {
        Network network = authUser.authUserandGenerateNetwork();
        if (network == null) {
            logger.error("Network generation failed");
            return null;
        }

        try {
            Channel channel = network.getChannel();
            if (channel == null) {
                logger.error("Channel not initialized");
                return null;
            }
            logger.info("KonaChannel has initialized");
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
    public BlockInfoContainer parseBlockInfo(Channel channel, long reqBlockNumber) throws Throwable {
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
                logger.error("Block not found with number " + reqBlockNumber);
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
            logger.error("Block not found with number " + reqBlockNumber);
            throw e.getCause();
        } catch (ProposalException e){
            logger.error("Block not found with number " + reqBlockNumber);
            throw e.getCause();
        }
    }
}
