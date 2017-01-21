package io.katharsis.core.internal.dispatcher.filter;

import java.io.Serializable;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.repository.decorate.RepositoryDecoratorFactoryBase;
import io.katharsis.repository.decorate.ResourceRepositoryDecorator;
import io.katharsis.repository.decorate.ResourceRepositoryDecoratorBase;
import io.katharsis.resource.mock.models.Schedule;
import io.katharsis.resource.mock.repository.ScheduleRepository;

public class TestRepositoryDecorator extends RepositoryDecoratorFactoryBase {

	@SuppressWarnings("unchecked")
	@Override
	public <T, I extends Serializable> ResourceRepositoryDecorator<T, I> decorateRepository(
			ResourceRepositoryV2<T, I> repository) {
		if (repository.getResourceClass() == Schedule.class) {
			return (ResourceRepositoryDecorator<T, I>) new DecoratedScheduleRepository();
		}
		return null;
	}

	public static class DecoratedScheduleRepository extends ResourceRepositoryDecoratorBase<Schedule, Long>
			implements ScheduleRepository {

		@Override
		public ScheduleList findAll(QuerySpec querySpec) {
			return (ScheduleList) super.findAll(querySpec);
		}
	}
}
