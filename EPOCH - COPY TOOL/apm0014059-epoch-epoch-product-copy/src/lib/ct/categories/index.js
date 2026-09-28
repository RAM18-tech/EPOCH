import { handleError } from '../commercetools/errors';

export const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

export const CategoriesService = ({ commercetools, sleepLength }) => {
  const categoryService = {};

  const { client, getRequestBuilder } = commercetools;

  categoryService.fetchAll = async ({ where = [] }) => {
    let page = 1;
    let allResults = [];
    let newResults = await categoryService.fetchPage({ page, where });
    while (newResults.length) {
      allResults = [...allResults, ...newResults];
      page += 1;
      // eslint-disable-next-line no-await-in-loop
      newResults = await categoryService.fetchPage({ page, where });
    }
    return allResults;
  };


  categoryService.fetchPage = async ({ page = 1, perPage = 200, where = [] }) => {
    const requestBuilder = getRequestBuilder();
    try {
      const options = { perPage, page };
      if (where && where.length) {
        options.where = where;
      }
      const result = await client
        .execute({
          uri: requestBuilder.categories.parse(options).build(),
          method: 'GET',
        });
      await sleep(sleepLength);
      return result.body.results;
    } catch (err) {
      handleError(err);
    }
  };

  return categoryService;
};
export default CategoriesService;
