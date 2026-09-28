import { createClient } from '@commercetools/sdk-client';
import { createAuthMiddlewareForClientCredentialsFlow } from '@commercetools/sdk-middleware-auth';
import { createHttpMiddleware } from '@commercetools/sdk-middleware-http';
import { createQueueMiddleware } from '@commercetools/sdk-middleware-queue';
import { createRequestBuilder } from '@commercetools/api-request-builder';
import fetch from 'node-fetch';
import HttpsProxyAgent from 'https-proxy-agent';


export const fetchPatched = (url, options = {}) => {
  const instanceOptions = {
    ...options,
  };

  if (!options.agent && process.env.HTTP_PROXY) {
    instanceOptions.agent = new HttpsProxyAgent(process.env.HTTP_PROXY);
  }

  return fetch(url, instanceOptions);
};

export const Commercetools = ({
  clientId, clientSecret, projectKey, host, oauthHost, scopes, concurrency = 10,
}) => {
  const commercetools = {};

  commercetools.client = createClient({
    middlewares: [
      createAuthMiddlewareForClientCredentialsFlow({
        host: oauthHost,
        projectKey,
        credentials: {
          clientId,
          clientSecret,
        },
        fetch: fetchPatched,
        scopes,
      }),
      createQueueMiddleware({ concurrency }),
      createHttpMiddleware({ host, fetch: fetchPatched, enableRetry: true }),
    ],
  });

  commercetools.getRequestBuilder = () => createRequestBuilder({ projectKey });

  return commercetools;
};
