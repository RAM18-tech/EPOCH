/* eslint-disable import/prefer-default-export */
import { handleError } from '../commercetools/errors';

export const ProductTypeService = ({ commercetools }) => {
  const productTypeService = {};
  const { client, getRequestBuilder } = commercetools;

  const NON_PRODUCT_PRODUCTTYPES_NAMES = ['offer',
    'offerAgreement',
    'offerCompliance',
    'offerEligibility',
    'offerProduct',
    'offerProductSelection',
    'offerRepresentative',
    'removalRule',
    'simpleMap',
  ];

  // TODO: Implement cache refresher endpoint
  productTypeService.keysToIds = {};
  productTypeService.idsToKeys = {};

  /**
     * Retrieves all productTypes which are not used for actual AT&T products
     */
  productTypeService.getNonProductProductTypes = async (headers = false) => {
    const requestBuilder = getRequestBuilder();
    const query = [buildProductTypeFilterQuery()];
    const uri = requestBuilder
      .productTypes
      .parse({ where: query })
      .perPage(300)
      .build();
    const request = { uri, method: 'GET' };
    if (headers && headers.authorization) {
      request.headers = { Authorization: headers.authorization };
    }
    try {
      const ctpResult = await client.execute(request);
      return { results: ctpResult.body.results, timings: ctpResult.headers['x-ctp-timing'] };
    } catch (e) {
      return handleError(e);
    }
  };

  const buildProductTypeFilterQuery = () => `name in ("${NON_PRODUCT_PRODUCTTYPES_NAMES.join('","')}")`;

  /**
     * Caches all productType keys to ids in the keysToIds map.
     */
  productTypeService.cacheKeysToIds = async (headers = false) => {
    const requestBuilder = getRequestBuilder();

    const request = {
      uri: requestBuilder.productTypes.perPage(300).build(),
      method: 'GET',
    };
    if (headers && headers.authorization) {
      request.headers = { Authorization: headers.authorization };
    }

    try {
      const ctpResult = await client.execute(request);
      cacheKeysToIds(ctpResult.body.results);
      return { results: ctpResult.body.results, timings: [ctpResult.headers['x-ctp-timing']] };
    } catch (e) {
      return handleError(e);
    }
  };

  /**
     * Retrieves the offer productTypeId.
     * First attempts to find it in the cached keysToIds map, otherwise makes a request to
     * fetch it from CTP.
     *
     */
  productTypeService.getOfferProductFamilyId = async (headers = false) => {
    const offerProductFamilyKey = 'offer';

    const offerProductFamilyId = productTypeService.keysToIds[offerProductFamilyKey];
    if (offerProductFamilyId) {
      return { results: [offerProductFamilyId], timings: [] };
    }

    const requestBuilder = getRequestBuilder();

    const request = {
      uri: requestBuilder.productTypes.parse({ key: offerProductFamilyKey }).build(),
      method: 'GET',
    };
    if (headers && headers.authorization) {
      request.headers = { Authorization: headers.authorization };
    }
    try {
      const ctpResult = await client.execute(request);
      productTypeService.keysToIds[ctpResult.body.key] = ctpResult.body.id;
      productTypeService.idsToKeys[ctpResult.body.id] = ctpResult.body.key;
      return { results: ctpResult.body };
    } catch (e) {
      return handleError(e);
    }
  };

  const cacheKeysToIds = productTypes => productTypes.forEach(productType => {
    productTypeService.keysToIds[productType.key] = productType.id;
    productTypeService.idsToKeys[productType.id] = productType.key;
  });


  return productTypeService;
};
