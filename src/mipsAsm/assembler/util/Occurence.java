package mipsAsm.assembler.util;

public class Occurence
{
	public final String fileName;
	public final int lineNum;
	public final String lineContent;
	
	public Occurence(String fileName, int lineNum, String lineContent)
	{
		this.fileName = fileName;
		this.lineNum = lineNum;
		this.lineContent = lineContent;
	}
}
