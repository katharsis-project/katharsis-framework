package io.katharsis.core.internal.boot;

import java.util.concurrent.Callable;

public interface TransactionRunner {

	public <T> T doInTransaction(Callable<T> callable);
}
