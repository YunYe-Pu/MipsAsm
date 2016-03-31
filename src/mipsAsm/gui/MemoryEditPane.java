package mipsAsm.gui;

import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import mipsAsm.simulator.util.Memory;

public class MemoryEditPane extends BorderPane
{
	private EditableHexWordGrid dispPane;
	private ScrollBar scrollV = new ScrollBar();
	
	private Memory memory;
	
	public MemoryEditPane(Memory memory)
	{
		super();
		this.memory = memory;
		this.dispPane = new MemoryEditGrid();
		
		this.scrollV.setMax(0x08000000 - 8);
		this.scrollV.setMin(0);
		this.scrollV.setUnitIncrement(1);
		this.scrollV.valueProperty().addListener((e, oldVal, newVal) -> this.dispPane.redraw());
		this.scrollV.setOrientation(Orientation.VERTICAL);
		this.dispPane.setOnScroll(e -> {
			this.dispPane.cancelModify();
			this.scrollV.fireEvent(e);
		});
		
		this.setCenter(this.dispPane);
		this.setRight(this.scrollV);
	}
	
	private class MemoryEditGrid extends EditableHexWordGrid
	{
		public MemoryEditGrid()
		{
			super(9, 9, 1, 0, 75, 25, Font.font("Courier New"));
		}

		@Override
		public void drawHeader()
		{
			this.labels[0].setText("offset");
			for(int i = 1; i < 9; i++)
				this.labels[i].setText(Integer.toString(i - 1));
		}

		@Override
		public void redraw()
		{
			int i;
			int offset = (int)scrollV.getValue();
			for(i = 9; i < 81; i += 9)
				this.labels[i].setText(String.format("%08x", (offset + i / 9 - 1) << 5));
			
			int[] page = memory.getPage(offset << 5);
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
			page = memory.getPage((offset << 5) + 1024);
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

		@Override
		public void commitModify(int newValue)
		{
			if(this.currModify % 9 == 0)
			{
				int pos = ((newValue >> 5) & 0x07ffffff) - (this.currModify / 9 - 1);
				if(pos > 0x08000000 - 8)
					pos = 0x08000000 - 8;
				else if(pos < 0)
					pos = 0;
				if(scrollV.getValue() == pos)
					this.redraw();
				else
					scrollV.setValue(pos);
			}
			else
				memory.writeWord(((int)scrollV.getValue() << 5) + (((this.currModify - 9) * 8 / 9) << 2), newValue);
		}
	}
	
}
