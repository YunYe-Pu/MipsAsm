package mipsAsm.gui;

import mipsAsm.disassembler.util.OpFormatter;
import mipsAsm.simulator.util.RegisterFile;

public class RegisterEditPane extends EditableHexWordGrid
{
	private final RegisterFile regs;
	
	public RegisterEditPane(RegisterFile regs)
	{
		super(8, 8, 80, 20, 4, 0, i -> ((i & 8) != 0 && i != 8));
		this.regs = regs;
		this.redraw();
	}

	@Override
	public void drawHeader()
	{
		for(int i = 0; i < 32; i++)
			this.labels[i + (i & 0xf8)].setText(OpFormatter.regNames[i]);
		this.labels[8].setText("00000000");
	}

	@Override
	public void redraw()
	{
		for(int i = 1; i < 32; i++)
			this.labels[i + 8 + (i & 0xf8)].setText(String.format("%08x", this.regs.get(i)));
	}

	@Override
	public void commitModify(int newValue)
	{
		this.regs.set(((this.currModify & 0xf0) >> 1) | (this.currModify & 7), newValue);
		this.redraw();
	}

}
