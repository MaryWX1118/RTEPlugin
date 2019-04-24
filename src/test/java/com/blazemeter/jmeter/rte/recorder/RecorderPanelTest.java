package com.blazemeter.jmeter.rte.recorder;

import kg.apc.emulators.TestJMeterUtils;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.junit.Before;
import org.junit.BeforeClass;

import static com.mongodb.util.MyAsserts.assertTrue;
import static org.assertj.swing.fixture.Containers.showInFrame;


public class RecorderPanelTest {
  private FrameFixture frame;
  private JTableFixture inputTable;
  private RTERecorderGui RecPanelGui;

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Before
  public void setup() {
    RecPanelGui = new RTERecorderGui();
    frame = showInFrame(RecPanelGui);
    inputTable = frame.table("table");
  }
  
}