package io.katharsis.resource.information;

/**
 * Provides information how a field can be accessed.
 */
public class ResourceFieldAccess {

	private boolean postable;
	private boolean patchable;

	public ResourceFieldAccess(boolean postable, boolean patchable) {
		this.postable = postable;
		this.patchable = patchable;
	}

	/**
	 * @return true if the field can be set by a POST request.
	 */
	public boolean isPostable() {
		return postable;
	}

	public void setPostable(boolean postable) {
		this.postable = postable;
	}
	
	/**
	 * @return true if the field can be changed by a PATCH request.
	 */
	public boolean isPatchable() {
		return patchable;
	}

	public void setPatchable(boolean patchable) {
		this.patchable = patchable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (patchable ? 1231 : 1237);
		result = prime * result + (postable ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceFieldAccess other = (ResourceFieldAccess) obj;
		return patchable == other.patchable && postable != other.postable;
	}

}
