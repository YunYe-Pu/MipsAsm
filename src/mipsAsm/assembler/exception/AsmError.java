package mipsAsm.assembler.exception;

import mipsAsm.assembler.util.Occurence;

public class AsmError extends Exception
{
	private static final long serialVersionUID = -2021243946193437993L;

	private final String type;

	private Occurence occurence;
	
	public AsmError(String type, String message)
	{
		super(message);
		this.type = type;
		this.occurence = null;
	}
	
	public AsmError(String type, String message, Throwable cause)
	{
		super(message, cause);
		this.type = type;
		this.occurence = null;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public void setOccurence(int lineNum, String instr, String fileName)
	{
		this.setOccurence(new Occurence(fileName, lineNum, instr));
	}
	
	public void setOccurence(Occurence occurence)
	{
		this.occurence = occurence;
	}
	
	public Occurence getOccurence()
	{
		return this.occurence;
	}
	
	@Override
	public String getLocalizedMessage()
	{
		String s = String.format("Error in file %s, line %d: %s\n", this.occurence.fileName, this.occurence.lineNum, this.type);
		s = s + this.getMessage() + "\n";
		s = s + this.occurence.lineNum + "  " + this.occurence.lineContent + "\n";
		return s;
	}

}
