package mipsAsm.assembler.exception;

public class OpCountMismatchError extends AsmError
{
	private static final long serialVersionUID = -5549410025722061889L;

	public OpCountMismatchError(int numExpected, int numProvided)
	{
		super("Operand number mismatch", String.format("Expected %d operand(s) but provided %d.", numExpected, numProvided));
	}
}
