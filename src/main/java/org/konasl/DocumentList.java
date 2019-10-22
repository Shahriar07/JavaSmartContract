/*
SPDX-License-Identifier: Apache-2.0
*/

package org.konasl;

import org.konasl.ledgerapi.DocumentStateList;
import org.hyperledger.fabric.contract.Context;

public class DocumentList {

    private DocumentStateList documentStateList;

    public DocumentList(Context ctx) {
        this.documentStateList = DocumentStateList.getStateList(ctx, DocumentList.class.getSimpleName(), DocumentContainer::deserialize);
    }

    public DocumentList addDocument(DocumentContainer document) {
        documentStateList.addState(document);
        return this;
    }

    public DocumentContainer getDocument(String documentKey) {
        return (DocumentContainer) this.documentStateList.getState(documentKey);
    }

    public DocumentList updateDocument(DocumentContainer document) {
        this.documentStateList.updateState(document);
        return this;
    }
}
