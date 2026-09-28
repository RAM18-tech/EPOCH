import { cloneDeep, isEqual } from 'lodash';
import { createSyncDiscountCodes, createSyncCartDiscounts } from '@commercetools/sync-actions';
import { handleError, NotFoundError } from '../commercetools/errors';

const syncDiscountCodes = createSyncDiscountCodes();
const syncCartDiscounts = createSyncCartDiscounts();

export const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

export const DiscountsService = ({ commercetools, sleepLength }) => {
  const discountsService = {};
  const { client, getRequestBuilder } = commercetools;

  /**
   * Retrieves a discount by key
   */
  discountsService.byKey = async ({ key, resourceTypeId = 'cartDiscounts' }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder[resourceTypeId].parse({ where: [`key="${key}"`] }).build(),
        method: 'GET',
      });
      await sleep(sleepLength);
      const [firstResult = false] = result.body.results;
      return firstResult;
    } catch (err) {
      handleError(err);
    }
  };

  discountsService.getCode = async (code) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder.discountCodes.parse({ where: [`code="${code}"`] }).build(),
        method: 'GET',
      });
      await sleep(sleepLength);
      return result.body.results[0];
    } catch (err) {
      handleError(err);
    }
  };

  /**
   * Retrieves a discount by id
   */
  discountsService.byId = async ({ id, resourceTypeId = 'cartDiscounts' }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder[resourceTypeId].parse({ id }).build(),
        method: 'GET',
      });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      console.warn(`Error fetching ${resourceTypeId} id: ${id}`);
      console.info(err);
    }
  };

  /**
   * Fetches a certain page of results given a certain where clause
   */
  discountsService.fetch = async ({
    where = [], page = 1, perPage = 200, resourceTypeId = 'cartDiscounts', sort = [{ by: 'createdAt', direction: 'desc' }],
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

  /**
   * Given a set of actions, updates a discount
   */
  discountsService.update = async ({ discount, actions, resourceTypeId = 'cartDiscounts' }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder[resourceTypeId].parse({ id: discount.id }).build(),
        method: 'POST',
        body: JSON.stringify({
          version: discount.version,
          actions,
        }),
      });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      console.warn(JSON.stringify(discount));
      console.info('Error updating discounts: ', err.body.errors, discount.id, discount.key);
    }
  };

  /**
   * Given a discount draft, creates a discount
   */
  discountsService.create = async ({ discount, resourceTypeId = 'cartDiscounts' }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder[resourceTypeId].build(),
        method: 'POST',
        body: discount,
      });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      console.warn(JSON.stringify(discount));
      console.info(err);
    }
  };

  /**
   * Creates a new discount or updates an existing discount.
   */
  discountsService.createOrUpdate = async ({
    discount, resourceTypeId = 'cartDiscounts', existingDiscount = {}, createCode = false, skipFetch = false,
  }) => {
    // look up by key
    let ourExistingDiscount = (existingDiscount) ? cloneDeep(existingDiscount) : {};
    if (!skipFetch && !ourExistingDiscount.id) {
      try {
        ourExistingDiscount = await discountsService.byKey({ key: discount.key, resourceTypeId });
      } catch (e) {
        // disregard any 404, as it can be expected.
        if (!(e instanceof NotFoundError)) {
          throw e;
        }
      }
    }
    if (ourExistingDiscount && ourExistingDiscount.id) {
      let actions = [];
      switch (resourceTypeId) {
        default:
        case 'cartDiscounts':
          actions = syncCartDiscounts.buildActions(discount, ourExistingDiscount).map(action => {
            // fix weird issue where setCustomType was trying to use id: instead of key:
            if (action.action === 'setCustomType') {
              // see if we even need this update action.
              if (ourExistingDiscount.custom) {
                if (isEqual(discount.custom.fields, ourExistingDiscount.custom.fields)) {
                  return false;
                }
              }
              return { ...action, ...discount.custom };
            }
            if (action.action === 'changeValue') {
              if (isEqual(action.value, ourExistingDiscount.value)) {
                return false;
              }
            }
            return action;
          }).filter(x => x);
          break;
        case 'discountCodes':
          actions = syncDiscountCodes.buildActions(discount, ourExistingDiscount);
          break;
      }
      if (!actions.length) {
        return ourExistingDiscount;
      }
      console.info(`Updating ${discount.key}`, JSON.stringify(actions), ourExistingDiscount.id);
      return discountsService.update({ discount: ourExistingDiscount, actions, resourceTypeId });
    }
    console.info(`Creating ${discount.key}`);
    const result = await discountsService.create({ discount, resourceTypeId });
    if (!result || !result.id) {
      return false;
    }
    if (createCode && resourceTypeId === 'cartDiscounts') {
      await discountsService.createBlankDiscountCode({
        code: discount.key,
        description: { en: 'migrated' },
        cartDiscounts: [{ typeId: 'cart-discount', id: result.id }],
      });
    }
    return result;
  };

  /**
   * Returns a map of discounts, using their key to key them
   */
  discountsService.fetchAllKeys = async ({ keys, resourceTypeId = 'cartDiscounts' }) => {
    const promoMap = {};
    const perPage = 200;
    for (let i = 0; i < keys.length; i += perPage) {
      const keyChunk = keys.slice(i, i + perPage);
      const where = [`key in (${keyChunk.map(x => `"${x}"`).join(',')})`];
      // eslint-disable-next-line no-await-in-loop
      const existingPromotions = await discountsService.fetch({
        perPage, where, page: 1, resourceTypeId,
      });
      existingPromotions.forEach(promo => {
        promoMap[promo.custom.fields.key] = promo;
        promoMap[promo.key] = promo;
      });
    }
    return promoMap;
  };


  discountsService.fetchAll = async ({
    resourceTypeId = 'cartDiscounts',
    sort = [{ by: 'createdAt', direction: 'desc' }],
    where = [],
  }) => {
    let allResults = [];
    let page = 1;

    let results = await discountsService.fetch({
      where, page, resourceTypeId, sort,
    });
    while (results.length) {
      allResults = [...allResults, ...results];
      page += 1;
      // eslint-disable-next-line no-await-in-loop
      results = await discountsService.fetch({
        where, page, resourceTypeId, sort,
      });
    }
    return allResults;
  };


  discountsService.delete = async (discount, resourceTypeId) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: `${requestBuilder[resourceTypeId].parse({ id: discount.id }).build()}?version=${discount.version}`,
        method: 'DELETE',
      });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      console.error(JSON.stringify(err.body));
    }
  };

  discountsService.createBlankDiscountCode = async ({ code, cartDiscounts = [] }) => {
    const blankDiscountCodeDraft = {
      isActive: true,
    };
    return discountsService.create({
      discount: {
        ...blankDiscountCodeDraft,
        code,
        cartDiscounts,
      },
      resourceTypeId: 'discountCodes',
      skipFetch: true,
    });
  };

  discountsService.createBlankDiscountWithCode = async (promotion, sortOrder = false) => {
    let sortOrderToSend = sortOrder;
    if (!sortOrder) {
      sortOrderToSend = Math.random();
    }
    const blankDiscountDraft = {
      name: { en: 'placeholder' },
      value: {
        type: 'relative',
        permyriad: 0,
      },
      sortOrder: `${sortOrderToSend}`,
      isActive: false,
      requiresDiscountCode: true,
      cartPredicate: '1=1',
    };
    const blankDiscount = await discountsService.create({
      discount: {
        ...blankDiscountDraft,
        key: promotion.promotion_code,
      },
      resourceTypeId: 'cartDiscounts',
    });
    discountsService.createBlankDiscountCode({
      code: promotion.promotion_code,
      cartDiscounts: [{ typeId: 'cart-discount', id: blankDiscount.id }],
    });
    return blankDiscount;
  };

  return discountsService;
};
export default DiscountsService;
