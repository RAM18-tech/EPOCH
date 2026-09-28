import '@babel/polyfill';
import { Commercetools } from '../commercetools';
import { ProductsService } from '../products';

export const ProductPublish = ({ apiDestinationInfo }) => {
  const service = {};
  const ctApiDestination = Commercetools(apiDestinationInfo);
  const destProductsService = ProductsService({ commercetools: ctApiDestination, sleepLength: 1500 });

  service.publish = async ({ productKeys = [], batchSize = 1 }) => {
    const publishProduct = async (key) => {
      console.log(`Publishing product ${key}`);

      try {
        const product = await destProductsService.byKey({ key });

        if (product) {
          destProductsService.update(
            product,
            [
              {
                action: 'transitionState',
                state: { typeId: 'state', key: 'live' },
                force: true,
              },
              { action: 'publish' },
            ],
          );
        }
      } catch (err) {
        console.error(err);
      }
    };

    while (productKeys.length) {
      await Promise.all(productKeys.splice(0, batchSize).map(key => publishProduct(key)));
    }
  };

  return service;
};

export default ProductPublish;
