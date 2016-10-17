package io.katharsis.rs.resource.registry;

import io.katharsis.resource.registry.ServiceUrlProvider;

import javax.ws.rs.core.UriInfo;

public class UriInfoServiceUrlProvider implements ServiceUrlProvider {

	private UriInfo globalUrlInfo;

	private ThreadLocal<UriInfo> threadLocal = new ThreadLocal<>();

	public UriInfoServiceUrlProvider() {
		// make use of thread local and started/finish events to get uriInfo
	}

	/**
	 * @deprecated Make use of the default constructor instead.
	 */
	@Deprecated
	public UriInfoServiceUrlProvider(UriInfo info) {
		this.globalUrlInfo = info;
	}

	@Override
	public String getUrl() {
		UriInfo urlInfo = globalUrlInfo;
		if (urlInfo == null) {
			urlInfo = threadLocal.get();
			if (urlInfo == null) {
				throw new IllegalStateException("uriInfo not available, make sure to call onRequestStarted in advance");
			}
		}
		String url = urlInfo.getBaseUri().toString();
		if(url.endsWith("/")){
			return url.substring(0, url.length() - 1);
		}else{
			return url;
		}
	}

	public void onRequestStarted(UriInfo uriInfo) {
		threadLocal.set(uriInfo);
	}

	public void onRequestFinished() {
		threadLocal.remove();
	}
}
