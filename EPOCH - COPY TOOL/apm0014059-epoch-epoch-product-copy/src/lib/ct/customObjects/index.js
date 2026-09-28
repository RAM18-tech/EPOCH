import { NotFoundError } from '../commercetools/errors';

export const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

export const CustomObjectsService = ({ commercetools, sleepLength }) => {
  const service = {};
  const { client, getRequestBuilder } = commercetools;

  /**
   * Fetches a certain page of results given a certain where clause
   */
  service.fetch = async ({
    where = [], page = 1, perPage = 200, resourceTypeId = 'customObjects', sort = [{ by: 'id', direction: 'asc' }],
  }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const results = await client.execute({
        uri: requestBuilder[resourceTypeId].parse({
          where,
          page,
          perPage,
          sort,
        }).withTotal(false).build(),
        method: 'GET',
      });
      await sleep(sleepLength);
      return results.body.results;
    } catch (err) {
      if (!(err instanceof NotFoundError)) {
        throw err;
      }
    }
  };

  service.fetchAll = async ({
    resourceTypeId = 'customObjects',
    sort = [{ by: 'id', direction: 'asc' }],
    where = [],
  }) => {
    let allResults = [];
    let page = 1;

    let results = await service.fetch({
      where, page, resourceTypeId, sort,
    });
    while (results.length) {
      allResults = [...allResults, ...results];

      page += 1;

      results = await service.fetch({
        where, page, resourceTypeId, sort,
      });
    }
    return allResults;
  };

  service.byId = async ({ id, resourceTypeId = 'customObjects' }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder[resourceTypeId].parse({ id }).build(),
        method: 'GET',
      });

      await sleep(sleepLength);
      return result.body;
    } catch (err) {
      if (!(err instanceof NotFoundError)) {
        throw err;
      }
    }
  };

  service.byKey = async ({ container, key, resourceTypeId = 'customObjects' }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const result = await client.execute({
        uri: requestBuilder[resourceTypeId].parse({
          where: [`key = "${key}"`, `container = "${container}"`],
          whereOperator: 'and',
        }).build(),
        method: 'GET',
      });

      await sleep(sleepLength);

      if (result.body && result.body.results && result.body.results.length) {
        return result.body.results.find(u => u);
      }
    } catch (err) {
      if (!(err instanceof NotFoundError)) {
        throw err;
      }
    }
  };

  service.createOrUpdate = async ({ customObject, resourceTypeId = 'customObjects' }) => {
    if (customObject) {
      delete customObject.version;
      const actionType = customObject.id ? 'update' : 'create';

      try {
        const requestBuilder = getRequestBuilder();

        const result = await client.execute({
          uri: requestBuilder[resourceTypeId].build(),
          method: 'POST',
          body: JSON.stringify(customObject),
        });
        await sleep(sleepLength);

        console.info(`${actionType} customObject result ${JSON.stringify(result)}`);

        return result.body;
      } catch (err) {
        console.warn(JSON.stringify(customObject));
        console.error(err);
        console.info(`Error in ${actionType} custom object: `, err.body.errors, customObject.id, customObject.key);
      }
    }
  };

  return service;
};

export default CustomObjectsService;
