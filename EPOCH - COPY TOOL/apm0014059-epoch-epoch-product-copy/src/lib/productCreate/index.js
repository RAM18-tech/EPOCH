import '@babel/polyfill';
import { cloneDeep } from 'lodash';
import { timePromise } from '../utils';
import { ProductExport } from '../ct/productExport';
import { ProductPublish } from '../ct/productPublish';
import { Commercetools } from '../ct/commercetools';
import { ProductsService } from '../ct/products';

export const ProductCreate = ({ apiSourceInfo, apiDestinationInfo, productKeys = [] }) => {
  const service = {};

  const productExport = ProductExport({ apiSourceInfo });
  const productPublish = ProductPublish({ apiDestinationInfo });
  const ctApiDestination = Commercetools(apiDestinationInfo);
  const productsService = ProductsService({ commercetools: ctApiDestination, sleepLength: 1500 });

  const create_product= async (exportedProductDraft) =>{
    try {
        console.log("Creating product "+exportedProductDraft.key)
        await productsService.create(exportedProductDraft);
      } catch (err) {
        console.error(err);
      }
  };


  const runExportImport = async () => {
    console.log(`Product export source: ${JSON.stringify(apiSourceInfo, null, 2)}`);
    console.log(`Product import destination: ${JSON.stringify(apiDestinationInfo, null, 2)}`);

    let old_SKUs=[];

    productKeys.forEach(product =>{
        old_SKUs.push(product.split(":")[0])
    });

    const exportedProducts = await productExport.exportByKey(old_SKUs);

    const finalProducts=[];

    for (let i = 0; i < productKeys.length; i += 1) {
        let oldProductKey=productKeys[i].split(":")[0];
        let newProductKey=productKeys[i].split(":")[1];
        let exportedProductDraft = {};
        exportedProducts.forEach( exprotedProd => {
            if(exprotedProd.key.includes(oldProductKey)){
                exportedProductDraft=cloneDeep(exprotedProd);
            }
        });
        exportedProductDraft.slug.en="offer-"+newProductKey;
        exportedProductDraft.key=newProductKey;

        finalProducts.push(exportedProductDraft);

        await create_product(exportedProductDraft);
      }



    await productPublish.publish({
      productKeys: finalProducts.map(draft => draft.key),
      batchSize: 25,
    });
    };

    service.run = async () => timePromise(runExportImport);

    return service;
};

export default ProductCreate;
