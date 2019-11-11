#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
#
# Exit on first error, print all commands.
set -ev

# don't rewrite paths for Windows Git Bash users
export MSYS_NO_PATHCONV=1

docker ps -a

# Install the chaincode to store document
docker exec -e "CORE_PEER_LOCALMSPID=OrgVFAMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgvfa.vfas.com/msp" -e "CORE_PEER_ADDRESS=vfapeer1.orgvfa.vfas.com:9051"  vfapeer1.orgvfa.vfas.com peer chaincode install -n documenthandler -v 1.0 -l java -p /etc/hyperledger/contracts

#docker exec -e "CORE_PEER_LOCALMSPID=OrgVFAMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgvfa.vfas.com/msp" -e "CORE_PEER_ADDRESS=vfapeer1.orgvfa.vfas.com:9051" vfapeer1.orgvfa.vfas.com peer channel join -b konachannel.block -o konaorderer.konai.com:7050
