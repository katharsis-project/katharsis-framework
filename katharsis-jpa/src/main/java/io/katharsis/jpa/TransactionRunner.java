package io.katharsis.jpa;

import java.util.concurrent.Callable;

public interface TransactionRunner {

	public <T> T doInTransaction(Callable<T> callable);
}
