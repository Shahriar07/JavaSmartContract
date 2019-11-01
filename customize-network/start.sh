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

docker-compose -f docker-compose.yml up -d ca.konai.com konaorderer.konai.com kslpeer1.orgkona.konai.com kslpeer2.orgkona.konai.com couchdb
docker ps -a

# wait for Hyperledger Fabric to start
# incase of errors when running later commands, issue export FABRIC_START_TIMEOUT=<larger number>
export FABRIC_START_TIMEOUT=10
#echo ${FABRIC_START_TIMEOUT}
sleep ${FABRIC_START_TIMEOUT}

# Create the channel
docker exec -e "CORE_PEER_LOCALMSPID=OrgKonaMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgkona.konai.com/msp" kslpeer1.orgkona.konai.com peer channel create -o konaorderer.konai.com:7050 -c konachannel -f /etc/hyperledger/configtx/channel.tx
# Join peer0.org1.example.com to the channel.
docker exec -e "CORE_PEER_LOCALMSPID=OrgKonaMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgkona.konai.com/msp" kslpeer1.orgkona.konai.com peer channel join -b konachannel.block

docker ps -a

docker exec -e "CORE_PEER_LOCALMSPID=OrgKonaMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgkona.konai.com/msp" -e "CORE_PEER_ADDRESS=kslpeer2.orgkona.konai.com:8051" kslpeer2.orgkona.konai.com peer channel fetch 0 konachannel.block -c konachannel -o konaorderer.konai.com:7050

# Join peer1.org1.example.com to the channel.
docker exec -e "CORE_PEER_LOCALMSPID=OrgKonaMSP" -e "CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/msp/users/Admin@orgkona.konai.com/msp" -e "CORE_PEER_ADDRESS=kslpeer2.orgkona.konai.com:8051" kslpeer2.orgkona.konai.com peer channel join -o konaorderer.konai.com:7050 -b ./konachannel.block
