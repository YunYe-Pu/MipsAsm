package mipsAsm.gui;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mipsAsm.disassembler.Disassembler;
import mipsAsm.simulator.Simulator;
import mipsAsm.util.BinaryType;
import mipsAsm.util.MenuHelper;

public class SimulatePerspective extends BorderPane
{
	private final Simulator simulator;
	
//	public int simRunLevel;
	
	private final TabPane mainPane;
	private final Label statusLabel;

	private final BorderPane debugPane;
	private final TextArea monitor;
	

//	private SimpleBooleanProperty outsideProgBound = new SimpleBooleanProperty(false);
	private SimpleBooleanProperty simRunning = new SimpleBooleanProperty(false);
	private boolean simInterrupted = false;
	private SimThread simulatorThread;
	private MonitorRedrawThread redrawThread;
//	private boolean simInterrupted = false;
	
//	private final Label[] sidePaneLabels;
//	private final VBox sidePane;
	
	//GUI components in debug pane
	private final TabPane bottomPane;
	private final MemoryEditPane memPane;
	private final RegisterEditPane regPane;
	private final Cp0DisplayPane cp0Pane;

	private final TextArea disassemblyContent;
	
	private final VBox sidePane;
	private final Label[] sidePaneLabels;
	
	private final MenuBar menuBar;
	private final Menu[] menus;
	private final MenuItem[][] menuItems;
	
	private static final Alert disassemblePrompt = new Alert(AlertType.ERROR, "Wrong file format for binary.", ButtonType.OK);
//	private static final Alert simPrompt = new Alert(AlertType.INFORMATION, "Program counter has reached the boundary of program data."
//			+ " The run operation will be unavailable from now on.", ButtonType.OK);
	private static final TextInputDialog offsetInputDialog = new TextInputDialog("0");

	static
	{
		offsetInputDialog.setTitle("Enter program offset");
		offsetInputDialog.setHeaderText("Input the program offset,\nin hexadecimal byte address:");
	}
	
	public SimulatePerspective()
	{
		super();
		this.menuBar = new MenuBar();
		this.menus = new Menu[2];
		this.menuItems = new MenuItem[2][];
		this.buildMenus();
		
		this.simulator = new Simulator();
		
		this.statusLabel = new Label("Paused");
		this.statusLabel.setMinHeight(20);
		this.statusLabel.setPadding(new Insets(0, 5, 2, 5));

		this.sidePaneLabels = new Label[5];
		for(int i = 0; i < 5; i++)
		{
			this.sidePaneLabels[i] = new Label();
			this.sidePaneLabels[i].fontProperty().bind(GUIMain.instance.editorFont);
		}
		this.sidePane = new VBox(this.sidePaneLabels);
		this.sidePane.setPadding(new Insets(10, 10, 10, 10));
		
		this.bottomPane = new TabPane();
		this.memPane = new MemoryEditPane(this.simulator.mem);
		this.regPane = new RegisterEditPane(this.simulator.gpr);
		this.cp0Pane = new Cp0DisplayPane(this.simulator.cp0);
		this.bottomPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		this.bottomPane.getTabs().addAll(
				new Tab("Memory", this.memPane),
				new Tab("Register", this.regPane),
				new Tab("Cop 0", this.cp0Pane));
		
		this.disassemblyContent = new TextArea();
		this.disassemblyContent.fontProperty().bind(GUIMain.instance.editorFont);
		this.disassemblyContent.setEditable(false);
		this.disassemblyContent.setOnKeyPressed(e -> {
			if(e.getCode().isFunctionKey())
				this.fireEvent(e);
		});
		
		this.monitor = new TextArea();
		this.monitor.fontProperty().bind(GUIMain.instance.editorFont);
		this.monitor.setEditable(false);
		this.monitor.setOnKeyPressed(e -> this.onMonitorKeyPress(e));
//		this.monitor.disableProperty().bind(this.simRunning.not());
		
		this.debugPane = new BorderPane();
		this.debugPane.setCenter(this.disassemblyContent);
		this.debugPane.setRight(this.sidePane);
		this.debugPane.setBottom(this.bottomPane);
//		this.debugPane.disableProperty().bind(this.simRunning);
		
		this.mainPane = new TabPane();
		this.mainPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		this.mainPane.getTabs().addAll(new Tab("Monitor", this.monitor), new Tab("Debug", this.debugPane));
		
		this.setTop(this.menuBar);
		this.setCenter(this.mainPane);
		this.setBottom(this.statusLabel);
		this.redraw();
	}
	
