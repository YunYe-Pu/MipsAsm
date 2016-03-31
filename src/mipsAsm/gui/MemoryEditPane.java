package mipsAsm.gui;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import mipsAsm.simulator.util.Memory;

public class MemoryEditPane extends BorderPane
{
	private Label[] labels = new Label[81];
	public GridPane dispPane = new GridPane();
	private TextField editText = new TextField();
	private ScrollBar scrollV = new ScrollBar();
//	private ScrollBar scrollH = new ScrollBar();
	
	private Memory memory;
	
	public MemoryEditPane(Memory memory)
	{
		int i;
		this.labels[0] = new Label("offset");
		this.labels[0].setOnMouseClicked(e -> this.cancelModify());
		for(i = 1; i < 9; i++)
		{
			this.labels[i] = new Label(Integer.toString(i - 1));
			this.labels[i].setOnMouseClicked(e -> this.cancelModify());
		}
		for(i = 9; i < 81; i++)
		{
			this.labels[i] = new IndexedLabel(i);
			this.labels[i].setOnMouseClicked(e -> this.onLabelClicked(e));
		}
		for(i = 0; i < 81; i++)
		{
			this.labels[i].setMinSize(75, 25);
			this.labels[i].setAlignment(Pos.CENTER);
			this.labels[i].setFont(Font.font("Courier New"));
			this.dispPane.add(this.labels[i], i % 9, i / 9);
		}
		
		this.editText.setAlignment(Pos.CENTER);
		this.editText.setPrefSize(75, 25);
		this.editText.setVisible(false);
		this.editText.setOnKeyPressed(e -> this.onTextKeyPressed(e));
		this.editText.setFont(Font.font("Courier New"));

		this.memory = memory;
		this.redrawMemory();
		
		this.scrollV.setMax(0x8000000 - 8);
		this.scrollV.setMin(0);
		this.scrollV.setUnitIncrement(1);
		this.scrollV.valueProperty().addListener((e, oldVal, newVal) -> this.redrawMemory());
		this.scrollV.setOrientation(Orientation.VERTICAL);
		this.dispPane.setOnScroll(e -> {
			this.cancelModify();
			this.scrollV.fireEvent(e);
		});
		
		this.setCenter(this.dispPane);
		this.setRight(this.scrollV);
	}
	
	public void redrawMemory()
	{
		int i;
		int offset = (int)this.scrollV.getValue();
		for(i = 9; i < 81; i += 9)
			this.labels[i].setText(String.format("%08x", (offset + i / 9 - 1) << 5));
		
		int[] page = this.memory.getPage(offset << 5);
		int addr = (offset << 3) & 1023;
		i = 0;
		if(page != null)
		{
			for(; i < 64 && addr < 1024; addr++, i++)
				this.labels[i + 10 + i / 8].setText(String.format("%08x", page[addr]));
		}
		else
		{
			for(; i < 64 && addr < 1024; addr++, i++)
				this.labels[i + 10 + i / 8].setText("00000000");
		}
		
		if(i == 64) return;
		page = this.memory.getPage((offset << 5) + 1024);
		addr = 0;
		if(page != null)
		{
			for(; i < 64; i++, addr++)
				this.labels[i + 10 + i / 8].setText(String.format("%08x", page[addr]));
		}
		else
		{
			for(; i < 64; i++, addr++)
				this.labels[i + 10 + i / 8].setText("00000000");
		}
	}
	
	
	
	private int currModify = -1;
	private String prevText = null;

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
			this.commitModify();
	}
	
	private void cancelModify()
	{
		if(this.currModify > -1)
		{
			this.labels[this.currModify].setGraphic(null);
			this.labels[this.currModify].setText(this.prevText);
			this.editText.setVisible(false);
			this.currModify = -1;
		}
	}
	
	private void startModify(int index)
	{
		this.prevText = this.labels[index].getText();
		this.labels[index].setText("");
		this.editText.setText(this.prevText);
		this.labels[index].setGraphic(this.editText);
		this.editText.setVisible(true);
		this.currModify = index;
	}
	
	private void commitModify()
	{
		if(this.currModify > -1)
		{
			try
			{
				int i = Integer.parseUnsignedInt(this.editText.getText(), 16);
				this.labels[this.currModify].setGraphic(null);
				this.labels[this.currModify].setText(String.format("%08x", i));
				this.editText.setVisible(false);
				if(currModify % 9 == 0)
				{
					this.scrollV.setValue(((i >> 5) & 0x08ffffff) - (this.currModify / 9 - 1));
					this.redrawMemory();
				}
				else
					this.memory.writeWord(((int)this.scrollV.getValue() << 5) +
						(((this.currModify - 9) * 8 / 9) << 2), i);
				this.currModify = -1;
			}
			catch(NumberFormatException e)
			{
				this.labels[this.currModify].setGraphic(null);
				this.labels[this.currModify].setText(this.prevText);
				this.editText.setVisible(false);
				this.currModify = -1;
			}
		}
	}
	
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
