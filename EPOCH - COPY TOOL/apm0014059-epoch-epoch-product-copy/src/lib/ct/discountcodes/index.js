import { cloneDeep, isEqual } from 'lodash';
import { createSyncDiscountCodes } from '@commercetools/sync-actions';
import { handleError, NotFoundError } from '../commercetools/errors';

const syncDiscountCodes = createSyncDiscountCodes();

export const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

export const DiscountCodesService = ({ commercetools, sleepLength }) => {
  const discountCodesService = {};

  const { client, getRequestBuilder } = commercetools;

  discountCodesService.getByCode = async (code, headers = false) => {
    const requestBuilder = getRequestBuilder();
    const request = {
      uri: requestBuilder.discountCodes.parse({ where: [`code="${code}"`] }).build(),
      method: 'GET',
    };
    if (headers && headers.authorization) {
      request.headers = { Authorization: headers.authorization };
    }
    try {
      const result = await client.execute(request);
      await sleep(sleepLength);
      return result.body.results[0];
    } catch (err) {
      handleError(err);
    }
  };

  /**
   * Creates a new discount or updates an existing discount.
   */
  discountCodesService.updateActive = async ({
    discount, resourceTypeId = 'cartDiscounts', headers, discountsService,
  }) => {
    // look up by key
    const existingDiscount = (discount) ? cloneDeep(discount) : {};
    let actions = [];
    if (discount.validFrom && discount.validUntil) {
      const today = new Date();
      const from = new Date(discount.validFrom);
      const until = new Date(discount.validUntil);


      if ((today > from) && (today < until)) {
        discount.isActive = true;
      } else {
        discount.isActive = false;
      }
    }

    actions = syncDiscountCodes.buildActions(discount, existingDiscount).map(action => {
      // fix weird issue where setCustomType was trying to use id: instead of key:
      if (action.action === 'setCustomType') {
        // see if we even need this update action.
        if (existingDiscount.custom) {
          if (isEqual(discount.custom.fields, existingDiscount.custom.fields)) {
            return false;
          }
        }
        return { ...action, ...discount.custom };
      }
      if (action.action === 'changeValue') {
        if (isEqual(action.value, existingDiscount.value)) {
          return false;
        }
      }
      return action;
    }).filter(x => x);

    if (!actions.length) {
      return existingDiscount;
    }

    return discountsService.update({
      discount: existingDiscount, actions, resourceTypeId, headers,
    });
  };


  /**
   * Creates a new discountCode or updates an existing discountCode.
   */
  discountCodesService.createOrUpdate = async ({
    discount, resourceTypeId = 'discountCodes', existingDiscount = {}, skipFetch = false, headers = false, discountsService,
  }) => {
    // look up by key
    let ourExistingDiscount = (existingDiscount) ? cloneDeep(existingDiscount) : {};
    if (!skipFetch && !ourExistingDiscount.id) {
      try {
        ourExistingDiscount = await discountCodesService.getByCode(discount.code, headers);
      } catch (e) {
        // disregard any 404, as it can be expected.
        if (!(e instanceof NotFoundError)) {
          throw e;
        }
      }
    }
    if (ourExistingDiscount && ourExistingDiscount.id) {
      let actions = [];
      // actions = syncDiscountCodes.buildActions(discount, ourExistingDiscount);
      // lord change start
      actions = syncDiscountCodes.buildActions(discount, ourExistingDiscount).map(action => {
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

      // lord change end
      if (!actions.length) {
        return ourExistingDiscount;
      }
      console.info(`Updating ${discount.code}`, JSON.stringify(actions), ourExistingDiscount.id);
      return discountCodesService.update({
        discount: ourExistingDiscount, actions, resourceTypeId, headers, discountsService,
      });
    }
    console.info(`Creating ${discount.code}`);
    const result = await discountCodesService.create({
      discount, resourceTypeId, headers, discountsService,
    });
    return result;
  };

  /**
   * Given a discount draft, creates a discountCode
   */
  discountCodesService.create = async ({
    discount, resourceTypeId = 'discountCodes', headers = false, discountsService,
  }) => {
    const requestBuilder = getRequestBuilder();
    const request = {
      uri: requestBuilder[resourceTypeId].build(),
      method: 'POST',
      body: discount,
    };
    if (headers && headers.authorization) {
      request.headers = { Authorization: headers.authorization };
    }
    try {
      const result = await client.execute(request);
      await sleep(sleepLength);
      const cartDis = result.body.cartDiscounts[0];
      const { id } = cartDis;
      const existingCart = await discountsService.byId({ id, headers, resourceTypeId: 'cartDiscounts' });
      await discountCodesService.updateActive({
        discount: existingCart, resourceTypeId: 'cartDiscounts', headers, discountsService,
      });
      return result.body;
    } catch (err) {
      let errorBody = err.body;
      if (!(discount.cartDiscounts.length > 0)) {
        errorBody = errorNotFoundCartDiscount(err);
      }
      console.warn(JSON.stringify(discount));
      console.info(err);
      return errorBody;
    }
  };

  /**
   * Given a set of actions, updates a discountCode
   */
  discountCodesService.update = async ({
    discount, actions, resourceTypeId = 'discountCodes', headers = false, discountsService,
  }) => {
    const requestBuilder = getRequestBuilder();
    const request = {
      uri: requestBuilder[resourceTypeId].parse({ id: discount.id }).build(),
      method: 'POST',
      body: JSON.stringify({
        version: discount.version,
        actions,
      }),
    };
    if (headers && headers.authorization) {
      request.headers = { Authorization: headers.authorization };
    }
    try {
      const result = await client.execute(request);
      await sleep(sleepLength);
      const cartDis = result.body.cartDiscounts[0];
      const { id } = cartDis;
      const existingCart = await discountsService.byId({ id, headers, resourceTypeId: 'cartDiscounts' });
      await discountCodesService.updateActive({
        discount: existingCart, resourceTypeId: 'cartDiscounts', headers, discountsService,
      });
      return result.body;
    } catch (err) {
      let errorBody = err.body;
      if (!(discount.cartDiscounts.length > 0)) {
        errorBody = errorNotFoundCartDiscount(err);
      }
      console.warn(JSON.stringify(discount));
      console.info('Error updating discountCode: ', err.body.errors, discount.id, discount.key);
      return errorBody;
    }
  };

  function errorNotFoundCartDiscount(err) {
    err.body.statusCode = 404;
    err.body.message = 'Promo not found';
    err.body.errors = [
      {
        code: '404',
        message: 'Promo not found',
      },
    ];
    return err.body;
  }


  return discountCodesService;
};
export default DiscountCodesService;
