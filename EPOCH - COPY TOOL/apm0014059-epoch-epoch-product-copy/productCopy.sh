#!/bin/bash

# Script to run script to copy products in a product keys list file from source to target project
# Prerequisites:
#   Nodejs (latest version)
#   npm (latest version)
#   yarn (latest version)
#   babel (latest version)

if [[ $# -lt 3 ]]; then
    echo "CT source and destination project required"
    echo "Usage: $0 <source project key> <target product key> <product keys file> . Ex. $0 epoch-staging2 epoch-dev products.txt"
    exit 1
fi

. ./env.sh

export TASK=productCopyByKeys
export PRODUCT_KEYS_FILE=$(readlink -f "${3}")

if [ ! -f "$PRODUCT_KEYS_FILE" ]; then
    echo "$PRODUCT_KEYS_FILE does not exist."
    exit 1
fi

if [ ${1} == ${2} ]; then
    TASK=productCopyFromExisting
fi

if [ $TASK == 'productCopyByKeys' ]; then
    echo 'Running products-by-keys copy script'
fi

if [ $TASK == 'productCopyFromExisting' ]; then
    echo 'Running products-by-keys-from-existing copy script'
fi

npm install && \
yarn build && \
node ./dist/main.js && \
(rm -frv dist || echo 'No temporary dist files to delete in directory') && \
echo 'Done executing script'
