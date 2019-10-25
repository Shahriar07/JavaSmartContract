package com.konasl.documenthandler.protocol.controllers.blockexplorer;

import com.konasl.documenthandler.protocol.ResponseCodeEnum;
import com.konasl.documenthandler.protocol.RestResponse;
import com.konasl.documenthandler.protocol.apidata.BlockInfoContainer;
import com.konasl.documenthandler.protocol.services.blockparser.BlockInformationParser;
import org.hyperledger.fabric.sdk.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Serve request to prepare block information from hyperledger network
 *
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/25/2019 19:36
 */

@RestController
public class BlockExplorerController {
    @Autowired
    BlockInformationParser blockParser;

    @RequestMapping(value = "/api/explore/block", method = RequestMethod.POST)
    public RestResponse parseBlockWithNumber(@RequestParam ("blockNumber") @Valid long blockNumber){
        Channel channel = blockParser.getChannel();
        if (channel== null) {
            new RestResponse(ResponseCodeEnum.BLOCKCHAIN_NETWORK_CONNECTION_FAILED);
        }
        try {
            BlockInfoContainer blockContainer = blockParser.parseBlockInfo(channel, blockNumber);
            if (blockContainer == null){
                return new RestResponse(ResponseCodeEnum.BLOCK_INFO_GENERATION_FAILED);
            }
            else {
                return new RestResponse(ResponseCodeEnum.SUCCESS, blockContainer);
            }
        } catch (Exception e){
            e.printStackTrace();
            return new RestResponse(ResponseCodeEnum.BLOCK_INFO_GENERATION_FAILED);
        }
    }

}
