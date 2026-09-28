import '@babel/polyfill';
import { timePromise } from '../utils';
import { CartDiscountExport } from '../ct/cartDiscountExport';
import { CartDiscountImport } from '../ct/cartDiscountImport';

export const CartDiscountCopy = ({ apiSourceInfo, apiDestinationInfo, discountKeys = [] }) => {
  const service = {};

  const cartDiscountExport = CartDiscountExport({ apiSourceInfo });
  const cartDiscountImport = CartDiscountImport({ apiSourceInfo, apiDestinationInfo });

  const runExportImport = async () => {
    console.log(`Cart discount export source: ${JSON.stringify(apiSourceInfo, null, 2)}`);
    console.log(`Cart discount import destination: ${JSON.stringify(apiDestinationInfo, null, 2)}`);

    const exportedDiscounts = await cartDiscountExport.exportByKey(discountKeys);

    await cartDiscountImport.importDiscounts(exportedDiscounts);
  };

  service.run = async () => timePromise(runExportImport);

  return service;
};

export default CartDiscountCopy;
