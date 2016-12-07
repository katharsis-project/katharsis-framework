package io.katharsis.jpa.merge;

import io.katharsis.jpa.annotations.JpaMergeRelations;
import io.katharsis.jpa.annotations.JpaResource;
import io.katharsis.jpa.model.TestEntity;

@JpaResource(type = "merged")
@JpaMergeRelations(attributes = { "oneRelatedValue", "manyRelatedValues" })
public class MergedResource extends TestEntity {

}
