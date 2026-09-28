# EPOCH Postman Test Collection Runner

## Introduction
This project facilitates running multiple postman collections automatically and generates reports for each collection.  

## PreRequisites

The following should be installed on your machine for the project to work:

* [Node](https://nodejs.org/en/) - LTS version (Currently 14.x)

## Setup

### Postman Dependencies

1. Create "Environments" folder under the main project.  Export postman environment files here.  
2. Create "Test Collections" folder under the main project.  Export postman collections here.  The file names should look like this: *.postman_collection.json

### Project Dependencies

Run `npm install` to install the package dependencies

### Test Collection Configuration
1. Configure the `collection.config.json` file to set the `postman_env` property 
2. Configure the `collection.config.json` file to set the `collections` property to set the collections you want to run

## Test Collection Execution
1. Run your you collections with `npm test`

## Licensing
[MIT](https://opensource.org/licenses/MIT)
