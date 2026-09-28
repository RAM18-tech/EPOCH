import { handleError } from '../commercetools/errors';

export const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

export const ChannelsService = ({ commercetools, sleepLength = 1000 }) => {
  const channelService = {};

  const { client, getRequestBuilder } = commercetools;

  /**
   * Retrieves a product by key
   */
  channelService.byKey = async ({ key }) => {
    const requestBuilder = await getRequestBuilder();
    try {
      const result = client
        .execute({
          uri: requestBuilder.products.parse({ key }).build(),
          method: 'GET',
        });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      handleError(err);
    }
  };

  /**
   * Retrieves a product by id
   */
  channelService.byId = async ({ id }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client
        .execute({
          uri: requestBuilder.products.parse({ id }).build(),
          method: 'GET',
        });
      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      handleError(err);
    }
  };

  channelService.fetchAll = async () => {
    let page = 1;
    let allResults = [];
    let newResults = await channelService.fetchPage(page);
    while (newResults.length) {
      allResults = [...allResults, ...newResults];
      page += 1;
      // eslint-disable-next-line no-await-in-loop
      newResults = await channelService.fetchPage(page);
    }
    return allResults;
  };


  channelService.fetchPage = async page => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client
        .execute({
          uri: requestBuilder.channels.parse({ perPage: 200, page }).build(),
          method: 'GET',
        });
      await sleep(sleepLength);
      return result.body.results;
    } catch (err) {
      handleError(err);
    }
  };

  return channelService;
};

export default ChannelsService;