	private void buildMenus()
	{
		BooleanExpression b1 = GUIMain.instance.editDisplayed;
		BooleanExpression b2 = b1.or(this.simRunning);
		this.menuItems[0] = new MenuItem[3];
		this.menuItems[0][0] = MenuHelper.item("Open", e -> this.onOpen(), b2, "O", KeyCombination.SHORTCUT_DOWN);
		this.menuItems[0][1] = new SeparatorMenuItem();
		this.menuItems[0][2] = MenuHelper.item("Quit", e -> this.onQuit(), b2, "Q", KeyCombination.SHORTCUT_DOWN);
		this.menus[0] = MenuHelper.menu("File", this.menuItems[0], "F");
		
		this.menuItems[1] = new MenuItem[6];
		this.menuItems[1][0] = MenuHelper.item("Step", e -> this.onStep(), b2, KeyCode.F5);
		this.menuItems[1][1] = MenuHelper.item("Run", e -> this.onRun(), b2, KeyCode.F6);
		this.menuItems[1][2] = MenuHelper.item("Pause", e -> this.onPause(), b1.or(this.simRunning.not()), KeyCode.F7);
//		this.menuItems[1][2].setDisable(true);
		this.menuItems[1][3] = new SeparatorMenuItem();
		this.menuItems[1][4] = MenuHelper.item("Reset", e -> this.onReset(), b1, "R", KeyCombination.SHORTCUT_DOWN);
		this.menuItems[1][5] = MenuHelper.item("Clear", e -> this.onClear(), b2);
		
		this.menus[1] = MenuHelper.menu("Simulate", this.menuItems[1], "S");
		
//		this.menuItems[2] = new MenuItem[1];
//		this.menuItems[2][0] = MenuHelper.item("Endian:little", e -> this.onEndianessSelect(), b2);
//		this.menus[2] = MenuHelper.menu("Options", this.menuItems[2], "O");
		
		this.menuBar.getMenus().addAll(this.menus);
		
	}
	
	protected boolean loadProgram(int[] program)
	{
		if(this.simulatorThread != null) return false;
		Optional<String> result = offsetInputDialog.showAndWait();
		if(!result.isPresent()) return false;
		int offset = 0;
		try
		{
			offset = Integer.parseUnsignedInt(result.get(), 16);
		}
		catch(NumberFormatException e)
		{
			offset = 0;
		}
		this.simulator.clear();
		this.simulator.mem.loadData(program, offset);
		
		this.disassemblyContent.setText(Disassembler.disassemble(program).toString());
		this.redraw();
		return true;
	}
	
	private void redraw()
	{
		this.memPane.redraw();
		this.regPane.redraw();
		this.cp0Pane.redraw();
		this.sidePaneLabels[0].setText(String.format("PC:  %08x", this.simulator.getPC()));
		this.sidePaneLabels[1].setText(String.format("HI:  %08x", this.simulator.regHI));
		this.sidePaneLabels[2].setText(String.format("LO:  %08x", this.simulator.regLO));
	}

	private String drawMonitor()
	{
		final int VRAM_ADDR = 0xb0000000;
		final int ROWS = 80;
		final int COLS = 30;
		final int ROW_SPACING = 48;//128 - 80
		
		int x, y, addr, c;
		addr = (VRAM_ADDR >> 2) & 0x3fffffff;
		int[] page = simulator.mem.getCreatePage(addr << 2);
		StringBuilder buffer = new StringBuilder(COLS * (ROWS + 1));
		for(y = 0; y < COLS; y++)
		{
			for(x = 0; x < ROWS; x++)
			{
				c = page[addr & 0x3ff] & 0xff;
				if(c >= 0x20 && c < 0x7f)
					buffer.append((char)c);
				else
					buffer.append(' ');
				addr++;
			}
			buffer.append('\n');
			addr += ROW_SPACING;
			if((addr & 0x3ff) == 0)
				page = simulator.mem.getCreatePage(addr << 2);
		}
		buffer.setLength(buffer.length() - 1);
		return buffer.toString();
	}
	
	private boolean onOpen()
	{
		File f = GUIMain.instance.promptOpenBinary();
		if(f == null) return false;
		try
		{
			int[] binary = BinaryType.read(f, GUIMain.instance.endianess.get());
			if(binary == null)
			{
				disassemblePrompt.showAndWait();
				return false;
			}
			return this.loadProgram(binary);
		}
		catch(IOException e)
		{
			return false;
		}
	}
	
	private void onQuit()
	{
		this.simulator.clear();
		GUIMain.instance.endSimulation();
	}
	
	private void onStep()
	{
		this.simulator.step();
		this.redraw();
	}
	
	private void onRun()
	{
		this.simRunning.set(true);
		this.simInterrupted = false;
		this.simulatorThread = new SimThread();
		this.redrawThread = new MonitorRedrawThread();
		this.simulatorThread.start();
		this.redrawThread.start();
	}
	
	private void onPause()
	{
		this.simInterrupted = true;
		this.simRunning.set(false);
		this.statusLabel.setText("Paused");
		this.redraw();
	}
	
	private void onReset()
	{
		this.simulator.signalColdReset();
	}
	
	private void onClear()
	{
		this.simulator.clear();
	}
	
	private void onMonitorKeyPress(KeyEvent event)
	{
		//TODO
	}
	
	private class SimThread extends Thread
	{
		private long timestamp = System.nanoTime();
		private long instCounter = 0;
		
		@Override
		public void run()
		{
			while(!simInterrupted)
			{
				simulator.step();
				this.instCounter++;
			}
		}
		
		public double getPerformance()//Performance in million instructions per second
		{
			long t1 = System.nanoTime();
			long instCnt = instCounter;
			instCounter = 0;
			double ret = instCnt / ((t1 - timestamp) / 1_000D);
			timestamp = t1;
			return ret;
		}
	}
	
	private class MonitorRedrawThread extends Thread
	{
		@Override
		public void run()
		{
			int cnt = 20;
			while(!simInterrupted)
			{
				try
				{
					String str = drawMonitor();
					Platform.runLater(() -> monitor.setText(str));
					cnt++;
					if(cnt >= 25)
					{
						Platform.runLater(() -> {
							String s = String.format("Running at %.2f MIPS", simulatorThread.getPerformance());
							statusLabel.setText(s);
						});
						cnt = 0;
					}
					Thread.sleep(20);
				}
				catch(InterruptedException e) {}
			}
		}
		
	}
}
