package mipsAsm.gui;

import java.io.File;
import java.util.function.Predicate;

import javafx.application.Platform;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mipsAsm.disassembler.Disassembler;
import mipsAsm.simulator.Simulator;
import mipsAsm.util.BinaryType;
import mipsAsm.util.MenuHelper;

public class SimulatePerspective extends BorderPane
{
	private final Simulator simulator;
	private SimpleBooleanProperty outsideProgBound = new SimpleBooleanProperty(false);
	private SimpleBooleanProperty simRunning = new SimpleBooleanProperty(false);
	private SimThread simulatorThread = null;
	private boolean simInterrupted = false;
	
	private final Label[] sidePaneLabels;
	private final VBox sidePane;
	
	private final TabPane bottomPane;
	private final MemoryEditPane memPane;
	private final RegisterEditPane regPane;

	private final TextArea disassemblyContent;
	
	private final MenuBar menuBar;
	private final Menu[] menus;
	private final MenuItem[][] menuItems;
	
	private static final Alert disassemblePrompt = new Alert(AlertType.ERROR, "Wrong file format for binary.", ButtonType.OK);
	private static final Alert simPrompt = new Alert(AlertType.INFORMATION, "Program counter has reached the boundary of program data."
			+ " The run operation will be unavailable from now on.", ButtonType.OK);
	
	public SimulatePerspective()
	{
		super();
		this.menuBar = new MenuBar();
		this.menus = new Menu[3];
		this.menuItems = new MenuItem[3][];
		this.buildMenus();
		
		this.simulator = new Simulator();

		this.sidePaneLabels = new Label[5];
		for(int i = 0; i < 5; i++)
		{
			this.sidePaneLabels[i] = new Label();
			this.sidePaneLabels[i].fontProperty().bind(GUIMain.instance.editorFont());
		}
		this.sidePane = new VBox(this.sidePaneLabels);
		this.sidePane.setPadding(new Insets(10, 10, 10, 10));
		
		this.bottomPane = new TabPane();
		this.memPane = new MemoryEditPane(this.simulator.mem);
		this.regPane = new RegisterEditPane(this.simulator.reg);
		this.bottomPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		this.bottomPane.getTabs().addAll(new Tab("Memory", this.memPane), new Tab("Register", this.regPane));
		
		this.disassemblyContent = new TextArea();
		this.disassemblyContent.fontProperty().bind(GUIMain.instance.editorFont());
		this.disassemblyContent.setEditable(false);
		this.disassemblyContent.setOnKeyPressed(e -> {
			if(e.getCode().isFunctionKey())
				this.fireEvent(e);
		});
		
		GUIMain.instance.endianess().addListener((e, oldVal, newVal) -> this.simulator.mem.setEndianess(newVal));
		GUIMain.instance.endianess().addListener((e, oldVal, newVal) -> this.menuItems[2][0].setText(newVal? "Endian:big": "Endian:little"));

		this.outsideProgBound.addListener((e, oldVal, newVal) -> { if(!oldVal && newVal) simPrompt.showAndWait(); });
		this.menuItems[1][1].disableProperty().bind(this.outsideProgBound.or(this.simRunning));
		this.menuItems[0][0].disableProperty().bind(this.simRunning);
		this.menuItems[0][2].disableProperty().bind(this.simRunning);
		this.menuItems[1][0].disableProperty().bind(this.simRunning);
		this.menuItems[1][2].disableProperty().bind(this.simRunning.not());
		this.menuItems[1][4].disableProperty().bind(this.simRunning);
		this.menuItems[2][0].disableProperty().bind(this.simRunning);
		
		this.setTop(this.menuBar);
		this.setCenter(this.disassemblyContent);
		this.setRight(this.sidePane);
		this.setBottom(this.bottomPane);
		this.redraw();
	}
	
