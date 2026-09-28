import { cloneDeep, isEqual } from 'lodash';
import { createSyncProducts } from '@commercetools/sync-actions';
import { handleError, NotFoundError } from '../commercetools/errors';

const syncProducts = createSyncProducts([
  { type: 'categoryOrderHints', group: 'black' },
  { type: 'base', group: 'white' },
  { type: 'meta', group: 'white' },
  { type: 'references', group: 'white' },
  { type: 'prices', group: 'white' },
  { type: 'attributes', group: 'white' },
  { type: 'images', group: 'white' },
  { type: 'variants', group: 'white' },
]);

export const attributeBlacklist = [
  'qualifyingProductIds',
  'qualifyingProductTypes',
  'qualifyingProductFamily',
  'qualifyingProductCategories',
  'qualifyingProductStatuses',
  'excludedProductIds',
  'excludedProductTypes',
  'excludedProductFamily',
  'excludedProductCategories',
  'excludedProductStatuses',
  // 'includedProductIds',
  'includedProductTypes',
  'includedProductFamily',
  'includedProductCategories',
  'includedProductStatuses',
  'compatibleProductIds',
  'compatibleProductTypes',
  'compatibleProductFamily',
  'compatibleProductCategories',
  'compatibleProductStatuses',
  'eligibilityCustomerTypes',
  'eligibilityBusinessSegment',
  'eligibilityZips',
  'eligibilityCustomerSegments',
  'eligibilitySalesChannels',
  'eligibilityMinimumPurchaseAmounts',
];

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

const enumKVtoKeys = value => (value && value.key ? value.key : value);

export const toNameValueMap = nameValues => {
  return nameValues.reduce((o, item) => ({ ...o, [item.name]: item.value }), {});
};

export const enumValuesToKeys = values => {
  if (Array.isArray(values)) {
    const isNested = values.find(x => !Array.isArray(x) && typeof x.value !== 'undefined');
    if (isNested) {
      return values.map(x => ({ name: x.name, value: enumValuesToKeys(x.value) }));
    } else {
      return values.map(enumValuesToKeys);
    }
  }
  return enumKVtoKeys(values);
};


export const isNestedValueEqual = (plainValue, nestedRepresentation) => {
  try {
    const plainRep = enumValuesToKeys(nestedRepresentation);
    const equality = isEqual(plainRep, plainValue);
    return equality;
  } catch (e) {
    console.warn(e);
  }
  return false;
};


