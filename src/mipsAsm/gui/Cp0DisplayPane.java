package mipsAsm.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import mipsAsm.disassembler.util.OpFormatter;
import mipsAsm.simulator.CP0RegisterFile;

public class Cp0DisplayPane extends ScrollPane
{
	private final CP0RegisterFile regs;
	private final VBox layout;
	private final Label[] labels;
	private static StringBuilder buffer = new StringBuilder(40);
	
	public Cp0DisplayPane(CP0RegisterFile regs)
	{
		this.regs = regs;
		this.layout = new VBox();
		this.labels = new Label[64];
		for(int i = 0; i < 64; i++)
		{
			this.labels[i] = new Label();
			this.labels[i].setMinSize(300, 20);
			this.labels[i].setAlignment(Pos.CENTER);
			this.labels[i].fontProperty().bind(GUIMain.instance.editorFont);
		}
		for(int i = 0; i < 64; i += 2)
		{
			this.labels[i].setText(OpFormatter.cp0RegNames[i >> 1]);
			this.labels[i + 1].setText("0000 0000 0000 0000 0000 0000 0000 0000");
		}
		this.layout.getChildren().addAll(this.labels);
		this.setContent(this.layout);
		this.setPrefHeight(180);
	}
	
	public void redraw()
	{
		for(int i = 1; i < 64; i += 2)
		{
			buffer.setLength(0);
			int mask = 0x80000000;
			int val = this.regs.hardGet(i >> 1);
			while(mask != 0)
			{
				buffer.append((mask & val) == 0? '0': '1');
				if((mask & 0x11111110) != 0)
					buffer.append(' ');
				mask = (mask >> 1) & 0x7fffffff;
			}
			this.labels[i].setText(buffer.toString());
		}
	}
}
