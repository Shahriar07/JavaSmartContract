package com.konasl.documenthandler.auth;

import com.konasl.documenthandler.constants.NetworkConstants;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Prepare user wallet and connect to the hyperledger network
 *
 * @author H. M. Shahriar (h.m.shahriar@konasl.com)
 * @since 10/10/2019 15:23
 */

@Service
public class AuthUser {

    @Autowired
    NetworkConstants networkConstants;

    /**
     * Prepare wallet user from the generated key
     * Using the network connection file connect with the hyperledger network
     *
     * @return connected network
     */
    public Network authUserandGenerateNetwork() {

        Gateway.Builder builder = Gateway.createBuilder();

        try {
            // A wallet stores a collection of identities
            Path walletPath = Paths.get(networkConstants.getClientWalletPath());
            Wallet wallet = Wallet.createFileSystemWallet(walletPath);

            String userName = networkConstants.getIdentityLabel();

            Path connectionProfile = Paths.get(networkConstants.getNetworkConfPath());

            // Set connection options on the gateway builder
            builder.identity(wallet, userName).networkConfig(connectionProfile).discovery(false);

            // Connect to gateway using application specified parameters
            Gateway gateway = builder.connect();
            if (gateway != null) {
                // Access kona network
                System.out.println("Use network channel: " + networkConstants.getChannelName());
                return gateway.getNetwork( networkConstants.getChannelName());

            } else {
                System.out.println("Gateway generation failed ");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
