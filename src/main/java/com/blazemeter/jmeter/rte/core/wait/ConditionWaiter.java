package com.blazemeter.jmeter.rte.core.wait;

import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.listener.ExceptionListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConditionWaiter<T extends WaitCondition> implements ExceptionListener {

  private static final Logger LOG = LoggerFactory.getLogger(ConditionWaiter.class);

  protected final T condition;
  protected AtomicBoolean conditionState = new AtomicBoolean(false);
  private final CountDownLatch lock = new CountDownLatch(1);
  private final ScheduledExecutorService stableTimeoutExecutor;
  private ExceptionHandler exceptionHandler;
  private ScheduledFuture stableTimeoutTask;
  private boolean ended;

  public ConditionWaiter(T condition, ScheduledExecutorService stableTimeoutExecutor,
      ExceptionHandler exceptionHandler) {
    this.condition = condition;
    this.stableTimeoutExecutor = stableTimeoutExecutor;
    this.exceptionHandler = exceptionHandler;
    exceptionHandler.addListener(this);
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
    exceptionHandler.throwAnyPendingError();
    if (!lock.await(condition.getTimeoutMillis(), TimeUnit.MILLISECONDS)) {
      cancelWait();
      throw new TimeoutException(
          "Timeout waiting for " + condition.getDescription() + " after " + condition
              .getTimeoutMillis() + " millis. " +
              "Check if Timeout values of the 'Wait for' components " +
              "are greater than Stable time or Silent interval.");
    }
    exceptionHandler.throwAnyPendingError();
  }

  private synchronized void cancelWait() {
    ended = true;
    lock.countDown();
    endStablePeriod();
  }

  @Override
  public void onException(Throwable e) {
    if (exceptionHandler.hasPendingError()) {
      cancelWait();
    }
  }

  public void stop() {
    cancelWait();
    exceptionHandler.removeListener(this);
  }

  protected void validateCondition(String log1, String log2, String event) {
    //consider using method overload for the cases when there is no event.. like sync;
    if (conditionState.get() != getCurrentConditionState()) {
      conditionState.set(getCurrentConditionState());
      if (getCurrentConditionState()) {
        LOG.debug(log1 + " {}", event);
        startStablePeriod();
      } else {
        LOG.debug(log2 + " {}", event);
        endStablePeriod();
      }
    }
  }

  protected abstract boolean getCurrentConditionState();
}
