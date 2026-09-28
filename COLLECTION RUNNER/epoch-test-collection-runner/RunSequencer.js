const { promisify } = require("util");
const { postman_env } = require("./collection.config.json");

function RunSequencer(args) {
    const defaultArgs = {
        logger: console
    };

    args = Object.assign(defaultArgs, args);

    const {
        collections,
        runCreatorConstructor,
        logger
    } = args;

    function logError(collectionName, error) {
        logger.log(`${collectionName} failed with error:  ${error}`);
    }

    function extractCollectionAndReporters({ collectionsFilePath, reportersFilePath }) {
        return {
            collectionJSON: require(collectionsFilePath),
            reportersJSON: require(reportersFilePath)
        };
    }
    // Function iterates through collections sequentially/recursively
    async function runNextCollection(currentCollection, results, numberOfRuns, failures) {
        // Check the input (attempt call to require and handle issues accorindlgy)
        // @TODO -- Handle require failures (put in try/catch)
        try {
            const {
                collectionJSON,
                reportersJSON
            } = extractCollectionAndReporters({
                collectionsFilePath: `./Test Collections/${currentCollection}.postman_collection.json`,
                reportersFilePath: `./Environments/${postman_env}`
            });

            // Promisify and Block the Newman Run
            const result = await new Promise((resolve, reject) => {
                const newmanRun = runCreatorConstructor({
                    newmanOptions: {
                        collection: collectionJSON,
                        environment: reportersJSON,
                        reporters: ['htmlextra'],
                        reporter: {
                            htmlextra: {
                                export: `./Reports/${currentCollection}.html`,
                                browserTitle: "EPOCH Test Collection Report",
                                title: `${currentCollection} Report`,
                            }
                        }
                    },
                    collectionName: currentCollection,
                    logger
                });
                newmanRun.run().on('done', function (err, summary) {
                    if (err || summary.error) {
                        reject(err);
                    }
                    resolve({ [currentCollection]: summary.run.failures.length });
                })
            }).catch((error) => {
                logError(currentCollection, error);
            });

            // retry up to 3 times if there are errors
            if (result[currentCollection] > 0 && numberOfRuns < 0) {
                console.log(`Retrying ${currentCollection}`);
                numberOfRuns++;
                await new Promise((resolve) => {
                    setTimeout(resolve, 5000);
                })
                return await runNextCollection(currentCollection, results, numberOfRuns, failures);
            }
            // Update the Results
            //Object.assign(results, result);
            results.push({ "collection": currentCollection, "failures": result[currentCollection] });
            if(result[currentCollection] > 0) {
                failures.push({ "collection": currentCollection, "failures": result[currentCollection] });
            }
            // Move on to the next collection
            const nextCollection = collections.shift();
            // Base case, we're done
            if (!nextCollection) {
                return;
            }
            // Call the next one sequentially/rescursively 
            return await runNextCollection(nextCollection, results, 0, failures);
        } catch (error) {
            logError(currentCollection, error);
            // Move on to the next collection
            const nextCollection = collections.shift();
            // Base case, we're done
            if (!nextCollection) {
                return;
            }
            // Call the next one sequentially/rescursively 
            return await runNextCollection(nextCollection, results, 0, failures);
        }
    }

    return {
        async runAllCollectionsSequentially() {
			console.log("postman_env : " + postman_env);
            const results = [];
            const failures = [];
            const nextCollection = collections.shift();
            await runNextCollection(nextCollection, results, 0, failures);
            console.table(results);
            console.table(failures);
        }
    }
}

module.exports = {
    RunSequencer
}