package mipsAsm.assembler.exception;

import mipsAsm.assembler.operand.Operand;

public class OpTypeMismatchError extends AsmError
{
	private static final long serialVersionUID = 6901657572933871591L;

	public OpTypeMismatchError(int index, String typeProvided, String typeExpected)
	{
		super("Operand type mismatch", String.format("Expected %s for operand %d but provided %s.", typeExpected, index + 1, typeProvided));
	}
	
	public OpTypeMismatchError(int index, Class<? extends Operand> typeProvided, Class<? extends Operand> typeExpected)
	{
		this(index, Operand.getTypeName(typeProvided), Operand.getTypeName(typeExpected));
	}
	
	public OpTypeMismatchError(int index, Operand typeProvided, Class<? extends Operand> typeExpected)
	{
		this(index, Operand.getTypeName(typeProvided), Operand.getTypeName(typeExpected));
	}
	public OpTypeMismatchError(int index, Class<? extends Operand> typeProvided, Operand typeExpected)
	{
		this(index, Operand.getTypeName(typeProvided), Operand.getTypeName(typeExpected));
	}
	
	public OpTypeMismatchError(int index, Operand typeProvided, Operand typeExpected)
	{
		this(index, Operand.getTypeName(typeProvided), Operand.getTypeName(typeExpected));
	}
	
	

}
