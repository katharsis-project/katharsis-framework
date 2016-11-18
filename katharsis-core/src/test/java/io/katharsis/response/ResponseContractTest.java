package io.katharsis.response;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.information.ResourceRepositoryInformation;
import io.katharsis.repository.information.internal.ResourceRepositoryInformationImpl;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.RegistryEntry;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import java.lang.reflect.Field;

public class ResponseContractTest {

    @Test
    public void ContainerEqualsContract() throws NoSuchFieldException {
        EqualsVerifier.forClass(Container.class)
            .withPrefabValues(QueryParams.class, new QueryParams(), new QueryParams())
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void DataLinksContainerEqualsContract() throws NoSuchFieldException {
        EqualsVerifier.forClass(DataLinksContainer.class)
                .withPrefabValues(Field.class, String.class.getDeclaredField("value"), String.class.getDeclaredField("hash"))
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void LinkageContainerContainerEqualsContract() throws NoSuchFieldException {
        ResourceRepositoryInformation resourceInformationRed = newRepositoryInformation(String.class, null);
        ResourceRepositoryInformation resourceInformationBlack = newRepositoryInformation(Integer.class, null);

        @SuppressWarnings("unchecked") RegistryEntry registryEntryRed = new RegistryEntry(resourceInformationRed, null, null);
        @SuppressWarnings("unchecked") RegistryEntry registryEntryBlack = new RegistryEntry(resourceInformationBlack, null, null);

        EqualsVerifier.forClass(LinkageContainer.class)
                .withPrefabValues(Field.class, String.class.getDeclaredField("value"), String.class.getDeclaredField("hash"))
                .withPrefabValues(ResourceRepositoryInformation.class, resourceInformationRed, resourceInformationBlack)
                .withPrefabValues(RegistryEntry.class, registryEntryRed, registryEntryBlack)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    private <T> ResourceRepositoryInformation newRepositoryInformation(Class<T> repositoryClass, String path) {
		return new ResourceRepositoryInformationImpl(null, path, new ResourceInformation(Task.class, path, null));
	}

    @Test
    public void RelationshipContainerContainerEqualsContract() throws NoSuchFieldException {
        EqualsVerifier.forClass(RelationshipContainer.class)
                .withPrefabValues(Field.class, String.class.getDeclaredField("value"), String.class.getDeclaredField("hash"))
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}
