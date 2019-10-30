package org.konasl.ledgerapi.impl;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.konasl.DocumentContract;
import org.konasl.ledgerapi.DocumentStateList;
import org.konasl.ledgerapi.State;
import org.konasl.ledgerapi.StateDeserializer;
import org.konasl.util.Utility;

import java.util.Arrays;
import java.util.logging.Logger;

import static org.konasl.constants.Constants.LIST_NAME;

/**
 * DocumentStateList provides a named virtual container for a set of ledger states. Each
 * state has a unique key which associates it with the container, rather than
 * the container containing a link to the state. This minimizes collisions for
 * parallel transactions on different states.
 */
public class DocumentStateListImpl implements DocumentStateList {

    // use the classname for the logger, this way you can refactor
    private final static Logger LOG = Logger.getLogger(DocumentContract.class.getName());

    private Context ctx;
    private String name;
    private Object supportedClasses;
    private StateDeserializer deserializer;

    /**
     * Store Fabric context for subsequent API access, and name of list
     *
     * @param deserializer
     */
    public DocumentStateListImpl(Context ctx, String listName, StateDeserializer deserializer) {
        this.ctx = ctx;
//        this.name = listName;
        this.name = LIST_NAME;
        this.deserializer = deserializer;

    }

    /**
     * Add a state to the list. Creates a new state in worldstate with appropriate
     * composite key. Note that state defines its own key. State object is
     * serialized before writing.
     */
    @Override
    public DocumentStateList addState(State state) {
        LOG.info("Adding state " + this.name);
        ChaincodeStub stub = this.ctx.getStub();
        LOG.info("Stub=" + stub);
        String[] splitKey = state.getSplitKey();
        LOG.info("Split key " + Arrays.asList(splitKey));

        CompositeKey ledgerKey = stub.createCompositeKey(this.name, splitKey);
        LOG.info("ledgerkey is ");
        LOG.info(ledgerKey.toString());

        byte[] data = State.serialize(state);
        LOG.info("ctx" + this.ctx);
        LOG.info("stub" + this.ctx.getStub());
        this.ctx.getStub().putState(ledgerKey.toString(), data);
        return this;
    }

    /**
     * Get a state from the list using supplied keys. Form composite keys to
     * retrieve state from world state. State data is deserialized into JSON object
     * before being returned.
     */
    @Override
    public State getState(String key) {

        CompositeKey ledgerKey = this.ctx.getStub().createCompositeKey(this.name, State.splitKey(key));

        byte[] data = this.ctx.getStub().getState(ledgerKey.toString());
        LOG.info("Data is " + Utility.byteArrayToString(data));
        LOG.info("LedgerKey " + ledgerKey.toString());
        if (data != null) {
            State state = this.deserializer.deserialize(data);
            return state;
        } else {
            return null;
        }
    }

    /**
     * Update a state in the list. Puts the new state in world state with
     * appropriate composite key. Note that state defines its own key. A state is
     * serialized before writing. Logic is very similar to addState() but kept
     * separate becuase it is semantically distinct.
     */
    @Override
    public DocumentStateList updateState(State state) {
        CompositeKey ledgerKey = this.ctx.getStub().createCompositeKey(this.name, state.getSplitKey());
        byte[] data = State.serialize(state);
        this.ctx.getStub().putState(ledgerKey.toString(), data);

        return this;
    }

}
