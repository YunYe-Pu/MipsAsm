package mipsAsm.gui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import mipsAsm.assembler.util.BitStream;
import mipsAsm.util.Config;
import mipsAsm.util.FileFormat;

public class GUIMain extends Application
{
	public static GUIMain instance;
	private Stage primaryStage;
	private static File[] initialFile;
	protected static Config config;

	protected final SimpleObjectProperty<Font> editorFont;
	protected final SimpleBooleanProperty editDisplayed;
	protected final BooleanExpression simDisplayed;
	
	private final Scene scene;
	private final EditPerspective editPane;
	private final SimulatePerspective simPane;
	
	private final FileChooser fileChooser;
	private static final ExtensionFilter[] sourceExtensions = {
		new ExtensionFilter("Assembly Source File", "*.s")
	};
	private final ExtensionFilter[] binaryExtensions;
	
	public GUIMain()
	{
		instance = this;
		this.fileChooser = new FileChooser();
		
		this.binaryExtensions = new ExtensionFilter[2 + config.formats.size()];
		int i = 0;
		for(FileFormat entry : config.formatList)
			this.binaryExtensions[i++] = new ExtensionFilter(entry.description, "*" + entry.extension);
		this.binaryExtensions[i++] = new ExtensionFilter("Hexadecimal Text File", "*.hex");
		this.binaryExtensions[i++] = new ExtensionFilter("All Files", "*.*");

		this.editorFont = new SimpleObjectProperty<Font>(Font.font("Courier New"));
		this.editDisplayed = new SimpleBooleanProperty(true);
		this.simDisplayed = this.editDisplayed.not();
		
		this.editPane = new EditPerspective();
		this.simPane = new SimulatePerspective();
		this.scene = new Scene(this.editPane);
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		this.primaryStage = primaryStage;
		
		primaryStage.setScene(this.scene);
		primaryStage.setWidth(800);
		primaryStage.setHeight(600);
		primaryStage.setOnCloseRequest(e -> {if(!this.onCloseRequest()) e.consume();});
		if(initialFile != null && initialFile.length > 0)
		{
			for(File f : initialFile)
				this.editPane.openFile(f);
		}
		this.editPane.onEditorTabChange();
		primaryStage.show();
	}
	
	public static void launchGUI(File[] initialFile, Config config, String... args)
	{
		GUIMain.initialFile = initialFile;
		GUIMain.config = config;
		Application.launch(args);
	}
	
	protected File promptSaveSource()
	{
		this.fileChooser.setTitle("Save source:");
		this.fileChooser.getExtensionFilters().setAll(sourceExtensions);
		this.fileChooser.setInitialDirectory(config.initDirectory);
		File f = this.fileChooser.showSaveDialog(this.primaryStage);
		if(f != null)
			config.initDirectory = f.getParentFile();
		return f;
	}

	protected File promptSaveBinary()
	{
		this.fileChooser.setTitle("Save binary:");
		this.fileChooser.getExtensionFilters().setAll(binaryExtensions);
		this.fileChooser.setInitialDirectory(config.initDirectory);
		File f = this.fileChooser.showSaveDialog(this.primaryStage);
		if(f != null)
			config.initDirectory = f.getParentFile();
		return f;
	}
	
	protected File promptOpenSource()
	{
		this.fileChooser.setTitle("Open source:");
		this.fileChooser.getExtensionFilters().setAll(sourceExtensions);
		this.fileChooser.setInitialDirectory(config.initDirectory);
		File f = this.fileChooser.showOpenDialog(this.primaryStage);
		if(f != null)
			config.initDirectory = f.getParentFile();
		return f;
	}

	protected List<File> promptOpenMultiSource()
	{
		this.fileChooser.setTitle("Open sources:");
		this.fileChooser.getExtensionFilters().setAll(sourceExtensions);
		this.fileChooser.setInitialDirectory(config.initDirectory);
		List<File> f = this.fileChooser.showOpenMultipleDialog(this.primaryStage);
		if(f != null && f.size() > 0)
		{
			config.initDirectory = f.get(0).getParentFile();
			return f;
		}
		else
			return null;
	}
	
	protected File promptOpenBinary()
	{
		this.fileChooser.setTitle("Open binary:");
		this.fileChooser.getExtensionFilters().setAll(binaryExtensions);
		this.fileChooser.setInitialDirectory(config.initDirectory);
		File f = this.fileChooser.showOpenDialog(this.primaryStage);
		if(f != null)
			config.initDirectory = f.getParentFile();
		return f;
	}

	protected List<File> promptOpenMultiBinary()
	{
		this.fileChooser.setTitle("Open binaries:");
		this.fileChooser.getExtensionFilters().setAll(binaryExtensions);
		this.fileChooser.setInitialDirectory(config.initDirectory);
		List<File> f = this.fileChooser.showOpenMultipleDialog(this.primaryStage);
		if(f != null && f.size() > 0)
		{
			config.initDirectory = f.get(0).getParentFile();
			return f;
		}
		else
			return null;
	}
	
	protected void setTitle(String title)
	{
		this.primaryStage.setTitle(title);
	}
	
	protected boolean startSimulation(File file)
	{
		try
		{
			int[] binary = BitStream.read(file, config.endian.get());
			if(binary == null) return false;
			return this.startSimulation(binary);
		}
		catch(IOException e)
		{
			return false;
		}
	}
	
	protected boolean startSimulation(int[] binary)
	{
		if(!this.simPane.loadProgram(binary))
			return false;
		this.editDisplayed.set(false);
		this.scene.setRoot(this.simPane);
		this.primaryStage.setTitle("MIPS Assembler IDE - Simulation");
		return true;
	}
	
	protected void endSimulation()
	{
		this.editDisplayed.set(true);
		this.scene.setRoot(this.editPane);
		this.editPane.onEditorTabChange();
	}
	
	protected void closeWindow()
	{
		this.primaryStage.close();
	}
	
	protected boolean onCloseRequest()
	{
		if(!this.editDisplayed.get())
		{
			if(!this.simPane.onCloseRequest())
				return false;
		}
		this.scene.setRoot(this.editPane);
		this.editPane.onEditorTabChange();
		return this.editPane.closeAllTabs();
	}
}
