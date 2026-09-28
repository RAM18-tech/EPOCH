#!/bin/bash

# Script to run script to copy products in a product keys list file from source to target project
# Prerequisites:
#   Nodejs (latest version)
#   npm (latest version)
#   yarn (latest version)
#   babel (latest version)

if [[ $# -lt 3 ]]; then
    echo "CT source and destination project required"
    echo "Usage: $0 <source project key> <target project key> <discount keys file> . Ex. $0 epoch-staging2 epoch-dev discounts.txt"
    exit 1
fi

. ./env.sh

export TASK=cartDiscountCopyByKeys
export CART_DISCOUNT_KEYS_FILE=$(readlink -f "${3}")

if [ ! -f "$CART_DISCOUNT_KEYS_FILE" ]; then
    echo "$CART_DISCOUNT_KEYS_FILE does not exist."
    exit 1
fi

echo 'Running cart-discounts-by-keys copy script'

npm install && \
yarn build && \
node ./dist/main.js && \
(rm -frv dist || echo 'No temporary dist files to delete in directory') && \
echo 'Done executing script'
