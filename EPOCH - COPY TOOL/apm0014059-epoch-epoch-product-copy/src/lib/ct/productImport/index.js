import '@babel/polyfill';
import _ from 'lodash';
import { Commercetools } from '../commercetools';
import { ProductsService } from '../products';
import { DataCache } from '../dataCache';
import { findObjectsByProperty, findObjectPaths, isNumeric, objectPathDelimiter } from '../../utils';

export const ProductImport = ({ apiSourceInfo, apiDestinationInfo }) => {
  const service = {};
  const destProductsService = ProductsService({ commercetools: Commercetools(apiDestinationInfo), sleepLength: 1500 });
  const dataCache = DataCache({ apiSourceInfo, apiDestinationInfo });
  const productReconciliationQueue = [];
  const retriedProductsByKey = {};

  const importProduct = async (exportedProductDraft) => {
    console.log(`Importing product: ${exportedProductDraft.key}`);

    const sourceProductTypeId = exportedProductDraft.productType.id;

    if (dataCache.sourceProjectCache.productTypes[sourceProductTypeId]) {
      const sourceProductTypeKey = dataCache.sourceProjectCache.productTypes[sourceProductTypeId].key;

      const destProductTypeId = Object.values(dataCache.destinationProjectCache.productTypes)
        .find(productType => productType.key === sourceProductTypeKey).id;

      exportedProductDraft.productType.id = destProductTypeId;
    }

    if (exportedProductDraft.state) {
      const sourceStateId = exportedProductDraft.state.id;

      if (dataCache.sourceProjectCache.states[sourceStateId]) {
        const sourceStateKey = dataCache.sourceProjectCache.states[sourceStateId].key;
        const destStateId = Object.values(dataCache.destinationProjectCache.states).find(state => state.key === sourceStateKey).id;
        exportedProductDraft.state.id = destStateId;
      }
    }

    exportedProductDraft.masterVariant.prices.forEach(price => {
      if (price.customerGroup && price.customerGroup.id) {
        const sourceCustomerGroupId = price.customerGroup.id;

        if (dataCache.sourceProjectCache.customerGroups[sourceCustomerGroupId]) {
          const sourceCustomerGroupKey = dataCache.sourceProjectCache.customerGroups[sourceCustomerGroupId].key;

          if (sourceCustomerGroupKey) {
            const destCustomerGroup = Object.values(dataCache.destinationProjectCache.customerGroups)
              .find(cg => cg.key === sourceCustomerGroupKey);

            if (destCustomerGroup) {
              price.customerGroup.id = destCustomerGroup.id;
            }
          }
        }
      }

      if (price.channel && price.channel.id) {
        const sourceChannelId = price.channel.id;

        if (dataCache.sourceProjectCache.channels[sourceChannelId]) {
          const sourceChannelKey = dataCache.sourceProjectCache.channels[sourceChannelId].key;

          if (sourceChannelKey) {
            const destChannel = Object.values(dataCache.destinationProjectCache.channels).find(channel => channel.key === sourceChannelKey);

            if (destChannel) {
              price.channel.id = destChannel.id;
            }
          }
        }
      }

      if (price.custom && price.custom.type && price.custom.type.id) {
        const sourceCustomTypeId = price.custom.type.id;

        if (dataCache.sourceProjectCache.types[sourceCustomTypeId]) {
          const sourceTypeKey = dataCache.sourceProjectCache.types[sourceCustomTypeId].key;

          if (sourceTypeKey) {
            const destType = Object.values(dataCache.destinationProjectCache.types).find(type => type.key === sourceTypeKey);

            if (destType) {
              price.custom.type.id = destType.id;
            }
          }
        }
      }
    });

    if (exportedProductDraft.variants) {
      exportedProductDraft.variants.forEach(variant => {
        if (variant.prices) {
          variant.prices.forEach(price => {
            if (price.customerGroup && price.customerGroup.id) {
              const sourceCustomerGroupId = price.customerGroup.id;

              if (dataCache.sourceProjectCache.customerGroups[sourceCustomerGroupId]) {
                const sourceCustomerGroupKey = dataCache.sourceProjectCache.customerGroups[sourceCustomerGroupId].key;

                if (sourceCustomerGroupKey) {
                  const destCustomerGroup = Object.values(dataCache.destinationProjectCache.customerGroups)
                    .find(cg => cg.key === sourceCustomerGroupKey);

                  if (destCustomerGroup) {
                    price.customerGroup.id = destCustomerGroup.id;
                  }
                }
              }
            }

            if (price.channel && price.channel.id) {
              const sourceChannelId = price.channel.id;

              if (dataCache.sourceProjectCache.channels[sourceChannelId]) {
                const sourceChannelKey = dataCache.sourceProjectCache.channels[sourceChannelId].key;

                if (sourceChannelKey) {
                  const destChannel = Object.values(dataCache.destinationProjectCache.channels)
                    .find(channel => channel.key === sourceChannelKey);

                  if (destChannel) {
                    price.channel.id = destChannel.id;
                  }
                }
              }
            }

            if (price.custom && price.custom.type && price.custom.type.id) {
              const sourceCustomTypeId = price.custom.type.id;

              if (dataCache.sourceProjectCache.types[sourceCustomTypeId]) {
                const sourceTypeKey = dataCache.sourceProjectCache.types[sourceCustomTypeId].key;

                if (sourceTypeKey) {
                  const destType = Object.values(dataCache.destinationProjectCache.types).find(type => type.key === sourceTypeKey);

                  if (destType) {
                    price.custom.type.id = destType.id;
                  }
                }
              }
            }
          });
        }
      });
    }

    // A bit more complicated with reference product ids
    await reconcileProductIdReferences(exportedProductDraft);

    try {
      await destProductsService.createOrUpdate(exportedProductDraft);
    } catch (err) {
      console.error(err);
    }
  };

  const reconcileProductIdReferences = async (exportedProductDraft) => {
    let hasMissingReferences = false;
    const draftToReconcile = _.cloneDeep(exportedProductDraft);
    const prodIdsToStrip = [];

    for (let i = 0; i < exportedProductDraft.masterVariant.attributes.length; i += 1) {
      const attribute = exportedProductDraft.masterVariant.attributes[i];
      const nestedProductIdsObjects = findObjectsByProperty(attribute, 'typeId', 'product');

      for (let j = 0; j < nestedProductIdsObjects.length; j += 1) {
        const nestedProductIdsObject = nestedProductIdsObjects[j];
        const sourceProductId = nestedProductIdsObject.id;
        const sourceCacheProduct = await dataCache.sourceProjectCache.products.get(sourceProductId);

        if (sourceCacheProduct) {
          const sourceProductKey = sourceCacheProduct.key;

          if (sourceProductKey) {
            const destProduct = await dataCache.destinationProjectCache.products.getByKey(sourceProductKey);

            if (destProduct) {
              nestedProductIdsObject.id = destProduct.id;
            } else {
              hasMissingReferences = true;
              prodIdsToStrip.push(nestedProductIdsObject.id);
            }
          }
        }
      }
    }

    if (hasMissingReferences && !retriedProductsByKey[draftToReconcile.key]) {
      stripProductIdAttributeObjects(exportedProductDraft, prodIdsToStrip);

      // Defer to queue and correct later
      productReconciliationQueue.push(draftToReconcile);
    }
  };

  const retryProductsWithMissingReferences = async () => {
    if (productReconciliationQueue.length) {
      dataCache.clearProductCache();

      while (productReconciliationQueue.length > 0) {
        const exportedProductDraft = productReconciliationQueue.shift();

        if (exportedProductDraft && exportedProductDraft.key) {
          console.log(`Reconciling product of missed references: ${exportedProductDraft.key}`);

          retriedProductsByKey[exportedProductDraft.key] = true;

          await importProduct(exportedProductDraft);
        }
      }
    }
  };

  /**
   * Find and remove attribute value objects from the master variant / variants that have the listed
   * reference product resource ids.
   */
  const stripProductIdAttributeObjects = (exportedProductDraft, productIdsToStrip) => {
    if (exportedProductDraft && productIdsToStrip.length) {
      for (let i = 0; i < productIdsToStrip.length; i += 1) {
        const productId = productIdsToStrip[i];
        let paths = findObjectPaths(exportedProductDraft, 'id', productId);

        while (paths.length > 0) {
          const path = paths.shift();

          if (path.includes(`${objectPathDelimiter}value${objectPathDelimiter}`) && (path.includes('masterVariant') || path.includes('variants'))) {
            const pathStack = path.split(objectPathDelimiter);

            // Remove root (begining) element and .value (final subpath) element
            pathStack.pop();
            pathStack.shift();

            let traversePoint = exportedProductDraft;

            for (let k = 0; k < pathStack.length; k += 1) {
              traversePoint = traversePoint[pathStack[k]];

              if (k === pathStack.length - 2 && Array.isArray(traversePoint) && isNumeric(pathStack[pathStack.length - 1])) {
                traversePoint.splice(parseInt(pathStack[pathStack.length - 1], 10), 1);
                break;
              }
            }
            paths = findObjectPaths(exportedProductDraft, 'id', productId);
          }
        }
      }
    }
  };

  service.importProducts = async (exportedProductDrafts = []) => {
    await dataCache.cacheStaticData();

    for (let i = 0; i < exportedProductDrafts.length; i += 1) {
      const exportedProductDraft = exportedProductDrafts[i];
      await importProduct(exportedProductDraft);
    }

    await retryProductsWithMissingReferences();
  };

  return service;
};

export default ProductImport;
