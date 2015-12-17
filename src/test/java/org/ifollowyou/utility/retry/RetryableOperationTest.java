package org.ifollowyou.utility.retry;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;

/**
 * RetryableOperation Tester.
 *
 * @author xman
 * @version 1.0
 * @since 2015-10-16
 */
@Slf4j
public class RetryableOperationTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: create(Callable<T> callable)
     */
    @Test
    public void testCreateCallable() throws Exception {
        Callable<String> callable = new Callable<String>() {
            public String call() throws Exception {
                return null;
            }
        };

        RetryableOperation.create(callable).retry(3);
    }

    /**
     * Method: create(Runnable runnable)
     */
    @Test
    public void testCreateRunnable() throws Exception {
        Runnable runnable = new RetryRunnable();
        RetryableOperation.create(runnable);
    }

    /**
     * Method: retry(int retries, Class<? extends Exception>... exceptions)
     */
    @Test
    public void testRetry() throws Exception {
        Runnable runnable = new RetryRunnable();
        RetryableOperation.create(runnable).retry(3, RuntimeException.class);
    }

    /**
     * Method: withExponentialBackoff()
     */
    @Test
    public void testWithExponentialBackoff() throws Exception {
        Runnable runnable = new RetryRunnable();
        RetryableOperation.create(runnable)
                .withExponentialBackoff()
                .retry(3);
    }


} 
