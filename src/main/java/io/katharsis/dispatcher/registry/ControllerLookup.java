package io.katharsis.dispatcher.registry;

import io.katharsis.dispatcher.controller.BaseController;

import java.util.Set;

/**
 * Gets the instances of the {@link BaseController}'s.
 */
public interface ControllerLookup {

	/**
	 * @return the instances of the {@link BaseController}'s.
	 */
	Set<BaseController> getControllers();
}
