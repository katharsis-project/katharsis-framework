package io.katharsis.repository;

import io.katharsis.response.MetaInformation;

/**
 * An optional interface that can be implemented along with {@link ResourceRepository} or {@link
 * RelationshipRepository} to get meta information.
 */
public interface MetaRepository {

    /**
     * Return meta information about a resource. Can be called after save, update or delete repository methods call
     *
     * @return meta information object
     */
    MetaInformation getMetaInformation();
}
