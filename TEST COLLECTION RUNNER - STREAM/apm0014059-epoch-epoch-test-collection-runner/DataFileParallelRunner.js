const newman = require('newman');
const { postman_env } = require("./manualcollection.config.json");
const { manual_collections } = require("./manualcollection.config.json");


const collections = [
   
  {
    collection: `./Data Collections/Protection Plan _Manual.postman_collection.json`, // replace with your collection path
	  collectionName: 'Protection Plan _Manual',
    folder: 'Removal Rule  product specified channels',
    iterationData: './DataFile/Removal Rule Product Specified Channels.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/Protection Plan _Manual.postman_collection.json`, // replace with your collection path
    collectionName: 'Protection Plan _Manual',
	  folder: 'OPUS Specified Channels',
    iterationData: 'DataFile/Opus Specified Channel.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/Protection Plan _Manual.postman_collection.json`, // replace with your collection path
    collectionName: 'Protection Plan _Manual',
	folder: 'Sales Indirect Launch',
    iterationData: 'DataFile/Sales IndirectLaunch.json',
    iterationCount: 5
  },  
  {
    collection: `./Data Collections/LoyaltyOffers Script Collection.postman_collection.json`, // replace with your collection path
    collectionName: 'LoyaltyOffers Script Collection',
	  folder: 'Loyalty Offer Not On Account',
    iterationData: 'DataFile/Contract_Loyalty_offer _Veribages.json',
    iterationCount: 2
  },
   {
    collection: `./Data Collections/LoyaltyOffers Script Collection.postman_collection.json`, // replace with your collection path
    collectionName: 'LoyaltyOffers Script Collection',
	  folder: 'Loyalty Offer Not On Account',
    iterationData: 'DataFile/EDSP_Loyalty_offer_verbiage.json',
    iterationCount: 2
  },
   {
    collection: `./Data Collections/LoyaltyOffers Script Collection.postman_collection.json`, // replace with your collection path
    collectionName: 'LoyaltyOffers Script Collection',
	  folder: 'Loyalty Offer Not On Account',
    iterationData: 'DataFile/TAZ_Loyalty_offer_veribages.json',
    iterationCount: 2
  },
   {
    collection: `./Data Collections/LoyaltyOffers Script Collection.postman_collection.json`, // replace with your collection path
    collectionName: 'LoyaltyOffers Script Collection',
	  folder: 'Loyalty Offer Not On Account',
    iterationData: 'DataFile/TAZBYOD_Loyalty_offer_veribages.json',
    iterationCount: 2
  },
   {
    collection: `./Data Collections/LoyaltyOffers Script Collection.postman_collection.json`, // replace with your collection path
    collectionName: 'LoyaltyOffers Script Collection',
	folder: 'Loyalty Offer Not On Account',
    iterationData: 'DataFile/TAZCONTRACT_Loyalty_offer_veribages.json',
    iterationCount: 2
  },
  {
    collection: `./Data Collections/LoyaltyOffers Script Collection.postman_collection.json`, // replace with your collection path
    collectionName: 'LoyaltyOffers Script Collection',
	  folder: 'Loyalty Offers On Account [A1,B1,C1,A2,B2,C2]',
    iterationData: 'DataFile/Contract _Benefit _A1_B1_C1_A2_B2_C2 _on_account.json',
    iterationCount: 19
  },
  {
    collection: `./Data Collections/LoyaltyOffers Script Collection.postman_collection.json`, // replace with your collection path
    collectionName: 'LoyaltyOffers Script Collection',
	  folder: 'Loyalty Offers On Account [A1,B1,C1,A2,B2,C2]',
    iterationData: 'DataFile/EDSP_Benefit_ A1_B1_C1_A2_B2_C2_on_account.json',
    iterationCount: 19
  },
  {
    collection: `./Data Collections/LoyaltyOffers Script Collection.postman_collection.json`, // replace with your collection path
    collectionName: 'LoyaltyOffers Script Collection',
	  folder: 'Loyalty Offers On Account [A1,B1,C1,A2,B2,C2]',
    iterationData: 'DataFile/TAZ_Benefit_A1_B1_C1_A2_B2_C2 _on_account.json',
    iterationCount: 19
  },
  {
    collection: `./Data Collections/LoyaltyOffers Script Collection.postman_collection.json`, // replace with your collection path
    collectionName: 'LoyaltyOffers Script Collection',
	  folder: 'Loyalty Offers On Account [A1,B1,C1,A2,B2,C2]',
    iterationData: 'DataFile/TAZBYOD_Benefit_A1_B1_C1_A2_B2_C2_on_account.json',
    iterationCount: 17
  },
  {
    collection: `./Data Collections/LoyaltyOffers Script Collection.postman_collection.json`, // replace with your collection path
    collectionName: 'LoyaltyOffers Script Collection',
	  folder: 'Loyalty Offers On Account [A1,B1,C1,A2,B2,C2]',
    iterationData: 'DataFile/TAZCONTRACT_Benefit_A1_B1_C1_A2_B2_C2_on_account.json',
    iterationCount: 19
  },
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'ISP without Optimomas',
    iterationData: './DataFile/INTRO ISP_without _Optimomas.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'ISP with Optimomas',
    iterationData: 'DataFile/INTRO ISP _with_optimomas.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'RR  DTV Affiliates',
    iterationData: './DataFile/TAZCONTRACT Affiliates.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'WESALUTE',
    iterationData: 'DataFile/Wesalute dealercode.json',
    iterationCount: 2
  },
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'DirectIntegrationPartner with optimomas -INTRO',
    iterationData: 'DataFile/INTRO with Optimomas.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'OPUS -INTRO SAVENOW',
    iterationData: 'DataFile/SAVENOW Data File_OPUS Store Updates.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'OPUS - LEASE RR_MD',
    iterationData: 'DataFile/OPUS _MD Specified Channels.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'DIP with RR_MD',
    iterationData: 'DataFile/MD_Specified Channels.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'DIP with RR DAP_MD',
    iterationData: 'DataFile/DAP _MD Dealers.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'OPUS - LEASE RR_DAP_MD',
    iterationData: 'DataFile/DAP_MD STORES.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'DirectIntegration Partnerwith optimomas - INTRO SAVENOW',
    iterationData: 'DataFile/INTRO SAVENOW with OPTIMOMAS delaer code.json',
    iterationCount: 5
  },  
  {
    collection: `./Data Collections/RR_MANUAL.postman_collection.json`, // replace with your collection path
    collectionName: 'RR_MANUAL',
    folder: 'DirectIntegrationPartner without optimomas - INTRO',
    iterationData: 'DataFile/INTRO Without optimomas.json',
    iterationCount: 5
  },
  {
    collection: `./Data Collections/Genre Sales Manual.postman_collection.json`, // replace with your collection path
    collectionName: 'Genre Sales Manual',
	  folder: 'Opus Store',
    iterationData: 'DataFile/Genre opus stores.json',
    iterationCount: 10
  },
  {
    collection: `./Data Collections/Genre Sales Manual.postman_collection.json`, // replace with your collection path
    collectionName: 'Genre Sales Manual',
	folder: 'Opus Store Migration',
    iterationData: 'DataFile/Genre Migration StoreID.json',
    iterationCount: 10
  }
];

