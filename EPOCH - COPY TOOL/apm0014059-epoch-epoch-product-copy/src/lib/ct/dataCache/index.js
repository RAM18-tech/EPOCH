import '@babel/polyfill';
import { Commercetools } from '../commercetools';
import { ProductsService } from '../products';
import { ChannelsService } from '../channels';
import { StatesService } from '../states';
import { TypesService } from '../types';
import { ProductTypeService } from '../productTypes';
import { CustomerGroupService } from '../customerGroup';
import { toObjectMapById } from '../../utils';
import { CustomObjectsService } from '../customObjects';

export const DataCache = ({ apiSourceInfo, apiDestinationInfo }) => {
  const service = {};

  const ctApiSource = Commercetools(apiSourceInfo);
  const ctApiDestination = Commercetools(apiDestinationInfo);

  const sourceProductsService = ProductsService({ commercetools: ctApiSource, sleepLength: 1500 });
  const sourceChannelService = ChannelsService({ commercetools: ctApiSource, sleepLength: 1500 });
  const sourceStateService = StatesService({ commercetools: ctApiSource, sleepLength: 1500 });
  const sourceTypesService = TypesService({ commercetools: ctApiSource, sleepLength: 1500 });
  const sourceProductTypeService = ProductTypeService({ commercetools: ctApiSource });
  const sourceCustomerGroupService = CustomerGroupService({ commercetools: ctApiSource });
  const sourceCustomObjectsService = CustomObjectsService({ commercetools: ctApiSource, sleepLength: 1500 });

  const destProductsService = ProductsService({ commercetools: ctApiDestination, sleepLength: 1500 });
  const destChannelService = ChannelsService({ commercetools: ctApiDestination, sleepLength: 1500 });
  const destStateService = StatesService({ commercetools: ctApiDestination, sleepLength: 1500 });
  const destTypesService = TypesService({ commercetools: ctApiDestination, sleepLength: 1500 });
  const destProductTypeService = ProductTypeService({ commercetools: ctApiDestination });
  const destCustomerGroupService = CustomerGroupService({ commercetools: ctApiDestination });
  const destCustomObjectsService = CustomObjectsService({ commercetools: ctApiDestination, sleepLength: 1500 });

  const newCache = ({ productsService, customObjectService }) => {
    const cache = {};

    cache.products = {
      internalCache: {},
      add: async (id) => {
        try {
          const res = await productsService.byId({ id });
          cache.products.internalCache[id] = res;
          return res;
        } catch (error) {
          console.error(`Couldnt add to product cache: id=${id}`);
        }
      },
      get: async (id) => cache.products.internalCache[id] || cache.products.add(id),
      getByKey: async (key) => {
        let product = Object.values(cache.products.internalCache).find(obj => obj.key === key);

        if (!product) {
          try {
            product = await productsService.byKey({ key });

            if (product) {
              cache.products.internalCache[product.id] = product;
            }
          } catch (error) {
            console.error(`Couldnt add to product cache: key=${key}`);
          }
        }
        return product;
      },
      recacheByKey: async (key) => {
        try {
          const product = await productsService.byKey({ key });

          if (product) {
            cache.products.internalCache[product.id] = product;
            return product;
          }
        } catch (error) {
          console.error(`Couldnt add to product cache: key=${key}`);
        }
      },
      clear: () => {
        cache.products.internalCache = {};
      },
    };

    cache.customObjects = {
      internalCache: {},
      add: async (id) => {
        try {
          const res = await customObjectService.byId({ id });
          cache.customObjects.internalCache[id] = res;
          return res;
        } catch (error) {
          console.error(`Couldnt add to customObject cache: id=${id}`);
        }
      },
      get: async (id) => cache.customObjects.internalCache[id] || cache.customObjects.add(id),
      getByKey: async (container, key) => {
        let customObject = Object.values(cache.customObjects.internalCache).find(obj => obj.key === key && obj.container === container);

        if (!customObject) {
          try {
            customObject = await customObjectService.byKey({ container, key });

            if (customObject) {
              cache.customObjects.internalCache[customObject.id] = customObject;
            }
          } catch (error) {
            console.error(`Couldnt add to customObject cache: key=${key}`);
          }
        }
        return customObject;
      },
      recacheByKey: async (container, key) => {
        try {
          const customObject = await customObjectService.byKey({ container, key });

          if (customObject) {
            cache.customObjects.internalCache[customObject.id] = customObject;
          }
          return customObject;
        } catch (error) {
          console.error(`Couldnt add to customObject cache: key=${key}`);
        }
      },
      clear: () => {
        cache.customObjects.internalCache = {};
      },
    };

    // Following caches are static (can cache all at once), no need for special functions/re-caching
    cache.channels = {};
    cache.states = {};
    cache.types = {};
    cache.productTypes = {};
    cache.customerGroups = {};

    return cache;
  };

  service.sourceProjectCache = newCache({
    productsService: sourceProductsService,
    customObjectService: sourceCustomObjectsService,
  });

  service.destinationProjectCache = newCache({
    productsService: destProductsService,
    customObjectService: destCustomObjectsService,
  });

  const cacheDestinationData = async () => {
    console.log('Caching destination data');

    const cacheChannels = async () => {
      const destChannels = [];
      destChannels.push(await destChannelService.fetchAll());
      service.destinationProjectCache.channels = toObjectMapById(destChannels);
    };

    const cacheStates = async () => {
      const destStates = [];
      destStates.push(await destStateService.fetchAll({ where: [] }));
      service.destinationProjectCache.states = toObjectMapById(destStates);
    };

    const cacheTypes = async () => {
      const destTypes = [];
      destTypes.push(await destTypesService.fetchAll({ where: [] }));
      service.destinationProjectCache.types = toObjectMapById(destTypes);
    };

    const cacheProductTypes = async () => {
      const destProductTypes = [];
      destProductTypes.push(await destProductTypeService.fetchAll({ where: [] }));
      service.destinationProjectCache.productTypes = toObjectMapById(destProductTypes);
    };

    const cacheCustomerGroups = async () => {
      const destCustGroups = [];
      destCustGroups.push(await destCustomerGroupService.getAll());
      service.destinationProjectCache.customerGroups = toObjectMapById(destCustGroups);
    };

    await Promise.all([cacheChannels, cacheStates, cacheTypes, cacheProductTypes, cacheCustomerGroups].map(func => func()));
    console.log('Finished caching destination CT project data');
  };

  const cacheSourceData = async () => {
    console.log('Caching source data');

    const cacheChannels = async () => {
      const sourceChannels = [];
      sourceChannels.push(await sourceChannelService.fetchAll());
      service.sourceProjectCache.channels = toObjectMapById(sourceChannels);
    };

    const cacheStates = async () => {
      const sourceStates = [];
      sourceStates.push(await sourceStateService.fetchAll({ where: [] }));
      service.sourceProjectCache.states = toObjectMapById(sourceStates);
    };

    const cacheTypes = async () => {
      const sourceTypes = [];
      sourceTypes.push(await sourceTypesService.fetchAll({ where: [] }));
      service.sourceProjectCache.types = toObjectMapById(sourceTypes);
    };

    const cacheProductTypes = async () => {
      const sourceProductTypes = [];
      sourceProductTypes.push(await sourceProductTypeService.fetchAll({ where: [] }));
      service.sourceProjectCache.productTypes = toObjectMapById(sourceProductTypes);
    };

    const cacheCustomerGroups = async () => {
      const sourceCustGroups = [];
      sourceCustGroups.push(await sourceCustomerGroupService.getAll());
      service.sourceProjectCache.customerGroups = toObjectMapById(sourceCustGroups);
    };

    await Promise.all([cacheChannels, cacheStates, cacheTypes, cacheProductTypes, cacheCustomerGroups].map(func => func()));
    console.log('Finished caching source CT project data');
  };

  service.cacheStaticData = async () => {
    await Promise.all([cacheSourceData, cacheDestinationData].map(func => func()));
  };

  service.clearProductCache = () => {
    service.sourceProjectCache.products.clear();
    service.destinationProjectCache.products.clear();
  };

  return service;
};

export default DataCache;
