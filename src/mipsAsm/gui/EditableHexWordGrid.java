package mipsAsm.gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public abstract class EditableHexWordGrid extends GridPane
{
	protected final Label[] labels;
	protected final TextField editText = new TextField();
	
	protected int currModify = -1;
	protected String prevText = null;
	
	public EditableHexWordGrid(int rows, int columns, int editableRow, int editableCol, int cellWidth, int cellHeight)
	{
		super();
		int cnt = rows * columns;
		this.labels = new Label[cnt];
		for(int i = 0; i < cnt; i++)
		{
			if(i % columns >= editableCol && i / columns >= editableRow)
			{
				this.labels[i] = new IndexedLabel(i);
				this.labels[i].setOnMouseClicked(e -> this.onLabelClicked(e));
			}
			else
			{
				this.labels[i] = new Label();
				this.labels[i].setOnMouseClicked(e -> this.cancelModify());
			}
			this.labels[i].setMinSize(cellWidth, cellHeight);
			this.labels[i].setAlignment(Pos.CENTER);
			this.labels[i].fontProperty().bind(GUIMain.instance.editorFont());
			this.add(this.labels[i], i % columns, i / columns);
		}

		this.editText.setAlignment(Pos.CENTER);
		this.editText.setPrefSize(cellWidth, cellHeight);
		this.editText.setVisible(false);
		this.editText.setOnKeyPressed(e -> this.onTextKeyPressed(e));
		this.editText.fontProperty().bind(GUIMain.instance.editorFont());
		
		this.drawHeader();
		this.redraw();
	}
	
	private void onLabelClicked(MouseEvent event)
	{
		if(event.getClickCount() == 1)
			this.cancelModify();
		else if(event.getClickCount() == 2)
			this.startModify(((IndexedLabel)event.getSource()).index);
	}
	
	private void onTextKeyPressed(KeyEvent event)
	{
		if(event.getCode() == KeyCode.ESCAPE)
			this.cancelModify();
		else if(event.getCode() == KeyCode.ENTER)
		{
			try
			{
				int i = Integer.parseUnsignedInt(this.editText.getText(), 16);
				this.labels[this.currModify].setGraphic(null);
				this.labels[this.currModify].setText(String.format("%08x", i));
				this.editText.setVisible(false);
				this.commitModify(i);
				this.currModify = -1;
			}
			catch(NumberFormatException e)
			{
				this.cancelModify();
			}
		}
	}
	
	public void cancelModify()
	{
		if(this.currModify > -1)
		{
			this.labels[this.currModify].setGraphic(null);
			this.labels[this.currModify].setText(this.prevText);
			this.editText.setVisible(false);
			this.currModify = -1;
		}
	}
	
	public void startModify(int index)
	{
		this.prevText = this.labels[index].getText();
		this.labels[index].setText("");
		this.editText.setText(this.prevText);
		this.labels[index].setGraphic(this.editText);
		this.editText.setVisible(true);
		this.currModify = index;
	}
	
	public abstract void drawHeader();

	public abstract void redraw();
	
	public abstract void commitModify(int newValue);
	
	protected static class IndexedLabel extends Label
	{
		public final int index;
		
		public IndexedLabel(int index)
		{
			super();
			this.index = index;
		}
		
		public IndexedLabel(String text, int index)
		{
			super(text);
			this.index = index;
		}
		
		public IndexedLabel(String text, Node graphic, int index)
		{
			super(text, graphic);
			this.index = index;
		}
	}

}
