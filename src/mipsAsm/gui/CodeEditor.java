package mipsAsm.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Scanner;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

public class CodeEditor extends Tab
{
	public File file;
	
	private final TextArea textArea;
	private String lastSavedContent;
	private final EventHandler<Event> onTabChange;
	
	private static final Alert closePrompt = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
	private static final Alert fileChangePrompt = new Alert(AlertType.INFORMATION, "", ButtonType.OK);
	
	/**
	 * Create a tab from a source code file.
	 * @param file The source code file, or null if to create an empty document.
	 */
	public CodeEditor(File file, EventHandler<Event> onTabChange)
	{
		this.textArea = new TextArea();
		this.file = file;
		if(file == null)
		{
			this.lastSavedContent = "";
			this.setText("Untitled");
		}
		else
		{
			this.reload();
			this.setText(file.getName());
		}
		
		this.setContent(this.textArea);
		this.setOnCloseRequest(e -> {
			if(!this.tryClose()) e.consume();
		});
		this.setOnClosed(onTabChange);
		this.setOnSelectionChanged(onTabChange);
		this.onTabChange = onTabChange;
	}
	
	/**
	 * Create a tab from disassembled code. The file field of this instance will be set to null.
	 * @param initContent The disassembled code.
	 */
	public CodeEditor(String initContent, String title, EventHandler<Event> onTabChange)
	{
		this.file = null;
//		this.lastSavedContent = "";
		this.lastSavedContent = initContent;
		this.textArea = new TextArea(initContent);
		this.setText(title);
		this.setContent(this.textArea);
		this.setOnCloseRequest(e -> {
			if(!this.tryClose()) e.consume();
		});
		this.setOnClosed(onTabChange);
		this.setOnSelectionChanged(onTabChange);
		this.onTabChange = onTabChange;
	}
	
	private void _save()
	{
		try
		{
			PrintWriter p = new PrintWriter(this.file);
			p.print(this.textArea.getText());
			this.lastSavedContent = this.textArea.getText();
			this.setText(this.file.getName());
			p.close();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean save()
	{
		if(this.file == null)
		{
			this.file = GUIMain.instance.promptSaveSource();
			if(this.file == null) return false;
		}
		this._save();
		return true;
	}
	
	public boolean saveAs()
	{
		File newFile = GUIMain.instance.promptSaveSource();
		if(newFile == null) return false;
		this.file = newFile;
		this._save();
		return true;
	}
	
	public boolean reload()
	{
		if(this.file == null) return false;
		try(Scanner scanner = new Scanner(this.file))
		{
			StringBuilder str = new StringBuilder();
			while(scanner.hasNextLine())
			{
				str.append(scanner.nextLine());
				str.append('\n');
			}
			this.lastSavedContent = str.toString();
			this.textArea.setText(this.lastSavedContent);
			return true;
		}
		catch(FileNotFoundException e)
		{
			fileChangePrompt.setContentText("Unable to open file " + this.file.getName()
				+ ". It might have been moved or deleted. Please save it before preceeding.");
			fileChangePrompt.showAndWait();
			this.file = null;
			if(this.save())
			{
				this.setText(this.file.getName());
				this.onTabChange.handle(null);
				return true;
			}
			else
			{
				this.setText("Untitled");
				this.lastSavedContent = "";
				this.onTabChange.handle(null);
				return false;
			}
		}
	}
	
	/**
	 * Attempts to close the tab. If the content has changed, will prompt the user
	 * whether to save the document.
	 * @return true if confirmed to close(either saved or not saved), false if cancelled.
	 */
	public boolean tryClose()
	{
		if(this.lastSavedContent.equals(this.textArea.getText()))
			return true;
		if(this.file == null)
			closePrompt.setContentText("File has been modified. Do you want to save it?");
		else
			closePrompt.setContentText("File " + this.file.getName() + " has been modified. Do you want to save it?");
		Optional<ButtonType> result = closePrompt.showAndWait();
		if(result.isPresent())
		{
			if(result.get() == ButtonType.YES)
				return this.save();
			else if(result.get() == ButtonType.NO)
				return true;
			else
				return false;
		}
		return false;
	}
	
	public boolean hasContentChanged()
	{
		return !this.lastSavedContent.equals(this.textArea.getText());
	}
}
