package io.katharsis.internal.boot;

import java.util.concurrent.Callable;

public interface TransactionRunner {

	public <T> T doInTransaction(Callable<T> callable);
}