export const ProductsService = ({ commercetools, sleepLength }) => {
  const productsService = {};
  const { client, getRequestBuilder } = commercetools;

  /**
   * Retrieves a product by key
   */
  productsService.byKey = async ({ key }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder.products.parse({ key }).build(),
        method: 'GET',
      });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      handleError(err);
    }
  };

  productsService.getByKeys = async ({ keys = [] }) => {
    if (!keys || !keys.length) {
      return [];
    }
    const requestBuilder = getRequestBuilder();
    const where = [`key in("${keys.join('", "')}")`];
    try {
      const result = await client.execute({
        uri: requestBuilder.products.parse({ where, perPage: 200 }).build(),
        method: 'GET',
      });
      await sleep(sleepLength);
      return result.body.results;
    } catch (err) {
      handleError(err);
    }
  };

  productsService.getOffersWithProducts = async ({ ids = [] }) => {
    if (!ids || !ids.length) {
      return [];
    }
    const requestBuilder = getRequestBuilder();
    const where = [`masterVariant(attributes(name="bundleProductIds" and value(id in("${ids.join('", "')}"))))`];
    try {
      const result = await client.execute({
        uri: requestBuilder.productProjections.parse({ where, perPage: 200, staged: true }).build(),
        method: 'GET',
      });
      await sleep(sleepLength);
      return result.body.results;
    } catch (err) {
      handleError(err);
    }
  };

  /**
   * Retrieves a product by id
   */
  productsService.byId = async ({ id }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder.products.parse({ id }).build(),
        method: 'GET',
      });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      handleError(err);
    }
  };

  /**
   * Given a set of actions, updates a product
   */
  productsService.update = async (product, actions) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder.products.parse({ id: product.id }).build(),
        method: 'POST',
        body: JSON.stringify({
          version: product.version,
          actions,
        }),
      });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      console.error(JSON.stringify(err.body.errors), product.key, product.id);
      console.info('request actions', JSON.stringify(actions));
    }
  };

  /**
   * Fetches a certain page of results given a certain where clause
   */
  productsService.fetch = async ({
    where = [], page = 1, perPage = 200, resourceTypeId = 'products', sort = [{ by: 'createdAt', direction: 'desc' }],
  }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const results = await client.execute({
        uri: requestBuilder[resourceTypeId].parse({
          where,
          page,
          perPage,
          sort,
        }).build(),
        method: 'GET',
      });
      await sleep(sleepLength);
      return results.body.results;
    } catch (err) {
      console.warn(`Error fetching ${resourceTypeId} where: ${where}`);
      console.info(err);
    }
  };

  productsService.fetchAll = async ({
    sort = [{ by: 'createdAt', direction: 'desc' }],
    where = [],
  }) => {
    let allResults = [];
    let page = 1;

    let results = await productsService.fetch({ where, page, sort });
    while (results.length) {
      allResults = [...allResults, ...results];
      page += 1;
      // eslint-disable-next-line no-await-in-loop
      results = await productsService.fetch({ where, page, sort });
    }
    return allResults;
  };
  productsService.delete = async (product) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: `${requestBuilder.products.parse({ id: product.id }).build()}?version=${product.version}`,
        method: 'DELETE',
      });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      console.error(JSON.stringify(err.body));
    }
  };

  /**
   * Given a ProductDraft, creates a product
   */
  productsService.create = async product => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder.products.build(),
        method: 'POST',
        body: JSON.stringify(product),
      });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      console.warn(JSON.stringify(product));
      console.info(err.body.errors);
    }
  };

  /**
   * Creates a new product or updates an existing product.
   */
  productsService.createOrUpdate = async (product, sameForAll) => {
    // look up by key
    let existingProduct;
    if (product.key) {
      try {
        existingProduct = await productsService.byKey({ key: product.key });
      } catch (e) {
        // disregard any 404, as it can be expected.
        if (!(e instanceof NotFoundError)) {
          throw e;
        }
      }
    }
    if (existingProduct && existingProduct.id) {
      const draftToProductProjection = cloneDeep(existingProduct);
      draftToProductProjection.key = `${product.key}`;

      if (product.masterData && product.masterData.staged) {
        draftToProductProjection.masterData.staged = cloneDeep(product.masterData.staged);
      } else {
        draftToProductProjection.masterData.staged = cloneDeep(product);
      }

      // draftToProductProjection.masterData.staged.variants = [draftToProductProjection.masterData.staged.masterVariant]
      delete draftToProductProjection.masterData.staged.key;
      delete draftToProductProjection.masterData.staged.productType;
      delete draftToProductProjection.masterData.staged.publish;
      // hack to trick syncActions into thinking we've got the id already.
      draftToProductProjection.masterData.staged.masterVariant.id = 1;
      if (
        existingProduct.masterData
        && existingProduct.masterData.staged.masterVariant
      ) {
        draftToProductProjection.masterData.staged.masterVariant.id = existingProduct.masterData.staged.masterVariant.id;

        if (!draftToProductProjection.masterData.staged.masterVariant.prices) {
          draftToProductProjection.masterData.staged.masterVariant.prices = [];
        }

        // if there's existing prices with ids
        if (
          existingProduct.masterData.staged.masterVariant.prices
          && draftToProductProjection.masterData.staged.masterVariant.prices
        ) {
          // match prices by amount & dates

          const draftPrices = draftToProductProjection.masterData.staged.masterVariant.prices;

          if (
            existingProduct.masterData.staged.masterVariant.prices.length
            === draftPrices.length
          ) {
            draftPrices.forEach((price, index) => {
              price.id = existingProduct.masterData.staged.masterVariant.prices[
                index.id
              ];
            });
          } else {
            existingProduct.masterData.staged.masterVariant.prices.forEach(price => {
              // TODO: channel (to make this generic)
              // TODO: customergroup (to make this generic)
              const matchedPrice = draftPrices.find(x => x.validUntil === price.validUntil
                && x.validFrom === price.validFrom
                && x.value.centAmount === price.value.centAmount
                && x.value.currencyCode === price.value.currencyCode);
              if (matchedPrice) {
                matchedPrice.id = price.id;
              }
            });
          }
        }
      }
      let actions = [];
      try {
        actions = syncProducts.buildActions(
          draftToProductProjection.masterData.staged,
          existingProduct.masterData.staged,
        );
      } catch (error) {
        console.error(
          `error during building sync actions for ${product.key}`,
          error,
        );
      }

      // for some reason it kept adding "setAttribute" for billingProductCode
      actions = actions.filter(x => {
        const matchingAttr = existingProduct.masterData.staged.masterVariant.attributes.find(attr => attr.name === x.name);
        if (
          x.action === 'setAttribute'
          && matchingAttr && (
            (x.value === matchingAttr.value)
            || (matchingAttr.value.key === x.value) // enums
            || isNestedValueEqual(x.value, matchingAttr.value)
          )
        ) {
          return false;
        } else if (x.action === 'setAttribute' && attributeBlacklist.indexOf(x.name) !== -1) {
          return false;
        } else if (x.action === 'transitionState' && product.state && x.state.id === product.state.id) {
          return false;
        }
        return true;
      }).map(x => {
        if (x.action === 'transitionState') {
          return { ...x, force: true };
        }
        return x;
      });

      if (draftToProductProjection.masterData.staged.masterVariant.assets) {
        existingProduct = await productsService.updateAssets(
          existingProduct,
          draftToProductProjection.masterData.staged.masterVariant.assets,
          existingProduct.masterData.staged.masterVariant.assets,
        );
      }

      if (!actions.length) {
        return existingProduct;
      }

      if (sameForAll) {
        actions.forEach(action => {
          if (sameForAll.includes(action.name)) {
            action.action = 'setAttributeInAllVariants';
            delete action.variantId;
          }
        });
      }

      console.info(`Updating product ${product.key}`, JSON.stringify(actions));
      return productsService.update(
        existingProduct,
        actions,
      );
    }
    console.info(`Creating product ${product.key}`);
    return productsService.create(product);
  };

  productsService.updateAssets = async (product, newAssets, existingAssets) => {
    const requestBuilder = getRequestBuilder();
    const assetActions = newAssets.map((newAsset) => {
      if (newAsset.custom && newAsset.custom.fields) {
        if (newAsset.custom.fields.longDesc === '') {
          delete newAsset.custom.fields.longDesc;
        }
        if (newAsset.custom.fields.shortDesc === '') {
          delete newAsset.custom.fields.shortDesc;
        }
      }
      return {
        action: 'addAsset',
        variantId: 1,
        asset: newAsset,
      };
    });
    try {
      if (assetActions.length === 0) {
        return product;
      }
      const deleteExistingAssetActions = existingAssets.map(existingAsset => {
        return {
          action: 'removeAsset',
          variantId: 1,
          assetId: existingAsset.id,
        };
      });
      const combinedAssetActions = [...deleteExistingAssetActions, ...assetActions];
      // console.log('Asset actions ', assetActions);
      const result = await client.execute({
        uri: requestBuilder.products.parse({ id: product.id }).build(),
        method: 'POST',
        body: JSON.stringify({
          version: product.version,
          actions: combinedAssetActions,
        }),
      });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      console.error(JSON.stringify(err.body.errors));
      console.info('request assetactions', JSON.stringify(assetActions));
      return product;
    }
  };


  return productsService;
};
export default ProductsService;
