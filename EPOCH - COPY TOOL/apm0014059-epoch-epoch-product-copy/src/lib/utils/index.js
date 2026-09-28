import lr from 'line-reader';
import Promise from 'bluebird';
import fetch from 'node-fetch';
import AbortController from 'abort-controller';
import HttpsProxyAgent from 'https-proxy-agent';
import { performance } from 'perf_hooks';

export const objectPathDelimiter = '/';

/**
 * Read a file and do a function for every line read
 */
export const forEachLine = (filename, func) => new Promise(((resolve, reject) => {
  lr.eachLine(filename, func, (err) => {
    if (err) {
      reject(err);
    } else {
      resolve();
    }
  });
}));

/**
 * Deep search object and get list of object with a matching key and value.
 * @returns
 */
export const findObjectsByProperty = (object, key, value) => {
  let found = [];

  const find = (anObject, propertyKey, propertyValue, matches) => {
    if (anObject == null) {
      return matches;
    }

    if (!propertyValue && Object.prototype.hasOwnProperty.call(anObject, innerProperty) && innerProperty === propertyKey) {
      matches.push(anObject[innerProperty]);
      return matches;
    }

    if (propertyValue && anObject[propertyKey] === propertyValue) {
      matches.push(anObject);
      return matches;
    }

    let innerProperty;

    for (let i = 0; i < Object.keys(anObject).length; i += 1) {
      innerProperty = Object.keys(anObject)[i];

      if (Object.prototype.hasOwnProperty.call(anObject, innerProperty) && typeof anObject[innerProperty] === 'object') {
        find(anObject[innerProperty], propertyKey, propertyValue, matches);
      }
    }
    return matches;
  };

  found = find(object, key, value, []);

  return found;
};

/**
 * Deep search object and list json paths of object with a matching key and value.
 *
 * For example:
 * Input is key = id, value = abc-32321-429328-3920390
 *
 * Output is
 * [/masterVariant/attributes/0/associatedProducts/0/bundleProducts/0/0/value/0/id]
 * @returns
 */
export const findObjectPaths = (object, aKey, aValue) => {
  let found = [];

  const find = (obj, name, val, path, matches) => {
    const currentPath = path || '';
    let matchingPath;

    if (!obj || typeof obj !== 'object') {
      return matches;
    }

    if (obj[name] === val) {
      matches.push(`${currentPath}${objectPathDelimiter}${name}`);
    }

    for (let i = 0; i < Object.keys(obj); i += 1) {
      const key = Object.keys(obj)[i];

      if (key === name && obj[key] === val) {
        matchingPath = currentPath;
      } else {
        find(obj[key], name, val, `${currentPath}${objectPathDelimiter}${key}`, matches);
      }

      if (matchingPath) {
        break;
      }
    }

    return matches;
  };

  found = find(object, aKey, aValue, '', []);

  return found;
};

/**
 * Check if input is a number
 */
export const isNumeric = (number) => {
  return !Number.isNaN(parseFloat(number)) && Number.isFinite(number);
};

/**
 * Facility for fetch via proxy for ProductExporter to work
 */
export const proxiedFetch = async (url, options) => {
  const controller = new AbortController();
  const { timeout = 10000 } = options;

  setTimeout(() => { controller.abort(); }, timeout);

  const proxy = process.env.HTTPS_PROXY || 'http://sub.proxy.att.com:8080';

  const fetchOption = {
    ...options,
    agent: new HttpsProxyAgent(proxy),
  };

  return fetch(url, fetchOption);
};

/**
 * Measure time taken for a wrapped promise to complete (in milliseconds)
 */
export const timePromise = async (promiseFunction) => {
  const begin = performance.now();
  try {
    await promiseFunction();
    const end = performance.now();
    const timeTakenMs = end - begin;
    return timeTakenMs;
  } catch (error) {
    console.error(error);
    const end = performance.now();
    const timeTakenMs = end - begin;
    return timeTakenMs;
  }
};

/**
 * Converts paginated CT api results 2d array to result object map by resource id.
 * Key is resource id, value is the result object.
 */
export const toObjectMapById = (ctResults2dArray) => {
  return ctResults2dArray
    .reduce((accumulatorArray, innerArray) => accumulatorArray.concat(innerArray), [])
    .filter(result => result.id)
    .reduce((accumulatorMap, object) => {
      accumulatorMap[object.id] = object;
      return accumulatorMap;
    }, {});
};

/**
 * Strip spaces, newline, and control characters from string
 */
export const stripWhitespace = (str) => str.replace(/\s+/g, '').replace(/[^\x20-\x7E]/gmi, '').trim();

/**
 * Pull GUIDs from a string
 */
export const extractGuids = (str) => {
  const guidRegex = /([a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12})/ig;
  const matches = [];
  let currentPattern;

  while (currentPattern = guidRegex.exec(str)) {
    matches.push(currentPattern.shift());
  }

  return matches.filter(exists => exists);
};

const Utils = () => { };

export default Utils;
