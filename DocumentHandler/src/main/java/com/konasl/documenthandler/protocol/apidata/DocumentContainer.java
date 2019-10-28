package com.konasl.documenthandler.protocol.apidata;

import lombok.Data;
import org.hyperledger.fabric.contract.annotation.Property;

/**
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/28/2019 12:29
 */

@Data
public class DocumentContainer {

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
        private String chunkKeyPrefix;

        @Property()
        String[] splitKey;
}
