import { handleError } from '../commercetools/errors';

export const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

export const CustomerGroupService = ({ commercetools }) => {
  const customerGroupService = {};
  customerGroupService.keysToIds = {};

  const { client, getRequestBuilder } = commercetools;

  /**
   * Caches all customerGroups keys to ids in the keysToIds map.
   */
  customerGroupService.cacheKeysToIds = async (headers = false) => {
    const requestBuilder = getRequestBuilder();

    const request = {
      uri: requestBuilder.customerGroups.build(),
      method: 'GET',
    };
    if (headers && headers.authorization) {
      request.headers = { Authorization: headers.authorization };
    }
    return client
      .execute(request)
      .then(res => { cacheKeysToIds(res.body.results); return { results: res.body.results }; })
      .catch(handleError);
  };

  customerGroupService.getAll = async (headers = false) => {
    const requestBuilder = getRequestBuilder();

    const request = {
      uri: requestBuilder.customerGroups.build(),
      method: 'GET',
    };
    if (headers && headers.authorization) {
      request.headers = { Authorization: headers.authorization };
    }
    const res = await client.execute(request);
    return res.body.results;
  };

  const cacheKeysToIds = customerGroups => customerGroups.forEach(customerGroup => {
    customerGroupService.keysToIds[customerGroup.key] = customerGroup.id;
  });

  return customerGroupService;
};

export default CustomerGroupService;
