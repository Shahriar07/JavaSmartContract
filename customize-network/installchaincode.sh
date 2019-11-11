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

# Install the chaincode to store document for kslpeer1.orgkona.konai.com peer
docker exec -e "CORE_PEER_LOCALMSPID=OrgMardMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgmard.mard.com/msp" -e "CORE_PEER_ADDRESS=mardpeer1.orgmard.mard.com:5051"  mardpeer1.orgmard.mard.com peer chaincode install -n documenthandler -v 1.0 -l java -p /etc/hyperledger/contracts

export FABRIC_START_TIMEOUT=10
#echo ${FABRIC_START_TIMEOUT}
sleep ${FABRIC_START_TIMEOUT}

# Install the chaincode to store document for vfapeer1.orgvfa.vfas.com peer
docker exec -e "CORE_PEER_LOCALMSPID=OrgVFAMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgvfa.vfas.com/msp" -e "CORE_PEER_ADDRESS=vfapeer1.orgvfa.vfas.com:9051"  vfapeer1.orgvfa.vfas.com peer chaincode install -n documenthandler -v 1.0 -l java -p /etc/hyperledger/contracts
