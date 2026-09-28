import { handleError } from '../commercetools/errors';

export const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

export const StatesService = ({ commercetools, sleepLength }) => {
  const stateService = {};

  const { client, getRequestBuilder } = commercetools;

  stateService.fetchAll = async ({ where = [] }) => {
    let page = 1;
    let allResults = [];
    let newResults = await stateService.fetchPage({ page, where });
    while (newResults.length) {
      allResults = [...allResults, ...newResults];
      page += 1;
      // eslint-disable-next-line no-await-in-loop
      newResults = await stateService.fetchPage({ page, where });
    }
    return allResults;
  };


  stateService.fetchPage = async ({ page = 1, perPage = 200, where = [] }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const options = { perPage, page };
      if (where && where.length) {
        options.where = where;
      }
      const result = await client
        .execute({
          uri: requestBuilder.states.parse(options).build(),
          method: 'GET',
        });
      await sleep(sleepLength);
      return result.body.results;
    } catch (err) {
      handleError(err);
    }
  };

  /**
   * Retrieves a state by key
   */
  stateService.byKey = async ({ key }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder.states.parse({ key }).build(),
        method: 'GET',
      });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      handleError(err);
    }
  };

  return stateService;
};
export default StatesService;
