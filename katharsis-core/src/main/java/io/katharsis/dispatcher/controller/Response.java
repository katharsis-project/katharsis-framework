package io.katharsis.dispatcher.controller;

import io.katharsis.resource.Document;

public class Response {

	private Integer httpStatus;

	private Document document;

	public Response(Document document, Integer statusCode) {
		super();
		this.httpStatus = statusCode;
		this.document = document;
	}

	public Integer getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(Integer httpStatus) {
		this.httpStatus = httpStatus;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

}
