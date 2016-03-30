package mipsAsm.assembler.util;

public class LabelOccurence
{
	public final int address;
	public final Occurence occurence;
	
	public LabelOccurence(int address, Occurence occurence)
	{
		this.address = address;
		this.occurence = occurence;
	}
}
