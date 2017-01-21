package io.katharsis.client;

import javax.ws.rs.core.FeatureContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.DefaultQuerySpecDeserializer;
import io.katharsis.rs.KatharsisFeature;

/**
 * Test-specific subclass of the {@link KatharsisFeature} used to 
 * configure extra test-specific JAX-RS functionality.
 * 
 * @author Craig Setera
 */
public class KatharsisTestFeature extends KatharsisFeature {
	private TestRequestFilter testRequestFilter;
	
	public KatharsisTestFeature(
		ObjectMapper objectMapper, 
		QueryParamsBuilder queryParamsBuilder,
		JsonServiceLocator jsonServiceLocator) 
	{
		super(objectMapper, queryParamsBuilder, jsonServiceLocator);
		testRequestFilter = new TestRequestFilter();
	}

	public KatharsisTestFeature(
		ObjectMapper objectMapper, 
		DefaultQuerySpecDeserializer defaultQuerySpecDeserializer,
		SampleJsonServiceLocator jsonServiceLocator) 
	{
		super(objectMapper, defaultQuerySpecDeserializer, jsonServiceLocator);
		testRequestFilter = new TestRequestFilter();
	}

	/*
	 * (non-Javadoc)
	 * @see io.katharsis.rs.KatharsisFeature#configure(javax.ws.rs.core.FeatureContext)
	 */
	@Override
	public boolean configure(FeatureContext context) {
		boolean result = super.configure(context);
		context.register(testRequestFilter);
		
		return result;
	}
	
	/**
	 * Return the {@link TestRequestFilter} that is registered
	 * with the feature.
	 * 
	 * @return
	 */
	public TestRequestFilter getTestFilter() {
		return testRequestFilter;
	}
}