	private void buildMenus()
	{
		this.menuItems[0] = new MenuItem[3];
		this.menuItems[0][0] = MenuHelper.item("Open", e -> this.onOpen(), "O", KeyCombination.SHORTCUT_DOWN);
		this.menuItems[0][1] = new SeparatorMenuItem();
		this.menuItems[0][2] = MenuHelper.item("Quit", e -> this.onQuit(), "Q", KeyCombination.SHORTCUT_DOWN);
		this.menus[0] = MenuHelper.menu("File", this.menuItems[0], "F");
		
		this.menuItems[1] = new MenuItem[5];
		this.menuItems[1][0] = MenuHelper.item("Step", e -> this.onStep(), KeyCode.F5);
		this.menuItems[1][1] = MenuHelper.item("Run", e -> this.onRun(), KeyCode.F6);
		this.menuItems[1][2] = MenuHelper.item("Stop", e -> this.onStop(), KeyCode.F7);
		this.menuItems[1][2].setDisable(true);
		this.menuItems[1][3] = new SeparatorMenuItem();
		this.menuItems[1][4] = MenuHelper.item("Restart", e -> this.onRestart(), "R", KeyCombination.SHORTCUT_DOWN);
		this.menus[1] = MenuHelper.menu("Simulate", this.menuItems[1], "S");
		
		this.menuItems[2] = new MenuItem[1];
		this.menuItems[2][0] = MenuHelper.item("Endian:little", e -> this.onEndianessSelect());
		this.menus[2] = MenuHelper.menu("Options", this.menuItems[2], "O");
		
		this.menuBar.getMenus().addAll(this.menus);
		
	}
	
	protected void loadProgram(int[] program)
	{
		if(this.simulatorThread != null) return;
		this.simulator.loadProgram(program, 0);
		this.outsideProgBound.set(Simulator.pcOutofRange.test(this.simulator));
		
		this.disassemblyContent.setText(Disassembler.disassemble(program).toString());
		this.redraw();
	}
	
	private void redraw()
	{
		this.memPane.redraw();
		this.regPane.redraw();
		this.sidePaneLabels[0].setText(String.format("PC:                  %08x", this.simulator.getPC()));
		this.sidePaneLabels[1].setText(String.format("Next instruction:    %08x", this.simulator.getCurrInstruction()));
		String disassemble = Disassembler.disassemble(this.simulator.getCurrInstruction());
		if(disassemble.startsWith("."))
			this.sidePaneLabels[2].setText("  Unrecognized instruction");
		else
			this.sidePaneLabels[2].setText("  " + disassemble);
		this.sidePaneLabels[3].setText("Last exception:");
		this.sidePaneLabels[4].setText(this.simulator.getLastException() == null? "  None": "  " + this.simulator.getLastException().name());
	}
	
	private boolean onOpen()
	{
		File f = GUIMain.instance.promptOpenBinary();
		if(f == null) return false;
		int[] binary = BinaryType.read(f, GUIMain.instance.getEndianess());
		if(binary == null)
		{
			disassemblePrompt.showAndWait();
			return false;
		}
		this.loadProgram(binary);
		return true;
	}
	
	private void onQuit()
	{
		GUIMain.instance.endSimulation();
	}
	
	private void onStep()
	{
		this.simulator.step();
		this.outsideProgBound.set(Simulator.pcOutofRange.test(this.simulator));
		this.redraw();
	}
	
	private void onRun()
	{
		this.simInterrupted = false;
		this.simRunning.set(true);
		this.simulatorThread = new SimThread();
		this.simulatorThread.start();
	}
	
	private void onStop()
	{
		this.simInterrupted = true;
	}
	
	private void onRestart()
	{
		this.simulator.resetSimProgress();
		this.outsideProgBound.set(Simulator.pcOutofRange.test(this.simulator));
		this.redraw();
	}
	
	private void onSimEnd()//called when the simulation thread stops
	{
		this.outsideProgBound.set(Simulator.pcOutofRange.test(this.simulator));
		this.simRunning.set(false);
		this.redraw();
		this.simulatorThread = null;
	}
	
	private void onEndianessSelect()
	{
		GUIMain.instance.setEndianess(!GUIMain.instance.getEndianess());
	}
	
	private class SimThread extends Thread
	{
		private final Predicate<Simulator> interrupted = sim -> simInterrupted;
		
		@Override
		public void run()
		{
			simulator.runUntil(this.interrupted.or(Simulator.exceptionOccur).or(Simulator.pcOutofRange));
			Platform.runLater(() -> onSimEnd());
		}
	}
}
