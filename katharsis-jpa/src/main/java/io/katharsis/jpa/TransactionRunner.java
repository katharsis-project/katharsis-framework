package io.katharsis.jpa;

import java.util.concurrent.Callable;

@Deprecated
/**
 * @Deprecated use io.katharsis.internal.boot.TransactionRunner
 */
public interface TransactionRunner extends io.katharsis.core.internal.boot.TransactionRunner {

	@Override
	public <T> T doInTransaction(Callable<T> callable);
}
