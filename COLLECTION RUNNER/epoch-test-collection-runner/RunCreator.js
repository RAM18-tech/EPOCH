const newman = require('newman');

function RunCreator(args) {
    const defaultArgs = {
        logger: console
    }

    args = Object.assign(defaultArgs, args);

    const {
        newmanOptions,
        collectionName,
        logger
    } = args;

    return {
        run() {
            return newman.run(newmanOptions, function (err) {
                if (err) {
                    logger.log(`${collectionName} failed with error: ${err}`);
                }
                logger.log(`${collectionName} run is complete!`);
            });
        }
    }
}

module.exports = {
    RunCreator
}