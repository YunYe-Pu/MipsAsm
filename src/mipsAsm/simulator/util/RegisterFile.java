package mipsAsm.simulator.util;

public class RegisterFile
{
	private int[] regs;
	
	public RegisterFile()
	{
		this.regs = new int[32];
		for(int i = 0; i < 32; i++)
			this.regs[i] = 0;
	}
	
	public int get(int index)
	{
		return this.regs[index & 31];
	}
	
	public void set(int index, int value)
	{
		if((index & 31) != 0)
			this.regs[index & 31] = value;
	}
	
	public void clear()
	{
		for(int i = 0; i < 32; i++)
			this.regs[i] = 0;
	}
}
