package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisibleCursorListener extends Tn3270ConditionWaiter<CursorWaitCondition> implements
    CursorMoveListener {

  private static final Logger LOG = LoggerFactory.getLogger(VisibleCursorListener.class);

  public VisibleCursorListener(CursorWaitCondition condition, Tn3270Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    client.addCursorMoveListener(this);
    if (getCurrentConditionState()) {
      LOG.debug(CursorWaitCondition.EXPECTED_CURSOR_POSITION);
      startStablePeriod();
      conditionState.set(true);
    }
  }

  private Position getCursorPosition() {
    return client.getCursorPosition().orElse(null);
  }

  @Override
  public void cursorMoved(int i, int i1, Field field) {
    validateCondition(CursorWaitCondition.EXPECTED_CURSOR_POSITION,
        CursorWaitCondition.NOT_EXPECTED_CURSOR_POSITION, "cursor moved");
  }

  @Override
  public void stop() {
    super.stop();
    client.removeCursorMoveListener(this);
  }

  @Override
  protected boolean getCurrentConditionState() {
    return condition.getPosition().equals(getCursorPosition());
  }

}
