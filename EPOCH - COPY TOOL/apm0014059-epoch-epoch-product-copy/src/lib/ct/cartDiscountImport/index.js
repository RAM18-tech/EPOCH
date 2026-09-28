import '@babel/polyfill';
import _ from 'lodash';
import { DataCache } from '../dataCache';
import { CustomObjectsService } from '../customObjects';
import { findObjectsByProperty, extractGuids } from '../../utils';
import { Commercetools } from '../commercetools';
import { DiscountsService } from '../discounts';

export const CartDiscountImport = ({ apiSourceInfo, apiDestinationInfo }) => {
  const service = {};
  const dataCache = DataCache({ apiSourceInfo, apiDestinationInfo });
  const destCustomObjectsService = CustomObjectsService({ commercetools: Commercetools(apiDestinationInfo), sleepLength: 1500 });
  const destDiscountService = DiscountsService({ commercetools: Commercetools(apiDestinationInfo), sleepLength: 1500 });

  const importDiscount = async (exportedDiscount) => {
    console.log(`Importing cart-discount: ${exportedDiscount.key}`);

    reconcileTypeIdReference(exportedDiscount);
    await reconcileDiscountProductIdReferences(exportedDiscount);
    await reconcileCustomObjectIdReferences(exportedDiscount);
    await reconcilePredicateReferences(exportedDiscount);
    await destDiscountService.createOrUpdate({ discount: exportedDiscount });
  };

  const reconcileDiscountProductIdReferences = async (exportedDiscount) => {
    await reconcileProductIdReferences(exportedDiscount);
  };

  const reconcileProductIdReferences = async (ctObject) => {
    const nestedProductIdsObjects = findObjectsByProperty(ctObject, 'typeId', 'product');

    for (let i = 0; i < nestedProductIdsObjects.length; i += 1) {
      const nestedProductIdsObject = nestedProductIdsObjects[i];
      const sourceProductId = nestedProductIdsObject.id;
      const sourceCacheProduct = await dataCache.sourceProjectCache.products.get(sourceProductId);

      if (sourceCacheProduct) {
        const sourceProductKey = sourceCacheProduct.key;

        if (sourceProductKey) {
          const destProduct = await dataCache.destinationProjectCache.products.getByKey(sourceProductKey);

          if (destProduct) {
            nestedProductIdsObject.id = destProduct.id;
          }
        }
      }
    }
  };

  const reconcileTypeIdReference = (exportedDiscount) => {
    if (exportedDiscount.custom && exportedDiscount.custom.type && exportedDiscount.custom.type.id) {
      const sourceCustomTypeId = exportedDiscount.custom.type.id;

      if (dataCache.sourceProjectCache.types[sourceCustomTypeId]) {
        const sourceTypeKey = dataCache.sourceProjectCache.types[sourceCustomTypeId].key;

        if (sourceTypeKey) {
          const destType = Object.values(dataCache.destinationProjectCache.types).find(type => type.key === sourceTypeKey);

          if (destType) {
            exportedDiscount.custom.type.id = destType.id;
          }
        }
      }
    }
  };

  const reconcileCustomObjectIdReferences = async (exportedDiscount) => {
    const sourceCustomObjectsRefs = findObjectsByProperty(exportedDiscount, 'typeId', 'key-value-document');

    if (sourceCustomObjectsRefs.length) {
      for (let i = 0; i < sourceCustomObjectsRefs.length; i += 1) {
        const customObjectReferenceDraft = sourceCustomObjectsRefs[i];

        const sourceCustomObject = customObjectReferenceDraft.id
          && await dataCache.sourceProjectCache.customObjects.get(customObjectReferenceDraft.id);

        if (sourceCustomObject) {
          const sourceKey = sourceCustomObject.key;

          let destCustomObject = await dataCache.destinationProjectCache.customObjects.getByKey(sourceCustomObject.container, sourceKey);

          if (destCustomObject) {
            const { id } = destCustomObject;
            destCustomObject = _.cloneDeep(sourceCustomObject);
            destCustomObject.id = id;
          } else {
            destCustomObject = _.cloneDeep(sourceCustomObject);
            delete destCustomObject.id;
          }

          await reconcileProductIdReferences(destCustomObject);
          await destCustomObjectsService.createOrUpdate({ customObject: destCustomObject });

          destCustomObject = await dataCache.destinationProjectCache.customObjects.recacheByKey(sourceCustomObject.container, sourceKey);

          if (destCustomObject) {
            customObjectReferenceDraft.id = destCustomObject.id;
          }
        }
      }
    }
  };

  const reconcilePredicateReferences = async (cartDiscount) => {
    if (cartDiscount && cartDiscount.target && cartDiscount.target.predicate) {
      cartDiscount.target.predicate = await reconcileIdsOnPredicate(cartDiscount.target.predicate, 'product');
      cartDiscount.target.predicate = await reconcileIdsOnPredicate(cartDiscount.target.predicate, 'custom-type');
    }

    if (cartDiscount && cartDiscount.cartPredicate) {
      cartDiscount.predicate = await reconcileIdsOnPredicate(cartDiscount.cartPredicate, 'product');
      cartDiscount.predicate = await reconcileIdsOnPredicate(cartDiscount.cartPredicate, 'custom-type');
    }
  };

  const reconcileIdsOnPredicate = async (predicate, idTypeToReconcile = 'product') => {
    let finalPredicate = predicate;

    const extractIdConditions = (string, idType) => {
      let idPattern = /product.id (=|!=|not\sin|in)? \(?".*?"(,?".*?")*\)?/g;

      if (idType === 'custom-type') {
        idPattern = /custom.type.id (=|!=|not\sin|in)? \(?".*?"(,?".*?")*\)?/g;
      }

      let currentPattern;
      const conditions = [];

      while (currentPattern = idPattern.exec(string)) {
        conditions.push(currentPattern.shift());
      }
      return conditions;
    };

    if (predicate) {
      const idConditions = extractIdConditions(predicate, idTypeToReconcile);

      for (let i = 0; i < idConditions.length; i += 1) {
        const idCondition = idConditions[i];
        const extractedIds = extractGuids(idCondition);

        for (let j = 0; j < extractedIds.length; j += 1) {
          const extractedId = extractedIds[j];

          if (idTypeToReconcile === 'product' && extractedId) {
            const sourceProduct = await dataCache.sourceProjectCache.products.get(extractedId);

            if (sourceProduct && sourceProduct.key) {
              const destProduct = await dataCache.destinationProjectCache.products.getByKey(sourceProduct.key);

              if (destProduct && destProduct.id) {
                const productIdReplaceRegex = new RegExp(`\\b${extractedId}\\b`, 'gi');
                finalPredicate = finalPredicate.replace(productIdReplaceRegex, destProduct.id);
              }
            }
          } else if (idTypeToReconcile === 'custom-type' && extractedId) {
            const sourceCustomType = dataCache.sourceProjectCache.types[extractedId];

            if (sourceCustomType && sourceCustomType.key) {
              const destType = Object.values(dataCache.destinationProjectCache.types).find(type => type.key === sourceCustomType.key);

              if (destType && destType.id) {
                const typeIdReplaceRegex = new RegExp(`\\b${extractedId}\\b`, 'gi');
                finalPredicate = finalPredicate.replace(typeIdReplaceRegex, destType.id);
              }
            }
          }
        }
      }
    }
    return finalPredicate;
  };

  service.importDiscounts = async (exportedDiscounts = []) => {
    await dataCache.cacheStaticData();

    for (let i = 0; i < exportedDiscounts.length; i += 1) {
      const exportedCartDiscount = exportedDiscounts[i];
      await importDiscount(exportedCartDiscount);
    }
  };

  return service;
};

export default CartDiscountImport;
