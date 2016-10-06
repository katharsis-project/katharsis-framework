package io.katharsis.jpa.mapping;

import javax.persistence.EntityManager;

import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.dto.RelatedDTO;
import io.katharsis.jpa.query.Tuple;

public class RelatedDTOMapper implements JpaMapper<RelatedEntity, RelatedDTO> {

	private EntityManager em;

	public RelatedDTOMapper(EntityManager em) {
		this.em = em;
	}

	@Override
	public RelatedDTO map(Tuple tuple) {
		RelatedDTO dto = new RelatedDTO();
		RelatedEntity entity = tuple.get(0, RelatedEntity.class);
		dto.setId(entity.getId());
		dto.setStringValue(entity.getStringValue());
		return dto;
	}

	@Override
	public RelatedEntity unmap(RelatedDTO dto) {
		RelatedEntity entity = em.find(RelatedEntity.class, dto.getId());
		if (entity == null) {
			entity = new RelatedEntity();
			entity.setId(dto.getId());
		}
		entity.setStringValue(dto.getStringValue());
		return entity;
	}
}