function getFullPath(source) {
  let path = [source.name];
  let parent = source.__parent;
  while (parent) {
    if (parent.name) {
      path.unshift(parent.name);
    }
    parent = parent.__parent;
  }
  return path.join('/');
}

async function runCollection(config) {
  let current_manual_collection = `./Data Collections/${manual_collections}.postman_collection.json`;
  let promises = postman_env.map(env => {
    return new Promise((resolve, reject) => {
      newman.run({
        //collection: current_manual_collection,
        ...config,
        environment: require(`./Environments/${env}`),
        reporters: 'htmlextra',
        reporter: {
          htmlextra: {
            export: `./Reports/${config.folder} Report.html`,
            browserTitle: "EPOCH Test Collection Report",
            title: `${config.folder} Report`,
          },
        },
      }, function (err, summary) {
        if (err || !summary || summary.error) {
          reject(err || new Error('No summary provided'));
        } else {
          let failures = summary.run.failures;
          let failedTests = failures.map(failure =>
            getFullPath(failure.source) + ':' + JSON.stringify(failure.error.message)
          );
          resolve({ "environment": env, "data": [failedTests, summary.run.failures.length] });
        }
      });
    });
  });

  let final_result = { "collection": config.collectionName + " " + config.folder }
  let minimum_failures_count = Number.POSITIVE_INFINITY
  let results = await Promise.all(promises);

  results.forEach(({ environment, data: [failures, count] }) => {
    let envKey = environment.split(' ')[1].split('.')[0];
    final_result[envKey] = count;

    if (count < minimum_failures_count) {
      minimum_failures_count = count;
      final_result['failures'] = failures;
    }
  });

  return final_result;
}

async function runAllCollectionsInParallel() {
  let test_results = [];
  let test_failures = []

  let maxParallel = 3; // Maximum number of parallel operations

  for (let i = 0; i < collections.length; i += maxParallel) {
    let promises = collections.slice(i, i + maxParallel).map(config => {
      // Check if the collection is in the manual_collections array
      if (manual_collections.includes(config.collectionName)) {
        return runCollection(config).catch(error => console.error(`Error running collection ${config.collectionName}: ${error}`));
      } else {
        console.log(`Skipping collection ${config.collectionName} because it is not in the manual_collections array`);
        return Promise.resolve(); // Resolve the promise immediately
      }
    });

    let results = await Promise.all(promises);

    results.forEach(result => {
      if (result) { // Check if the result is not undefined
        test_failures.push({ "Collection": result['collection'], "Errors": result['failures'] });
        delete result['failures'];
        test_results.push(result);
      }
    });
  }
  console.table(test_results);
  console.log("")
  console.log('--------------------------------   ERRORS   --------------------------------')
  console.log("")
  test_failures.forEach(collection => {
    console.log(collection.Collection);
    collection.Errors.forEach(error => {
      console.log("       " + error);
    })
  })

}

(async () => {
  console.log('postman_env:', postman_env); // Debug log
  console.log('Collections to run:', collections); // Debug log
  let startTime = Date.now();
  await runAllCollectionsInParallel();
  let endTime = Date.now();
  let timeTaken = endTime - startTime; // Time taken in milliseconds
  let hours = Math.floor(timeTaken / 1000 / 60 / 60);
  let minutes = Math.floor((timeTaken / 1000 / 60) % 60);
  let seconds = ((timeTaken / 1000) % 60).toFixed(0);
  console.log("\n\n")
  console.log(`Total time taken: ${hours} hours, ${minutes} minutes, and ${seconds} seconds`);
})();