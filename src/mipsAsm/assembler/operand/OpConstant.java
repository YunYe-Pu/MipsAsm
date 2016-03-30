package mipsAsm.assembler.operand;

import mipsAsm.assembler.util.AsmWarning;

public class OpConstant extends Operand
{
	public static final OpConstant ZERO = new OpConstant(0);
	
	private int value;
	
	public OpConstant(int value)
	{
		this.value = value;
	}
	
	@Override
	public int getEncoding()
	{
		return this.value;
	}

	@Override
	public AsmWarning setWidth(int width)
	{
		this.value &= ((1 << width) - 1);
		return null;
	}

}
