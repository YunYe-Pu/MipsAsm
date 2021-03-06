package mipsAsm.gui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
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
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.util.BitStream;
import mipsAsm.disassembler.Disassembler;
import mipsAsm.util.MenuHelper;

public class EditPerspective extends BorderPane
{
	public static GUIMain instance;
	
	private final MenuBar menuBar;
	private final Menu[] menus;
	private final MenuItem[][] menuItems;
	private final TabPane codeEditors;
	private final TextArea console;
	private final ByteArrayOutputStream consoleBuffer;
	private final Assembler assembler;
	
	private final SimpleBooleanProperty emptyTab = new SimpleBooleanProperty(true);
	private final SimpleBooleanProperty noSource = new SimpleBooleanProperty(true);
	private final SimpleBooleanProperty noCurrentSource = new SimpleBooleanProperty(true);
	
	private static final Alert assemblePrompt1 = new Alert(AlertType.CONFIRMATION, "There are unsaved modifications. Do you want to save them?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
	private static final Alert assemblePrompt2 = new Alert(AlertType.ERROR, "Unknown error occurred during assembly.", ButtonType.OK);
	private static final Alert assemblePrompt3 = new Alert(AlertType.ERROR, "Unable to open some of the assembly input files. Please verify whether they have changed on disk.", ButtonType.OK);
	private static final Alert disassemblePrompt = new Alert(AlertType.ERROR, "Wrong file format for binary.", ButtonType.OK);
	
	public EditPerspective()
	{
		super();
		this.menuBar = new MenuBar();
		this.menus = new Menu[4];
		this.menuItems = new MenuItem[4][];
		this.buildMenus();
		
		this.codeEditors = new TabPane();
		this.codeEditors.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
		this.console = new TextArea();
		this.console.setEditable(false);
		this.console.fontProperty().bind(GUIMain.instance.editorFont);
		this.console.setOnKeyPressed(e -> {
			if(e.getCode().isFunctionKey()) this.console.getParent().fireEvent(e);
		});
		this.consoleBuffer = new ByteArrayOutputStream();
		this.assembler = new Assembler(new PrintStream(this.consoleBuffer));
		Pane p1 = new Pane(this.codeEditors, this.console);
		this.codeEditors.prefHeightProperty().bind(p1.heightProperty().multiply(0.8f));
		this.codeEditors.prefWidthProperty().bind(p1.widthProperty());
		this.codeEditors.relocate(0, 0);
		this.console.prefHeightProperty().bind(p1.heightProperty().multiply(0.2f));
		this.console.prefWidthProperty().bind(p1.widthProperty());
		this.console.setLayoutX(0);
		this.console.layoutYProperty().bind(p1.heightProperty().multiply(0.8f));
		
		GUIMain.config.endian.addListener((e, oldVal, newVal) -> this.assembler.setEndianess(newVal));
		GUIMain.config.endian.addListener((e, oldVal, newVal) -> this.menuItems[3][0].setText(newVal? "Endian:big": "Endian:little"));
		
		this.setOnDragOver(e ->
		{
			if(e.getGestureSource() == this) return;
			if(e.getDragboard().hasFiles())
				e.acceptTransferModes(TransferMode.ANY);
		});
		
		this.setOnDragDropped(event ->
		{
			List<File> files = event.getDragboard().getFiles();
			if(files == null || files.isEmpty()) return;
			for(File f : files)
				this.openFile(f);
			this.onEditorTabChange();
			this.codeEditors.getSelectionModel().selectLast();
		});

		this.setTop(this.menuBar);
		this.setCenter(p1);
	}
	
	private void buildMenus()
	{
		BooleanExpression b1 = GUIMain.instance.simDisplayed;
		BooleanExpression b2 = b1.or(this.emptyTab);
		
		this.menuItems[0] = new MenuItem[9];
		this.menuItems[0][0] = MenuHelper.item("New", e -> this.onNewSource(), b1, "N", KeyCombination.SHORTCUT_DOWN);
		this.menuItems[0][1] = MenuHelper.item("Open", e -> this.onOpenSource(), b1, "O", KeyCombination.SHORTCUT_DOWN);
		this.menuItems[0][2] = MenuHelper.item("Save", e -> this.onSave(), b2, "S", KeyCombination.SHORTCUT_DOWN);
		this.menuItems[0][3] = MenuHelper.item("Save as", e -> this.onSaveAs(), b2, "S", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
		this.menuItems[0][4] = MenuHelper.item("Save all", e -> this.onSaveAll(), b2, "S", KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);
		this.menuItems[0][5] = MenuHelper.item("Reload", e -> this.onReload(), b1.or(this.noCurrentSource), "R", KeyCombination.SHORTCUT_DOWN);
		this.menuItems[0][6] = MenuHelper.item("Reload all", e -> this.onReloadAll(), b1.or(this.noSource), "R", KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);
		this.menuItems[0][7] = new SeparatorMenuItem();
		this.menuItems[0][8] = MenuHelper.item("Quit", e -> GUIMain.instance.closeWindow(), b1, "Q", KeyCombination.SHORTCUT_DOWN);
		this.menus[0] = MenuHelper.menu("File", this.menuItems[0], "F");
		
		this.menuItems[1] = new MenuItem[4];
		this.menuItems[1][0] = MenuHelper.item("Assemble current source", e -> this.onAssembleSingle(true), b2, KeyCode.F3);
		this.menuItems[1][1] = MenuHelper.item("Assemble all source", e -> this.onAssembleAll(true), b2, KeyCode.F4);
		this.menuItems[1][2] = new SeparatorMenuItem();
		this.menuItems[1][3] = MenuHelper.item("Disassemble", e -> this.onDisassemble(), b1, "D", KeyCombination.SHORTCUT_DOWN);
		this.menus[1] = MenuHelper.menu("Assemble", this.menuItems[1], "A");
		
		this.menuItems[2] = new MenuItem[3];
		this.menuItems[2][0] = MenuHelper.item("Simulate current project", e -> this.onSimCurrentProj(), b2);
		this.menuItems[2][1] = MenuHelper.item("Simulate current source", e -> this.onSimCurrentFile(), b2);
		this.menuItems[2][2] = MenuHelper.item("Open and simulate", e -> this.onSimNew(), b1);
		this.menus[2] = MenuHelper.menu("Simulate", this.menuItems[2], "S");
		
		this.menuItems[3] = new MenuItem[1];
		this.menuItems[3][0] = MenuHelper.item("Endian:little", e -> this.onEndianessSelect(), b1);
		this.menus[3] = MenuHelper.menu("Options", this.menuItems[3], "O");
		
		this.menuBar.getMenus().addAll(this.menus);
	}
	
	private BitStream assemble(File[] input)
	{
		try
		{
			BitStream asmResult = this.assembler.assemble(input);
			this.console.setText(this.consoleBuffer.toString());
			this.consoleBuffer.reset();
			return asmResult;
		}
		catch(FileNotFoundException e)
		{
			assemblePrompt3.showAndWait();
			return null;
		}
	}
	
	private boolean promptSaveAsmResult(BitStream asmResult)
	{
		try
		{
			File asmOutputFile = GUIMain.instance.promptSaveBinary();
			if(asmOutputFile != null)
				asmResult.write(asmOutputFile);
			return true;
		}
		catch(IOException e)
		{
			assemblePrompt2.showAndWait();
			return false;
		}
	}
	
	//Event handler callbacks
	
	private void onNewSource()
	{
		this.codeEditors.getTabs().add(new CodeEditorTab(null, e -> this.onEditorTabChange()));
		this.codeEditors.getSelectionModel().selectLast();
		this.onEditorTabChange();
		this.console.clear();
	}
	
	private boolean onOpenSource()
	{
		List<File> list = GUIMain.instance.promptOpenMultiSource();
		if(list == null) return false;
		if(list.isEmpty()) return false;
		for(File f : list)
			this.codeEditors.getTabs().add(new CodeEditorTab(f, e -> this.onEditorTabChange()));
		this.codeEditors.getSelectionModel().selectLast();
		this.onEditorTabChange();
		this.console.clear();
		return true;
	}
	
	private boolean onSave()
	{
		return ((CodeEditorTab)this.codeEditors.getSelectionModel().getSelectedItem()).save();	
	}
	
	private boolean onSaveAs()
	{
		return ((CodeEditorTab)this.codeEditors.getSelectionModel().getSelectedItem()).saveAs();
	}

	private boolean onSaveAll()
	{
		for(Tab t : this.codeEditors.getTabs())
			if(!((CodeEditorTab)t).save()) return false;
		return true;
	}
	
	private boolean onReload()
	{
		return ((CodeEditorTab)this.codeEditors.getSelectionModel().getSelectedItem()).reload();
	}
	
	private void onReloadAll()
	{
		for(Tab t : this.codeEditors.getTabs())
			((CodeEditorTab)t).reload();
	}
	
	protected void onEditorTabChange()
	{
		if(this.codeEditors.getTabs().isEmpty())
		{
			this.emptyTab.set(true);
			this.noSource.set(true);
			this.noCurrentSource.set(true);
			GUIMain.instance.setTitle("MIPS Assembler IDE");
		}
		else
		{
			this.emptyTab.set(false);
			boolean noSource = true;
			for(Tab t : this.codeEditors.getTabs())
			{
				if(((CodeEditorTab)t).file != null)
				{
					noSource = false;
					break;
				}
			}
			this.noSource.set(noSource);
			this.noCurrentSource.set(((CodeEditorTab)this.codeEditors.getSelectionModel().getSelectedItem()).file == null);
			GUIMain.instance.setTitle("MIPS Assembler IDE - " + this.codeEditors.getSelectionModel().getSelectedItem().getText());
		}
	}
	
	private BitStream onAssembleSingle(boolean save)
	{
		CodeEditorTab e = (CodeEditorTab)this.codeEditors.getSelectionModel().getSelectedItem();
		if(e.save())
		{
			File[] input = new File[1];
			input[0] = e.file;
			BitStream result = this.assemble(input);
			if(result != null && save)
				this.promptSaveAsmResult(result);
			return result;
		}
		else
			return null;
	}
	
	private BitStream onAssembleAll(boolean save)
	{
		for(Tab t : this.codeEditors.getTabs())
		{
			if(((CodeEditorTab)t).hasContentChanged())
			{
				Optional<ButtonType> result = assemblePrompt1.showAndWait();
				if(result.isPresent() && result.get() == ButtonType.YES)
				{
					if(this.onSaveAll())
						break;
					else
						return null;
				}
				else
					return null;
			}
		}
		File[] input = new File[this.codeEditors.getTabs().size()];
		for(int i = 0; i < input.length; i++)
			input[i] = ((CodeEditorTab)this.codeEditors.getTabs().get(i)).file;
		BitStream result = this.assemble(input);
		if(result != null && save)
			this.promptSaveAsmResult(result);
		return result;
	}
	
	private boolean onDisassemble()
	{
		File f = GUIMain.instance.promptOpenBinary();
		if(f == null) return false;
		try
		{
			int[] data = BitStream.read(f, GUIMain.config.endian.get());
			if(data == null)
			{
				disassemblePrompt.showAndWait();
				return false;
			}
			StringBuilder str = Disassembler.disassemble(data);
			this.codeEditors.getTabs().add(new CodeEditorTab(str.toString(), "Disassembled", e -> this.onEditorTabChange()));
			this.codeEditors.getSelectionModel().selectLast();
			this.onEditorTabChange();
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
	}
	
	private boolean onSimCurrentProj()
	{
		BitStream s = this.onAssembleAll(false);
		if(s == null) return false;
		return GUIMain.instance.startSimulation(s.getData());
	}
	
	private boolean onSimCurrentFile()
	{
		BitStream s = this.onAssembleSingle(false);
		if(s == null) return false;
		return GUIMain.instance.startSimulation(s.getData());
	}
	
	private boolean onSimNew()
	{
		File f = GUIMain.instance.promptOpenBinary();
		if(f == null) return false;
		return GUIMain.instance.startSimulation(f);
	}
	
	private void onEndianessSelect()
	{
		GUIMain.config.endian.set(!GUIMain.config.endian.get());
	}
	
	protected boolean openFile(File file)
	{
		if(!file.exists())
			return false;
		String extension = file.getName();
		int i = extension.lastIndexOf('.');
		if(i >= 0)
			extension = extension.substring(i);
		if(!".s".equalsIgnoreCase(extension)) return false;
		this.codeEditors.getTabs().add(new CodeEditorTab(file, e -> this.onEditorTabChange()));
		this.onEditorTabChange();
		return true;
	}
	
	protected boolean closeAllTabs()
	{
		for(Tab t : this.codeEditors.getTabs())
		{
			if(((CodeEditorTab)t).hasContentChanged())
			{
				Optional<ButtonType> result = assemblePrompt1.showAndWait();
				if(result.isPresent())
				{
					if(result.get() == ButtonType.YES)
						return this.onSaveAll();
					else if(result.get() == ButtonType.NO)
						return true;
					else
						return false;
				}
			}
		}
		return true;
	}
	
}
