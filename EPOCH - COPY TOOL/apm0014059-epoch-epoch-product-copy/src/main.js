/*
Running the satellite offer/discout copy tool

export HTTP_PROXY=http://sub.proxy.att.com:8080;
export HTTPS_PROXY=http://sub.proxy.att.com:8080;

export SOURCE_CLIENT_ID='5WeaCOykrqu9s3PFVh5msY2k'
export SOURCE_CLIENT_SECRET='6lXSiFndzSxuHe04s8mHBSe3rZZXRqBc'
export SOURCE_PROJECT_KEY='epoch-staging2'
export SOURCE_SCOPES='manage_project:epoch-staging2 manage_api_clients:epoch-staging2 view_api_clients:epoch-staging2'

export TARGET_CLIENT_ID='VjADKLsGAKoUAXXQTD2L4uvB'
export TARGET_CLIENT_SECRET='wpUuMPEW46NR_7E0CFPyTmzZqNCInQMz'
export TARGET_PROJECT_KEY='epoch-dev'
export TARGET_SCOPES='manage_project:epoch-dev manage_api_clients:epoch-dev view_api_clients:epoch-dev'

#################################################

For satellite offers product copy:

export TASK=satelliteProductCopy

For product copy by keys:

export TASK=productCopyByKeys
export PRODUCT_KEYS_FILE=/home/jh315p/product.txt

For cart discount copy by keys:

export TASK=cartDiscountCopyByKeys
export CART_DISCOUNT_KEYS_FILE=/home/jh315p/discounts.txt

#################################################

yarn build && node ./dist/main.js
*/

import { config } from 'dotenv';
import { SatelliteProductCopy } from './lib/satelliteProductCopy';
import { ProductCopy } from './lib/productCopy';
import { forEachLine, stripWhitespace } from './lib/utils';
import { CartDiscountCopy } from './lib/cartDiscountCopy';
import ProductCreate from './lib/productCreate';

const SATELLITE_PRODUCT_COPY = 'satelliteProductCopy';
const PRODUCT_COPY_BY_KEYS = 'productCopyByKeys';
const PRODUCT_COPY_FROM_EXISTING = 'productCopyFromExisting';
const CART_DISCOUNT_COPY_BY_KEYS = 'cartDiscountCopyByKeys';

const emptyTask = () => {
  const service = {};
  service.run = async () => {
    console.log('Empty task. Doing nothing.');
    return 0;
  };
  return service;
};

const main = async () => {
  config();

  const task = process.env.TASK;
  const productKeysFile = process.env.PRODUCT_KEYS_FILE;
  const cartDiscountKeysFile = process.env.CART_DISCOUNT_KEYS_FILE;

  const apiSourceInfo = {
    clientId: process.env.SOURCE_CLIENT_ID,
    clientSecret: process.env.SOURCE_CLIENT_SECRET,
    projectKey: process.env.SOURCE_PROJECT_KEY,
    host: process.env.SOURCE_HOST,
    oauthHost: process.env.SOURCE_OAUTH_HOST,
    scopes: process.env.SOURCE_SCOPES.split(' '),
  };

  const apiDestinationInfo = {
    clientId: process.env.TARGET_CLIENT_ID,
    clientSecret: process.env.TARGET_CLIENT_SECRET,
    projectKey: process.env.TARGET_PROJECT_KEY,
    host: process.env.TARGET_HOST,
    oauthHost: process.env.TARGET_OAUTH_HOST,
    scopes: process.env.TARGET_SCOPES.split(' '),
  };

  let copyService = emptyTask();

  if (task === SATELLITE_PRODUCT_COPY) {
    copyService = SatelliteProductCopy({ apiSourceInfo, apiDestinationInfo });
  } else if (task === PRODUCT_COPY_BY_KEYS) {
    console.log(`Reading product keys from file: ${productKeysFile}`);

    const productKeys = [];

    await forEachLine(productKeysFile, line => {
      const productKey = stripWhitespace(line);

      if (productKey) {
        productKeys.push(productKey);
      }
    });

    copyService = ProductCopy({ apiSourceInfo, apiDestinationInfo, productKeys });
  }else if (task === PRODUCT_COPY_FROM_EXISTING) {
    console.log(`Reading product keys from file: ${productKeysFile}`);

    const productKeys = [];

    await forEachLine(productKeysFile, line => {
      const productKey = stripWhitespace(line);

      if (productKey) {
        productKeys.push(productKey);
      }
    });

    copyService = ProductCreate({ apiSourceInfo, apiDestinationInfo, productKeys });
  } else if (task === CART_DISCOUNT_COPY_BY_KEYS) {
    console.log(`Reading cart discount keys from file: ${cartDiscountKeysFile}`);

    const discountKeys = [];

    await forEachLine(cartDiscountKeysFile, line => {
      const discountKey = stripWhitespace(line);

      if (discountKey) {
        discountKeys.push(discountKey);
      }
    });

    copyService = CartDiscountCopy({ apiSourceInfo, apiDestinationInfo, discountKeys });
  }

  const timeTaken = await copyService.run();

  console.log(`Time taken: ${timeTaken} ms`);
};

main();
