package io.katharsis.module;

import org.junit.Test;
import org.mockito.Mockito;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.decorate.RelationshipRepositoryDecoratorBase;
import io.katharsis.repository.decorate.ResourceRepositoryDecoratorBase;
import io.katharsis.resource.mock.models.Schedule;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.ScheduleRepository;

public class DecoratorTest {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testDecoratedResourceRepositoryBase() {
		ScheduleRepository repository = Mockito.mock(ScheduleRepository.class);
		ResourceRepositoryDecoratorBase<Schedule, Long> decorator = new ResourceRepositoryDecoratorBase() {
		};
		decorator.setDecoratedObject(repository);

		decorator.create(null);
		Mockito.verify(repository, Mockito.times(1)).create(Mockito.any(Schedule.class));

		decorator.delete(null);
		Mockito.verify(repository, Mockito.times(1)).delete(Mockito.anyLong());

		decorator.findAll(null);
		Mockito.verify(repository, Mockito.times(1)).findAll(Mockito.any(QuerySpec.class));

		decorator.findAll(null, null);
		Mockito.verify(repository, Mockito.times(1)).findAll(Mockito.anyListOf(Long.class), Mockito.any(QuerySpec.class));

		decorator.findOne(null, null);
		Mockito.verify(repository, Mockito.times(1)).findOne(Mockito.anyLong(), Mockito.any(QuerySpec.class));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testDecoratedRelationshipRepositoryBase() {
		RelationshipRepositoryV2<Schedule, Long, Task, Long> repository = Mockito.mock(RelationshipRepositoryV2.class);
		RelationshipRepositoryDecoratorBase<Schedule, Long, Task, Long> decorator = new RelationshipRepositoryDecoratorBase() {
		};
		decorator.setDecoratedObject(repository);

		decorator.findManyTargets(null, null, null);
		Mockito.verify(repository, Mockito.times(1)).findManyTargets(Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(QuerySpec.class));

		decorator.findOneTarget(null, null, null);
		Mockito.verify(repository, Mockito.times(1)).findOneTarget(Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(QuerySpec.class));

		decorator.setRelation(null, null, null);
		Mockito.verify(repository, Mockito.times(1)).setRelation(Mockito.any(Schedule.class), Mockito.anyLong(),
				Mockito.anyString());

		decorator.addRelations(null, null, null);
		Mockito.verify(repository, Mockito.times(1)).addRelations(Mockito.any(Schedule.class), Mockito.anyListOf(Long.class),
				Mockito.anyString());

		decorator.removeRelations(null, null, null);
		Mockito.verify(repository, Mockito.times(1)).removeRelations(Mockito.any(Schedule.class), Mockito.anyListOf(Long.class),
				Mockito.anyString());
	}
}
