package mipsAsm.assembler.directive;

import mipsAsm.assembler.instruction.Instruction;

public class DataInstruction implements Instruction
{
	public final int data;
	
	public DataInstruction(int data)
	{
		this.data = data;
	}
	
	@Override
	public int toBinary()
	{
		return this.data;
	}
}
