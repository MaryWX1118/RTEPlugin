package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;

public class SilenceListenerIT extends ConditionWaiterIT {

  private Stopwatch waitTime;

  @Override
  protected ConditionWaiter<?> buildConditionWaiter() {
    waitTime = Stopwatch.createStarted();
    return new SilenceListener(new SilentWaitCondition(TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor);
  }

  @Test
  public void shouldUnblockAfterSilentTimeWhenNoEvents() throws Exception {
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(STABLE_MILLIS);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenKeepReceivingChanges()
      throws Exception {
    startPeriodicEventGenerator(buildStateChangeGenerator());
    listener.await();
  }

}
