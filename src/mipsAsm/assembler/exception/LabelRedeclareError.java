package mipsAsm.assembler.exception;

import mipsAsm.assembler.util.Occurence;

public class LabelRedeclareError extends AsmError
{
	private static final long serialVersionUID = -7528037647464092631L;

	private Occurence firstDeclare;

	public LabelRedeclareError(String labelName, Occurence firstDeclare)
	{
		super("Label redeclaration", "Redeclaration of label \"" + labelName + "\".");
		this.firstDeclare = firstDeclare;
	}
	
	public String getLocalizedMessage()
	{
		String s = super.getLocalizedMessage();
		s += String.format("First declared in file %s, line %d:\n", firstDeclare.fileName, firstDeclare.lineNum);
		s += firstDeclare.lineNum + "  " + firstDeclare.lineContent + "\n";
		return s;
	}
	
}
