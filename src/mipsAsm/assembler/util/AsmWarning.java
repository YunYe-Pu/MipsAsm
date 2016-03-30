package mipsAsm.assembler.util;

public class AsmWarning
{
	private final String type;
	private final String message;
	
	public AsmWarning(String type, String message)
	{
		this.message = message;
		this.type = type;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public String getMessage()
	{
		return this.message;
	}
	
}
