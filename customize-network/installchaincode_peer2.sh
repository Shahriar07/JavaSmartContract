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
docker exec -e "CORE_PEER_LOCALMSPID=OrgKonaMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgkona.konai.com/msp" -e "CORE_PEER_ADDRESS=kslpeer2.orgkona.konai.com:8051"  kslpeer2.orgkona.konai.com peer chaincode install -n documenthandler -v 1.0 -l java -p /etc/hyperledger/contracts
