const { RunCreator } = require('./RunCreator');
const { RunSequencer } = require('./RunSequencer');
const { collections } = require('./collection.config.json');

const runSequencer = RunSequencer({
    runCreatorConstructor: RunCreator,
    collections,
    logger: console
    // S3 

})

runSequencer.runAllCollectionsSequentially();
