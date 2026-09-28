package com.dtv.dcp.epoch.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dtv.dcp.epoch.model.cache.CacheEvictionResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The Interface HazelcastCacheResource.
 */
@Api("Cache")
@RequestMapping("/epochofferms")
@Produces({ MediaType.APPLICATION_JSON })
@RestController
public interface RedisCacheResource {

	/**
	 * Evict cache.
	 *
	 * @param mapName the map name
	 * @param keys the keys
	 * @return the response
	 */
	@GET
	@RequestMapping("/cache/evict")
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Evicts Cache", notes = "Evicts near Cache", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Cache Evicted successfully"),
	@ApiResponse(code = 500, message = "Cache Eviction operation failed.") })
	public CacheEvictionResponse evictCache(@QueryParam("mapName") String mapName);

	
	/**
	 * Delete cache.
	 *
	 * @param mapName the map name
	 * @param keys    the keys
	 * @return the response
	 */
	@GET
	@RequestMapping("/cache/catalog/delete")
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Evicts Cache", notes = "Delete near Cache", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Cache Deleted successfully"),
			@ApiResponse(code = 500, message = "Cache Deletion operation failed.") })
	public CacheEvictionResponse deleteCatalogCache(@QueryParam("mapName") String mapName, @QueryParam("keys") List<String> keys);
	
}