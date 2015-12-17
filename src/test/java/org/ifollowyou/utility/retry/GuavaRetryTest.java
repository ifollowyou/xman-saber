package org.ifollowyou.utility.retry;


import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GuavaRetryTest {

    @Test
    public void testRetry() {
        Callable<Boolean> yourTask = new Callable<Boolean>() {
            public Boolean call() throws Exception {

                if (true) {
                    log.info("Throw a runtime exception...");
                    throw new RuntimeException("Retry test");
                }

                return true; // do something interesting here
            }
        };

        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(Predicates.<Boolean>isNull())
                .retryIfExceptionOfType(RuntimeException.class)
                .withWaitStrategy(WaitStrategies.fixedWait(2000, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(5))
                .build();

        try {
            retryer.call(yourTask);
        } catch (Exception e) {
            log.error("Failed to call!", e);
        }
    }
}
