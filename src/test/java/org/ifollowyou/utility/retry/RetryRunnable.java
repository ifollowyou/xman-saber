package org.ifollowyou.utility.retry;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryRunnable implements Runnable {

    public void run() {
        try {
            log.info("Sleeping...");
            Thread.sleep(2000);

            throw new RuntimeException("RetryException");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
