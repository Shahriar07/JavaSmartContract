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

docker-compose -f docker-compose.yml down

docker-compose -f docker-compose.yml up -d ca.konai.com vfaorderer.orderer.com mardpeer1.orgmard.mard.com vfapeer1.orgvfa.vfas.com couchdb
docker ps -a

# wait for Hyperledger Fabric to start
# incase of errors when running later commands, issue export FABRIC_START_TIMEOUT=<larger number>
export FABRIC_START_TIMEOUT=10
#echo ${FABRIC_START_TIMEOUT}
sleep ${FABRIC_START_TIMEOUT}


# Create the channel
docker exec -e "CORE_PEER_LOCALMSPID=OrgVFAMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgvfa.vfas.com/msp" -e "CORE_PEER_ADDRESS=vfapeer1.orgvfa.vfas.com:9051" vfapeer1.orgvfa.vfas.com peer channel create -o vfaorderer.orderer.com:7050 -c vfachannel -f /etc/hyperledger/configtx/channel.tx

# Join vfapeer1.orgvfa.vfas.com to the channel.
docker exec -e "CORE_PEER_LOCALMSPID=OrgVFAMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgvfa.vfas.com/msp" -e "CORE_PEER_ADDRESS=vfapeer1.orgvfa.vfas.com:9051" vfapeer1.orgvfa.vfas.com peer channel join -b vfachannel.block -o vfaorderer.orderer.com:7050

docker exec -e "CORE_PEER_LOCALMSPID=OrgMardMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgmard.mard.com/msp" -e "CORE_PEER_ADDRESS=mardpeer1.orgmard.mard.com:5051" mardpeer1.orgmard.mard.com peer channel fetch 0 vfachannel.block -c vfachannel -o vfaorderer.orderer.com:7050

# Join kslpeer1.orgkona.konai.com to the channel.
docker exec -e "CORE_PEER_LOCALMSPID=OrgMardMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgmard.mard.com/msp" -e "CORE_PEER_ADDRESS=mardpeer1.orgmard.mard.com:5051" mardpeer1.orgmard.mard.com peer channel join -b vfachannel.block -o vfaorderer.orderer.com:7050


#docker exec -e "CORE_PEER_LOCALMSPID=OrgVFAMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgvfa.vfas.com/msp" -e "CORE_PEER_ADDRESS=vfapeer1.orgvfa.vfas.com:9051" vfapeer1.orgvfa.vfas.com peer channel fetch 0 vfachannel.block -c vfachannel -o vfaorderer.orderer.com:7030

# Join vfapeer1.orgvfa.vfas.com to the channel.
#docker exec -e "CORE_PEER_LOCALMSPID=OrgVFAMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgvfa.vfas.com/msp" -e "CORE_PEER_ADDRESS=vfapeer1.orgvfa.vfas.com:9051" vfapeer1.orgvfa.vfas.com peer channel join -b vfachannel.block -o vfaorderer.orderer.com:7030
