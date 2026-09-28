import '@babel/polyfill';
import { timePromise } from '../utils';
import { ProductExport } from '../ct/productExport';
import { ProductImport } from '../ct/productImport';
import { ProductPublish } from '../ct/productPublish';

export const ProductCopy = ({ apiSourceInfo, apiDestinationInfo, productKeys = [] }) => {
  const service = {};

  const productExport = ProductExport({ apiSourceInfo });
  const productPublish = ProductPublish({ apiDestinationInfo });
  const productImport = ProductImport({ apiSourceInfo, apiDestinationInfo });

  const runExportImport = async () => {
    console.log(`Product export source: ${JSON.stringify(apiSourceInfo, null, 2)}`);
    console.log(`Product import destination: ${JSON.stringify(apiDestinationInfo, null, 2)}`);

    const exportedProducts = await productExport.exportByKey(productKeys);

    await productImport.importProducts(exportedProducts);

    await productPublish.publish({
      productKeys: exportedProducts.map(draft => draft.key),
      batchSize: 25,
    });
  };

  service.run = async () => timePromise(runExportImport);

  return service;
};

export default ProductCopy;
