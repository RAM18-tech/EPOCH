#!/bin/bash

# Script to run Satellite Product Copy script to copy satellite products from source to target project
# Prerequisites:
#   Nodejs (latest version)
#   npm (latest version)
#   yarn (latest version)
#   babel (latest version)

if [[ $# -lt 2 ]]; then
    echo "CT source and destination project required"
    echo "Usage: $0 <source project key> <target product key> . Ex. $0 epoch-staging2 epoch-dev"
    exit 1
fi

. ./env.sh

echo 'Running satellite products copy script'

export TASK=satelliteProductCopy

npm install && \
yarn build && \
node ./dist/main.js && \
(rm -frv dist || echo 'No temporary dist files to delete in directory') && \
echo 'Done executing script'
