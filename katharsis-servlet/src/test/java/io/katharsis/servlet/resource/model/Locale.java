package io.katharsis.servlet.resource.model;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;

/**
 * Created by nickmitchell on 1/6/17.
 */
@JsonApiResource(type = "lang-locales")
public class Locale {
	@JsonApiId
	private Long id;

	private java.util.Locale locale;

	public Locale(Long id, java.util.Locale locale) {
		this.id = id;
		this.locale = locale;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public java.util.Locale getLocale() {
		return locale;
	}

	public void setLocale(java.util.Locale locale) {
		this.locale = locale;
	}
}
