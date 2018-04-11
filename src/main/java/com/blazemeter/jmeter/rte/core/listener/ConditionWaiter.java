package com.blazemeter.jmeter.rte.core.listener;

import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class ConditionWaiter<T extends WaitCondition> {

  protected final T condition;
  private final CountDownLatch lock = new CountDownLatch(1);
  private final ScheduledExecutorService stableTimeoutExecutor;
  private ScheduledFuture stableTimeoutTask;
  private boolean ended;

  public ConditionWaiter(T condition, ScheduledExecutorService stableTimeoutExecutor) {
    this.condition = condition;
    this.stableTimeoutExecutor = stableTimeoutExecutor;
  }

  protected synchronized void cancelWait() {
    ended = true;
    lock.countDown();
    endStablePeriod();
  }

  protected synchronized void startStablePeriod() {
    if (ended) {
      return;
    }
    endStablePeriod();
    stableTimeoutTask = stableTimeoutExecutor
        .schedule(lock::countDown, condition.getStableTimeoutMillis(), TimeUnit.MILLISECONDS);
  }

  protected synchronized void endStablePeriod() {
    if (stableTimeoutTask != null) {
      stableTimeoutTask.cancel(false);
    }
  }

  public void await() throws InterruptedException, TimeoutException, RteIOException {
    if (!lock.await(condition.getTimeoutMillis(), TimeUnit.MILLISECONDS)) {
      cancelWait();
      throw new TimeoutException(
          "Timeout waiting for " + condition.getDescription() + " after " + condition
              .getTimeoutMillis() + " millis. " +
              "Check if Timeout values of the 'Wait for' components " +
              "are greater than Stable time or Silent interval.");
    }
  }

  public void stop() {
    cancelWait();
  }

}
