package mipsAsm.gui;

import java.io.File;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import mipsAsm.util.BinaryType;

public class GUIMain extends Application
{
	public static GUIMain instance;
	private Stage primaryStage;

	private final SimpleBooleanProperty endianess;
	private final SimpleObjectProperty<Font> editorFont;
	
	private final Scene scene;
	private final EditPerspective editPane;
	private final SimulatePerspective simPane;
	
	private final FileChooser fileChooser;
	private static final ExtensionFilter[] sourceExtensions = {
		new ExtensionFilter("Assembly Source File", "*.s")
	};
	private static final ExtensionFilter[] binaryExtensions = {
		new ExtensionFilter("COE File", "*.coe"),
		new ExtensionFilter("Hexadecimal Text File", "*.hex"),
		new ExtensionFilter("All Files", "*.*")
	};
	
	public GUIMain()
	{
		instance = this;
		this.fileChooser = new FileChooser();
		this.endianess = new SimpleBooleanProperty();
		this.editorFont = new SimpleObjectProperty<Font>(Font.font("Courier New"));
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
		this.editPane.onEditorTabChange();
		primaryStage.show();
	}
	
	public static void launch(String... args)
	{
		Application.launch(args);
	}
	
	protected File promptSaveSource()
	{
		this.fileChooser.setTitle("Save source:");
		this.fileChooser.getExtensionFilters().setAll(sourceExtensions);
		File f = this.fileChooser.showSaveDialog(this.primaryStage);
		if(f != null)
			this.fileChooser.setInitialDirectory(f.getParentFile());
		return f;
	}

	protected File promptSaveBinary()
	{
		this.fileChooser.setTitle("Save binary:");
		this.fileChooser.getExtensionFilters().setAll(binaryExtensions);
		File f = this.fileChooser.showSaveDialog(this.primaryStage);
		if(f != null)
			this.fileChooser.setInitialDirectory(f.getParentFile());
		return f;
	}
	
	protected File promptOpenSource()
	{
		this.fileChooser.setTitle("Open source:");
		this.fileChooser.getExtensionFilters().setAll(sourceExtensions);
		File f = this.fileChooser.showOpenDialog(this.primaryStage);
		if(f != null)
			this.fileChooser.setInitialDirectory(f.getParentFile());
		return f;
	}

	protected List<File> promptOpenMultiSource()
	{
		this.fileChooser.setTitle("Open sources:");
		this.fileChooser.getExtensionFilters().setAll(sourceExtensions);
		List<File> f = this.fileChooser.showOpenMultipleDialog(this.primaryStage);
		if(f != null && f.size() > 0)
		{
			this.fileChooser.setInitialDirectory(f.get(0).getParentFile());
			return f;
		}
		else
			return null;
	}
	
	protected File promptOpenBinary()
	{
		this.fileChooser.setTitle("Open binary:");
		this.fileChooser.getExtensionFilters().setAll(binaryExtensions);
		File f = this.fileChooser.showOpenDialog(this.primaryStage);
		if(f != null)
			this.fileChooser.setInitialDirectory(f.getParentFile());
		return f;
	}

	protected List<File> promptOpenMultiBinary()
	{
		this.fileChooser.setTitle("Open binaries:");
		this.fileChooser.getExtensionFilters().setAll(binaryExtensions);
		List<File> f = this.fileChooser.showOpenMultipleDialog(this.primaryStage);
		if(f != null && f.size() > 0)
		{
			this.fileChooser.setInitialDirectory(f.get(0).getParentFile());
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
		int[] binary = BinaryType.read(file, this.endianess.get());
		if(binary == null) return false;
		return this.startSimulation(binary);
	}
	
	protected boolean startSimulation(int[] binary)
	{
		this.simPane.loadProgram(binary);
		this.scene.setRoot(this.simPane);
		this.primaryStage.setTitle("MIPS Assembler IDE - Simulation");
		return true;
	}
	
	protected void endSimulation()
	{
		this.scene.setRoot(this.editPane);
		this.editPane.onEditorTabChange();
	}
	
	public boolean getEndianess()
	{
		return this.endianess.get();
	}
	
	public BooleanProperty endianess()
	{
		return this.endianess;
	}
	
	public void setEndianess(boolean endianess)
	{
		this.endianess.set(endianess);
	}
	
	public Font getEditorFont()
	{
		return this.editorFont.get();
	}
	
	public void setEditorFont(Font newFont)
	{
		this.editorFont.set(newFont);
	}
	
	public ObjectProperty<Font> editorFont()
	{
		return this.editorFont;
	}

	public void closeWindow()
	{
		this.primaryStage.close();
	}
	
	public boolean onCloseRequest()
	{
		this.scene.setRoot(this.editPane);
		this.editPane.onEditorTabChange();
		return this.editPane.closeAllTabs();
	}
}
