package io.katharsis.resource.internal;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.Document;
import io.katharsis.resource.LinksContainer;
import io.katharsis.resource.MetaContainer;
import io.katharsis.resource.Resource;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.internal.DocumentMapperUtil.DefaultSelfRelatedLinksInformation;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.SelfLinksInformation;
import io.katharsis.utils.PropertyUtils;

public class DocumentMapper {

	private ResourceRegistry resourceRegistry;
	private ObjectMapper objectMapper;

	private io.katharsis.resource.internal.IncludedRelationshipExtractor includedRelationshipExtractor;
	private DocumentMapperUtil util;

	private ResourceMapper resourceMapper;

	public DocumentMapper(ResourceRegistry resourceRegistry, ObjectMapper objectMapper) {
		this(resourceRegistry, objectMapper, false);
	}

	public DocumentMapper(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, boolean client) {
		this.resourceRegistry = resourceRegistry;
		this.objectMapper = objectMapper;

		this.util = new DocumentMapperUtil(resourceRegistry, objectMapper);
		this.resourceMapper = new ResourceMapper(util, client, objectMapper);
		this.includedRelationshipExtractor = new IncludedRelationshipExtractor(util, resourceMapper);
	}

	public Document toDocument(JsonApiResponse response, QueryAdapter queryAdapter) {
		if (response == null) {
			return null;
		}

		Document doc = new Document();
		addErrors(doc, response.getErrors());
		util.setMeta(doc, response.getMetaInformation());
		util.setLinks(doc, response.getLinksInformation());
		addData(doc, response.getEntity(), queryAdapter);
		addIncluded(doc, response.getEntity(), queryAdapter);
		return doc;
	}

	private void addIncluded(Document doc, Object entity, QueryAdapter queryAdapter) {
		List<Resource> data = DocumentMapperUtil.toList(doc.getData());
		List<Object> entities = DocumentMapperUtil.toList(entity);

		doc.setIncluded(includedRelationshipExtractor.extractIncludedResources(data, entities, queryAdapter));
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
