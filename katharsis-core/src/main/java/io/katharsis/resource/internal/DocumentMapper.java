package io.katharsis.resource.internal;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.internal.boot.PropertiesProvider;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.resource.Document;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.JsonApiResponse;

public class DocumentMapper {

	private DocumentMapperUtil util;

	private ResourceMapper resourceMapper;

	private IncludeLookupSetter includeLookupSetter;

	public DocumentMapper(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, PropertiesProvider propertiesProvider) {
		this(resourceRegistry, objectMapper, propertiesProvider, false);
	}

	public DocumentMapper(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, PropertiesProvider propertiesProvider, boolean client) {
		this.util = new DocumentMapperUtil(resourceRegistry, objectMapper);
		this.resourceMapper = new ResourceMapper(util, client, objectMapper);
		this.includeLookupSetter = new IncludeLookupSetter(resourceRegistry, resourceMapper, propertiesProvider);
	}

	public Document toDocument(JsonApiResponse response, QueryAdapter queryAdapter) {
		return toDocument(response, queryAdapter, null);
	}

	public Document toDocument(JsonApiResponse response, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider) {
		if (response == null) {
			return null;
		}

		Document doc = new Document();
		addErrors(doc, response.getErrors());
		util.setMeta(doc, response.getMetaInformation());
		util.setLinks(doc, response.getLinksInformation());
		addData(doc, response.getEntity(), queryAdapter);
		addRelationDataAndInclusions(doc, response.getEntity(), queryAdapter, parameterProvider);

		return doc;
	}

	private void addRelationDataAndInclusions(Document doc, Object entity, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider) {
		if (doc.getData() != null) {
			includeLookupSetter.setIncludedElements(doc, entity, queryAdapter, parameterProvider);
		}
	}

	private void addData(Document doc, Object entity, QueryAdapter queryAdapter) {
		if (entity != null) {
			if (entity instanceof Iterable) {
				ArrayList<Object> dataList = new ArrayList<>();
				for (Object obj : (Iterable<?>) entity) {
					dataList.add(resourceMapper.toData(obj, queryAdapter));
				}
				doc.setData(dataList);
			} else {
				doc.setData(resourceMapper.toData(entity, queryAdapter));
			}
		}
	}

	private void addErrors(Document doc, Iterable<ErrorData> errors) {
		if (errors != null) {
			List<ErrorData> errorList = new ArrayList<>();
			for (ErrorData error : errors) {
				errorList.add(error);
			}
			doc.setErrors(errorList);
		}
	}

}
