package io.katharsis.jpa;

import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.core.internal.boot.KatharsisBoot;
import io.katharsis.core.internal.boot.ReflectionsServiceDiscovery;
import io.katharsis.core.internal.exception.ExceptionMapperRegistry;
import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.exception.BadRequestException;
import io.katharsis.jpa.internal.PersistenceExceptionMapper;
import io.katharsis.jpa.internal.PersistenceRollbackExceptionMapper;
import io.katharsis.jpa.internal.TransactionRollbackExceptionMapper;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.repository.response.HttpStatus;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;

public class JpaExceptionMapperTests {

	private KatharsisBoot boot;

	@Before
	public void setup() {
		boot = new KatharsisBoot();
		boot.addModule(JpaModule.newClientModule());
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery("io.katharsis.jpa.mock.model", new SampleJsonServiceLocator()));
		boot.boot();
	}

	@Test
	public void testPersistenceException() {
		PersistenceException exception = new PersistenceException(new BadRequestException("test"));
		ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
		PersistenceExceptionMapper mapper = (PersistenceExceptionMapper) exceptionMapperRegistry.findMapperFor(PersistenceException.class).get();
		ErrorResponse response = mapper.toErrorResponse(exception);
		ErrorData errorData = response.getErrors().iterator().next();
		Assert.assertEquals(Integer.toString(HttpStatus.BAD_REQUEST_400), errorData.getStatus());
		Assert.assertEquals("test", errorData.getDetail());
	}

	@Test
	public void testPersistenceRollbackException() {
		javax.persistence.RollbackException exception = new javax.persistence.RollbackException(new BadRequestException("test"));
		ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
		PersistenceRollbackExceptionMapper mapper = (PersistenceRollbackExceptionMapper) exceptionMapperRegistry.findMapperFor(javax.persistence.RollbackException.class).get();
		ErrorResponse response = mapper.toErrorResponse(exception);
		ErrorData errorData = response.getErrors().iterator().next();
		Assert.assertEquals(Integer.toString(HttpStatus.BAD_REQUEST_400), errorData.getStatus());
		Assert.assertEquals("test", errorData.getDetail());
	}
	
	@Test
	public void testTransactionRollbackException() {
		javax.transaction.RollbackException exception = new javax.transaction.RollbackException(){
			public Throwable getCause(){
				return new BadRequestException("test");
			}
		};
		ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
		TransactionRollbackExceptionMapper mapper = (TransactionRollbackExceptionMapper) exceptionMapperRegistry.findMapperFor(exception.getClass()).get();
		ErrorResponse response = mapper.toErrorResponse(exception);
		ErrorData errorData = response.getErrors().iterator().next();
		Assert.assertEquals(Integer.toString(HttpStatus.BAD_REQUEST_400), errorData.getStatus());
		Assert.assertEquals("test", errorData.getDetail());
	}
}
