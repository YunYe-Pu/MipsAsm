package mipsAsm.simulator;

public class CP0RegisterFile
{
	private int[] regs;
	private int[] mask;
	private int[] resetValue;
	
	public CP0RegisterFile()
	{
		this.mask = new int[]{
			0x1f,//Index
			0,//Random
			0x03ffffff,//EntryLo0
			0x03ffffff,//EntryLo1
			0xff800000,//Context
			0x1fffe000,//PageMask
			0x1f,//Wired
			0,//Reserved
			0,//BadVAddr
			-1,//Count
			0xffffe0ff,//EntryHi
			-1,//Compare
			0x1060ff17,//Status
			0x00c00300,//Cause
			-1,//EPC
			0,//PRId
			0x7,//Config
			0,//LLAddr
			0,//WatchLo, reserved
			0,//WathcHi, reserved
			0,
			0,
			0,
			0,//Debug, reserved
			0,//DEPC, reserved
			0,//Performance Counter, reserved
			0,//ErrCtl, reserved
			0,//CacheErr, reserved
			0,//Tag/Data Lo, reserved
			0,//Tag/Data Hi, reserved
			-1,//ErrorEPC
			0//DESAVE, reserved
		};
		this.resetValue = new int[]{
			0,//Index
			31,//Random
			0,//EntryLo0
			0,//EntryLo1
			0,//Context
			0,//PageMask
			0,//Wired
			0,//Reserved
			0,//BadVAddr
			0,//Count
			0,//EntryHi
			0,//Compare
			0b0001_0000_0100_0000_1111_1111_0000_0100,//Status
			0,//Cause
			0,//EPC
			0,//PRId TODO
			0,//Config TODO
			0,//LLAddr
			0,//WatchLo, reserved
			0,//WatchHi, reserved
			0,
			0,
			0,
			0,//Debug, reserved
			0,//DEPC, reserved
			0,//Performance Counter, reserved
			0,//ErrCtl, reserved
			0,//CacheErr, reserved
			0,//Tag/Data Lo, reserved
			0,//Tag/Data Hi, reserved
			0,//ErrorEPC
			0//DESAVE, reserved
		};
		this.regs = this.resetValue.clone();
	}
	
	public int get(int index, int sel)
	{
		if((sel & 7) != 0)
			return 0;
		else
			return this.regs[index & 31];
	}
	
	public int hardGet(int index)
	{
		return this.regs[index];
	}
	
	public void set(int index, int sel, int value)//Software set
	{
		if((sel & 7) != 0)
			return;
		else
		{
			index &= 31;
			this.regs[index] &= ~this.mask[index];
			this.regs[index] |= (this.mask[index] & value);
		}
	}
	
	public void hardSet(int index, int value, int mask)//Hardware set
	{
		this.regs[index] &= ~mask;
		this.regs[index] |= (mask & value);
	}
	
	public void reset()
	{
		for(int i = 0; i < 32; i++)
			this.regs[i] = this.resetValue[i];
	}
	
	public boolean inUserMode()
	{
		return (this.regs[12] & 0b10110) == 0b10000;
	}
}
