/*package com.dtv.dcp.epoch.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

*//**
 * The Interface HazelcastCacheResource.
 *//*
@Api("Cache")
@RequestMapping("/")
@Produces({ MediaType.APPLICATION_JSON })
public interface HazelcastCacheResource {

	*//**
	 * Evict cache.
	 *
	 * @param mapName the map name
	 * @param keys the keys
	 * @return the response
	 *//*
	@GET
	@RequestMapping("/v2/cache/evict")
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Evicts Cache", notes = "Evicts near Cache", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Cache Evicted successfully"),
	@ApiResponse(code = 500, message = "Cache Eviction operation failed.") })
	public Response evictCache(@QueryParam("mapName") String mapName, @QueryParam("keys") List<String> keys);
		
	*//**
	 * Fetch cache info.
	 *
	 * @param mapName the map name
	 * @param includeValues the include values
	 * @return the response
	 *//*
	@GET
	@RequestMapping("/v2/cache/keys")
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Evicts Cache", notes = "Evicts near Cache", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Cache Evicted successfully"),
	@ApiResponse(code = 500, message = "Cache Eviction operation failed.") })
	public Response fetchCacheInfo(@QueryParam("mapName") String mapName,
			@QueryParam("includeValues") boolean includeValues);
	
	
	*//**
	 * Gets the value from cache.
	 *
	 * @param mapName the map name
	 * @param key the key
	 * @return the value from cache
	 *//*
	@GET
	@RequestMapping("/v2/cache/getvaluefromcache")
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Retreieves the cachevalue for the key specified", notes = "Retreieves the cachevalue", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Retreieved the cachevalue successfully"),
	@ApiResponse(code = 500, message = "Error Occured While Retrieving value Cache key") })
	public Response getValueFromCache(@QueryParam("mapName") String mapName, @QueryParam("key") String key);
}*/