package io.katharsis.module;

public interface InitialzingModule extends Module {

	/**
	 * Called once Katharsis is fully initialized. From this point in time, the module is, for example,
	 * allowed to access the resource registry.
	 */
	public void init();

}
