const { postman_env } = require("./collection.config.json");
const { collections } = require("./collection.config.json");
const newman = require('newman');
 
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
 
async function runCollection(collection) {
    let current_collection=`./Test Collections/${collection}.postman_collection.json`;
    let promises = postman_env.map(env => {
        return new Promise((resolve, reject) => {
            newman.run({
                collection: current_collection,
                environment:`./Environments/${env}`,
                reporters: ['progress']
               
            }, function (err, summary) {
                if (err || !summary) {
                    reject(err);
                } else if (!summary.run) {
                    reject(new Error('summary.run is undefined'));
                } else {
                    let failures = summary.run.failures;
                    let failedTests = failures.map(failure =>
                        getFullPath(failure.source)+':'+JSON.stringify(failure.error.message)
                    );
                    resolve({"environment":env, "data": [failedTests,summary.run.failures.length] });
                }
            });
        });
    });
 
    let final_result = { "collection": collection }
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
    let test_results=[];
    let test_failures=[]
 
    let maxParallel = 3; // Maximum number of parallel operations
 
    for (let i = 0; i < collections.length; i += maxParallel) {
        let promises = collections.slice(i, i + maxParallel).map(collection => {
            return runCollection(collection).catch(error => console.error(`Error running collection ${collection}: ${error}`));
        });
 
 
        let results = await Promise.all(promises);
 
        results.forEach(result => {
            test_failures.push({ "Collection": result['collection'], "Errors": result['failures'] });
            delete result['failures'];
            test_results.push(result);
        });
    }
    console.table(test_results);
    console.log("")
    console.log('--------------------------------   ERRORS   --------------------------------')
    console.log("")
    test_failures.forEach(collection=>{
        console.log(collection.Collection);
        collection.Errors.forEach(error =>{
            console.log("       "+error);
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