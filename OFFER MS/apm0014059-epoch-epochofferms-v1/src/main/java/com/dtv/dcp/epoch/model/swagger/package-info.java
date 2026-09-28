/**
 * Swagger documentation has limilation that it cannot parse the generic types
 * in the @ApiOperation "response" attribute. It display type object as object
 * without it's attributes. Due to this limitation the workaround is that every
 * model class should have a concrete class created extending the default
 * Resource class and the public method should be overridden.
 * 
 */
package com.dtv.dcp.epoch.model.swagger;