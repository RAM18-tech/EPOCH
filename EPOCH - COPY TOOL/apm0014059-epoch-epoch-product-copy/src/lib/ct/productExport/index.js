import SdkAuth from '@commercetools/sdk-auth';
import ProductExporter from '@commercetools/product-exporter';
import fs from 'fs';
import { createClient } from '@commercetools/sdk-client';
import { createAuthMiddlewareForClientCredentialsFlow } from '@commercetools/sdk-middleware-auth';
import { createHttpMiddleware } from '@commercetools/sdk-middleware-http';
import { forEachLine, proxiedFetch } from '../../utils';

export const ProductExport = ({ apiSourceInfo }) => {
  const service = {};

  let productExporterAccessToken;

  const productExporterApiConfig = {
    host: apiSourceInfo.oauthHost,
    apiUrl: apiSourceInfo.host,
    projectKey: apiSourceInfo.projectKey,
    disableRefreshToken: false,
    credentials: {
      clientId: apiSourceInfo.clientId,
      clientSecret: apiSourceInfo.clientSecret,
    },
    scopes: apiSourceInfo.scopes,
    fetch: proxiedFetch,
  };

  const productExporterMiddlewareClient = createClient({
    middlewares: [
      createAuthMiddlewareForClientCredentialsFlow({
        ...productExporterApiConfig,
      }),
      createHttpMiddleware({
        host: productExporterApiConfig.apiUrl,
        enableRetry: true,
        fetch: proxiedFetch,
      }),
    ],
  });

  const getProductExporterAccessToken = async () => {
    const authClient = new SdkAuth(productExporterApiConfig);
    const token = await authClient.clientCredentialsFlow();
    return token;
  };

  const checkToken = async () => {
    if (!productExporterAccessToken) {
      productExporterAccessToken = await getProductExporterAccessToken();
    }
  };

  const exportProducts = async (queryPredicate) => {
    const exportConfig = {
      batch: 100,
      expand: [],
      json: true,
      predicate: queryPredicate,
      staged: true,
    };

    const logger = {
      error: console.error,
      warn: console.warn,
      info: console.log,
      debug: console.debug,
    };

    const productExporter = new ProductExporter(
      productExporterApiConfig,
      exportConfig,
      logger,
      productExporterAccessToken.access_token,
    );

    productExporter.client = productExporterMiddlewareClient;

    const tmpFile = `export.${Date.now()}.tmp.json`;

    const outputStream = fs.createWriteStream(tmpFile);
    outputStream.on('error', () => { });
    outputStream.on('finish', () => process.stdout.write('Done with export\n'));

    await productExporter.run(outputStream);

    const products = [];

    await forEachLine(tmpFile, line => products.push(JSON.parse(line)));

    return products;
  };

  service.exportSatelliteProducts = async () => {
    await checkToken();

    const exportedProducts = [];

    await Promise.all([
      async () => {
        console.log('Exporting non-offer satellite products');

        const exportedNonOffers = (await exportProducts('masterVariant(attributes(name="productFamily" and value(key="satellite")))'))
          .filter(product => product.key && product.key.toLowerCase().includes('_satellite'));

        exportedProducts.push(...exportedNonOffers);
      },
      async () => {
        console.log('Exporting offer satellite products');

        const exportedOffers = (await exportProducts('masterVariant(attributes(name="offerProductFamily" and value(key="satellite")))'))
          .filter(product => product.key && product.key.toLowerCase().includes('_satellite'));

        exportedProducts.push(...exportedOffers);
      },
    ].map(func => func()));

    return exportedProducts;
  };

  service.exportByKey = async (productKeys = []) => {
    await checkToken();

    let exportedProducts = [];

    if (productKeys.length) {
      console.log(`Exporting products by keys: ${productKeys}`);

      exportedProducts = await exportProducts(`(key in ( ${productKeys.map(k => `"${k}"`).join(',')}))`);
    }

    return exportedProducts;
  };

  return service;
};

export default ProductExport;
