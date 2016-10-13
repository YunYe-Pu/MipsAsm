package mipsAsm.simulator;

import mipsAsm.simulator.util.SimException;

import static mipsAsm.simulator.util.SimExceptionCode.*;

/**
 * Address translation unit: Translation Look-aside Buffer with 32 entries.
 * Written according to MIPS32 Architecture Vol.3
 * 
 * @author YunYe Pu
 */
public class TLB
{
	private final TLBEntry[] entries = new TLBEntry[32];
	
	public TLB()
	{
		for(int i = 0; i < 32; i++)
		{
			entries[i] = new TLBEntry();
			entries[i].age = i;
		}
	}
	
	public int addressTranslation(Simulator simulator, int vAddr, boolean write) throws SimException
	{
		if((vAddr & 0x80000000) != 0 && simulator.cp0.inUserMode())//Trying to access kernel address space in user mode
			simulator.signalException(write? AddressError_S: AddressError_L, vAddr);
		if((vAddr & 0xc0000000) == 0x80000000)//0x80000000 to 0xbfffffff, unmapped
			return vAddr;
		if((vAddr & 0xe0000000) == 0 && (simulator.cp0.hardGet(12) & 0b100) != 0)//0x00000000 to 0x1fffffff is unmapped if ERL bit in Cause register is set.
			return vAddr;
		int i = 0;
		int temp = simulator.cp0.hardGet(10) & 0xff;
		for(; i < 32; i++)
		{
			if(entries[i].match(vAddr, temp))
				break;
		}
		if(i == 32)
			simulator.signalException(write? TLBRefill_S: TLBRefill_L, vAddr);
		temp = entries[i].translate(simulator, vAddr, write);
		int j = 0;
		for(; j < 32; j++)
		{
			if(entries[j].age < entries[i].age)
				entries[j].age++;
		}
		entries[i].age = 0;
		//Update random register
		updateRandom(simulator);
		return 0;
	}
	
	public void readEntry(Simulator simulator)
	{
		int i = simulator.cp0.hardGet(0) & 0x1f;
		simulator.cp0.hardSet(5, entries[i].pageMask, 0x1fffe000);
		//EntryHi
		simulator.cp0.hardSet(10, entries[i].vpn2, 0xffffe000);
		simulator.cp0.hardSet(10, entries[i].asid, 0xff);
		//EntryLo0, EntryLo1
		simulator.cp0.hardSet(2, entries[i].pfn0 >> 6, 0x03ffffc0);
		simulator.cp0.hardSet(3, entries[i].pfn1 >> 6, 0x03ffffc0);
		simulator.cp0.hardSet(2, entries[i].d0? -1: 0, 4);
		simulator.cp0.hardSet(3, entries[i].d1? -1: 0, 4);
		simulator.cp0.hardSet(2, entries[i].c0 << 3, 0x38);
		simulator.cp0.hardSet(3, entries[i].c1 << 3, 0x38);
		simulator.cp0.hardSet(2, entries[i].v0? -1: 0, 2);
		simulator.cp0.hardSet(3, entries[i].v1? -1: 0, 2);
		simulator.cp0.hardSet(2, entries[i].globl? -1: 0, 1);
		simulator.cp0.hardSet(3, entries[i].globl? -1: 0, 1);
	}
	
	public void writeIndexed(Simulator simulator)
	{
		write(simulator, simulator.cp0.hardGet(0) & 0x1f);
	}
	
	public void writeRandom(Simulator simulator)
	{
		write(simulator, simulator.cp0.hardGet(1));
	}
	
	public void probe(Simulator simulator)
	{
		int i;
		int addr = simulator.cp0.hardGet(10) & 0xffffe000;
		int asid = simulator.cp0.hardGet(10) & 0xff;
		for(i = 0; i < 32; i++)
			if(entries[i].match(addr, asid)) break;
		if(i == 32)
			simulator.cp0.hardSet(0, -1, 0x80000000);
		else
			simulator.cp0.hardSet(0, i, -1);
	}
	
	private void write(Simulator simulator, int index)
	{
		entries[index].asid = simulator.cp0.hardGet(10) & 0xff;
		entries[index].vpn2 = simulator.cp0.hardGet(10) & 0xffffe000;
		entries[index].pageMask = simulator.cp0.hardGet(0x1fffe000);
		entries[index].globl = (simulator.cp0.hardGet(2) & simulator.cp0.hardGet(3) & 1) != 0;
		entries[index].pfn0 = simulator.cp0.hardGet(2) & 0x03ffffc0;
		entries[index].pfn1 = simulator.cp0.hardGet(3) & 0x03ffffc0;
		entries[index].c0 = (simulator.cp0.hardGet(2) >> 3) & 7;
		entries[index].c1 = (simulator.cp0.hardGet(3) >> 3) & 7;
		entries[index].d0 = (simulator.cp0.hardGet(2) & 4) != 0;
		entries[index].d1 = (simulator.cp0.hardGet(3) & 4) != 0;
		entries[index].v0 = (simulator.cp0.hardGet(2) & 2) != 0;
		entries[index].v1 = (simulator.cp0.hardGet(3) & 2) != 0;
		for(TLBEntry e : entries)
			if(e.age < entries[index].age) e.age++;
		entries[index].age = 0;
		updateRandom(simulator);
	}
	
	private void updateRandom(Simulator simulator)
	{
		int i, j;
		i = j = simulator.cp0.hardGet(6);//read wired register
		for(j++; j < 32; j++)
		{
			if(entries[j].age > entries[i].age)
				i = j;
		}
		simulator.cp0.hardSet(1, i, 0x1f);
	}
	
	private static class TLBEntry
	{
		public int vpn2 = 0;
		public int asid = 0;

		public int pageMask = 0;
		public boolean globl = false;
		public int pfn0 = 0;
		public boolean d0 = false;
		public boolean v0 = false;
		public int pfn1 = 0;
		public boolean d1 = false;
		public boolean v1 = false;
		public int c0 = 0;
		public int c1 = 0;
		public int age = 0;
		
		public boolean match(int vAddr, int asid)
		{
			if(asid == this.asid || this.globl)
				return true;
			if(((vAddr ^ this.vpn2) & (0xffffe000 & ~this.pageMask)) == 0)
				return true;
			return false;
		}
		
		public int translate(Simulator sim, int vAddr, boolean write) throws SimException
		{
			int m = this.pageMask | 0x1fff;
			if((vAddr & m & ~(m >> 1)) == 0)
			{
				if(!this.v0)
					sim.signalException(write? TLBInvalid_S: TLBInvalid_L, vAddr);
				if(write & !this.d0)
					sim.signalException(TLBModified, vAddr);
				return (vAddr & ~m) | (this.pfn0 & m);
			}
			else
			{
				if(!this.v1)
					sim.signalException(write? TLBInvalid_S: TLBInvalid_L, vAddr);
				if(write & !this.d1)
					sim.signalException(TLBModified, vAddr);
				return (vAddr & ~m) | (this.pfn1 & m);
			}
		}
	}
	
}
