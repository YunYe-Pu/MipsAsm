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
	
	private final Scene editScene;
	private final EditPerspective editPane;
	private final SimpleBooleanProperty endianess;
	private final SimpleObjectProperty<Font> editorFont;
	
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
		this.editorFont = new SimpleObjectProperty<Font>(Font.font("Courier New", 13));
		this.editPane = new EditPerspective();
		this.editScene = new Scene(this.editPane);
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		this.primaryStage = primaryStage;
		
		primaryStage.setScene(this.editScene);
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
	
	public File promptSaveSource()
	{
		this.fileChooser.setTitle("Save source:");
		this.fileChooser.getExtensionFilters().setAll(sourceExtensions);
		File f = this.fileChooser.showSaveDialog(this.primaryStage);
		if(f != null)
			this.fileChooser.setInitialDirectory(f.getParentFile());
		return f;
	}

	public File promptSaveBinary()
	{
		this.fileChooser.setTitle("Save binary:");
		this.fileChooser.getExtensionFilters().setAll(binaryExtensions);
		File f = this.fileChooser.showSaveDialog(this.primaryStage);
		if(f != null)
			this.fileChooser.setInitialDirectory(f.getParentFile());
		return f;
	}
	
	public File promptOpenSource()
	{
		this.fileChooser.setTitle("Open source:");
		this.fileChooser.getExtensionFilters().setAll(sourceExtensions);
		File f = this.fileChooser.showOpenDialog(this.primaryStage);
		if(f != null)
			this.fileChooser.setInitialDirectory(f.getParentFile());
		return f;
	}

	public List<File> promptOpenMultiSource()
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
	
	public File promptOpenBinary()
	{
		this.fileChooser.setTitle("Open binary:");
		this.fileChooser.getExtensionFilters().setAll(binaryExtensions);
		File f = this.fileChooser.showOpenDialog(this.primaryStage);
		if(f != null)
			this.fileChooser.setInitialDirectory(f.getParentFile());
		return f;
	}

	public List<File> promptOpenMultiBinary()
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
	
	public void setTitle(String title)
	{
		this.primaryStage.setTitle(title);
	}
	
	public boolean startSimulation(File file)
	{
		int[] binary = BinaryType.read(file, this.endianess.get());
		if(binary == null) return false;
		return this.startSimulation(binary);
	}
	
	public boolean startSimulation(int[] binary)
	{
		//TODO
		
		return false;
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
		return this.editPane.closeAllTabs();
	}
}
