/*
SPDX-License-Identifier: Apache-2.0
*/

package com.konasl.documenthandler.auth;

import com.konasl.documenthandler.constants.NetworkConstants;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallet.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import sun.nio.ch.Net;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generate wallet required key files from keystore
 * Will be used later to communicate with blockchain network
 */

@Service
public class WalletInitilizer {

    @Autowired
    NetworkConstants networkConstants;

    @Bean
    public void initializeUserWallet() {
        System.out.println("User wallet initialization started");
        try {
            // A wallet stores a collection of identities
            Path walletPath = Paths.get("..","identity", "user", "shahriar", "wallet");
            Wallet wallet = Wallet.createFileSystemWallet(walletPath);

            // Location of credentials to be stored in the wallet
            Path credentialPath = Paths.get("..","crypto-config",
                    "peerOrganizations", "orgkona.konai.com", "users", "User1@orgkona.konai.com", "msp");
            Path certificatePem = credentialPath.resolve(Paths.get("signcerts",
                    "User1@orgkona.konai.com-cert.pem"));
            Path privateKey = credentialPath.resolve(Paths.get("keystore",
                    networkConstants.getKeyFileName()));

            // Load credentials into wallet
            String identityLabel = "User1@orgkona.konai.com";
            Identity identity = Identity.createIdentity("OrgKonaMSP", Files.newBufferedReader(certificatePem), Files.newBufferedReader(privateKey));

            wallet.put(identityLabel, identity);
            System.out.println("Wallet key file generated successfully");

        } catch (IOException e) {
            System.err.println("Error adding to wallet");
            e.printStackTrace();
        }
    }

}
