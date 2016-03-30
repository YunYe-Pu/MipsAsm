package mipsAsm.assembler.exception;

public class LabelNotDeclaredError extends AsmError
{
	private static final long serialVersionUID = 954374477499659208L;

	public final String labelName;
	
	public LabelNotDeclaredError(String labelName)
	{
		super("Label not declared", "Unable to locate label \"" + labelName + "\".");
		this.labelName = labelName;
	}
}
