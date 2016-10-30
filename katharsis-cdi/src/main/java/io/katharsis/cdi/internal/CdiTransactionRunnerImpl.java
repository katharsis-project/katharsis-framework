package io.katharsis.cdi.internal;

import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@Transactional // does not seem to work on method-level
@ApplicationScoped
public class CdiTransactionRunnerImpl {

	public <T> T doInTransaction(Callable<T> callable) {
		try {
			return callable.call();
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
