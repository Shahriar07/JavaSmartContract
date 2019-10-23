package org.konasl;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

class DocumentHolderContext extends Context {

    public DocumentHolderContext(ChaincodeStub stub) {
        super(stub);
    }

}