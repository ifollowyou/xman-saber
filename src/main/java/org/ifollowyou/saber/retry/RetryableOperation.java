package org.ifollowyou.saber.retry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Class that provides retrying functionality. Example:
 * <p></p>
 * <code>
 * Callable<String> callable = new Callable<String>() {..};
 * String result = RetryableOperation.create(callable).retry(5, IOException.class);
 * </code>
 *
 * @param <T> the return type of the operation
 */
public class RetryableOperation<T> {
    private Callable<T> callable;
    private Runnable runnable;
    private boolean exponentialBackoff;
    private int backoffInterval = 500;

    /**
     * Create a retryable operation based on a Callable instance. The return
     * type of retry(..) is the type parameter of the Callable instance.
     *
     * @param callable
     * @return a new instance of RetryableOperation
     */
    public static <T> RetryableOperation<T> create(Callable<T> callable) {
        return new RetryableOperation<T>().withCallable(callable);
    }

    /**
     * Creates a retryable operation based on a Runnable instance. In this case
     * the retry(..) method always returns null.
     *
     * @param runnable A Runnable Task
     * @return a new instance of RetryableOperation
     */
    public static RetryableOperation create(Runnable runnable) {
        return new RetryableOperation().withRunnable(runnable);
    }

    /**
     * Retries the operation. Retrying happens regardless of the exception thrown.
     *
     * @param retries    number of retries before the exception is thrown to the caller
     * @param exceptions the operation will be retried only if the exception that occurs is one of the
     *                   exceptions passed in this array
     * @return the result of the operation (null if Runnable is used instead of Callable)
     * @throws Exception the exception that occurred on the last attempt
     */
    public T retry(int retries, Class<? extends Exception>... exceptions) throws Exception {
        if (callable == null && runnable == null) {
            throw new IllegalStateException("Either runnable or callable must be set");
        }
        Set<Class<? extends Exception>> retryFor = new HashSet<>();
        retryFor.addAll(Arrays.asList(exceptions));
        for (int i = 0; i < retries; i++) {
            try {
                if (exponentialBackoff && i > 0) {
                    int sleepTime = (int) ((Math.pow(2, i) - 1) / 2) * backoffInterval;
                    Thread.sleep(sleepTime);
                }
                if (callable != null) {
                    return callable.call();
                } else if (runnable != null) {
                    runnable.run();
                    return null;
                }
            } catch (Exception e) {
                if (retryFor.isEmpty() || retryFor.contains(e.getClass())) {
                    if (i == retries - 1) {
                        throw e;
                    }
                } else {
                    // if the exception is not the expected one, throw it immediately
                    throw e;
                }
            }
        }
        // can't be reached - in case of failure on the last iteration the exception is rethrown
        return null;
    }


    private RetryableOperation<T> withCallable(Callable<T> callable) {
        this.callable = callable;
        return this;
    }

    private RetryableOperation<T> withRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    public RetryableOperation<T> withExponentialBackoff() {
        this.exponentialBackoff = true;
        return this;
    }
}
