package mipsAsm.simulator.util;

import java.util.HashMap;

/**
 * A class simulating a memory space of 32-bit addressing. Memory is organized
 * in pages of 4kiB(1024 words), and only pages containing data are stored.
 * Writing to a page that has not been recorded will create a blank page first,
 * while reading will return 0 directly without creating a blank page.
 * 
 * @author YunYe Pu
 */
public class Memory
{
	private HashMap<Integer, int[]> pages = new HashMap<>();
	private int endianess;//0 for little, -1 for big.
	
	/** 
	 * Get a page if it exists, or create one if does not exist.
	 * @param address
	 * @return
	 */
	public int[] getCreatePage(int address)
	{
		address >>= 12;
		int[] page = this.pages.get(address);
		if(page == null)
		{
			page = new int[1024];
//			for(int i = 0; i < 1024; i++)
//				page[i] = 0;
			this.pages.put(address, page);
		}
		return page;
	}
	
	public int[] getPage(int address)
	{
		return this.pages.get(address >> 12);
	}
	
	public Memory()
	{
		this.endianess = 0;
	}
	
	@Deprecated
	public void loadProgram(int[] programData)
	{
		int i;
		int[] page = new int[1024];
		this.pages.put(0, page);
		
		for(i = 0; i < programData.length; i++)
		{
			page[i & 0x3ff] = programData[i];
			if((i & 0x3ff) == 0x3ff)
			{
				page = new int[1024];
				this.pages.put((i + 1) >> 10, page);
			}
		}
		for(i &= 0x3ff; i < 1024; i++)
			page[i] = 0;
		
		this.endianess = 0;
	}
	
	public void loadData(int[] data, int startAddr)
	{
//		int i;
//		int[] page = new int[1024];
//		startAddr = (startAddr >> 2) & 0x3fffffff;
//		this.pages.put(startAddr >> 10, page);
//		for(i = 0; i < data.length; i++)
//		{
//			page[(i + startAddr) & 0x3ff] = data[i];
//			if(((i + startAddr) & 0x3ff) == 0x3ff)
//			{
//				page = new int[1024];
//				this.pages.put((i + 1 + startAddr) >> 10, page);
//			}
//		}
//		this.endianess = 0;
		for(int d : data)
		{
			this.writeWord(startAddr, d);
			startAddr += 4;
		}
	}
	
	public void addPage(int pageAddr, int[] data)
	{
		if(data.length == 1024)
			this.pages.put(pageAddr, data);
	}
	
	public void setEndianess(boolean endianess)
	{
		this.endianess = endianess? -1: 0;
	}
	
	public void clear()
	{
		this.pages.clear();
		this.endianess = 0;
	}
	
	
	public void writeWord(int address, int data)
	{
		int[] page = this.getCreatePage(address);
		page[(address >> 2) & 0x3ff] = data;
	}

	public void writeHalfWord(int address, int data)
	{
		int[] page = this.getCreatePage(address);
		int shift = ((address ^ this.endianess) & 0x2) << 3;
		address = (address >> 2) & 0x3ff;
		page[address] = (page[address] & ~(0xffff << shift)) | ((data & 0xffff) << shift);
	}
	
	public void writeByte(int address, int data)
	{
		int[] page = this.getCreatePage(address);
		int shift = ((address ^ this.endianess) & 0x3) << 3;
		address = (address >> 2) & 0x3ff;
		page[address] = (page[address] & ~(0xff << shift)) | ((data & 0xff) << shift);
	}

	public int readWord(int address)
	{
		int[] page = this.pages.get(address >> 12);
		if(page == null)
			return 0;
		else
			return page[(address >> 2) & 0x3ff];
	}
	
	public int readHalfWord(int address)
	{
		int shift = ((address ^ this.endianess) & 0x2) << 3;
		short ret = (short)(this.readWord(address) >> shift);
		return (int)ret;
	}
	
	public int readHalfWordUnsigned(int address)
	{
		int shift = ((address ^ this.endianess) & 0x2) << 3;
		return (this.readWord(address) >> shift) & 0xffff;
	}
	
	public int readByte(int address)
	{
		int shift = ((address ^ this.endianess) & 0x3) << 3;
		byte ret = (byte)(this.readWord(address) >> shift);
		return (int)ret;
	}
	
	public int readByteUnsigned(int address)
	{
		int shift = ((address ^ this.endianess) & 0x3) << 3;
		return (this.readWord(address) >> shift) & 0xff;
	}
	
	public int readLeft(int address, int originValue)
	{
		int shift = (((~address ^ this.endianess) & 0x3) << 3);
		int mask = 0xffffffff << shift;
		return (originValue & ~mask) | ((this.readWord(address) << shift) & mask);
	}
	
	public int readRight(int address, int originValue)
	{
		int shift = (((address ^ this.endianess) & 0x3) << 3);
		int mask = 0xffffff00 << (24 - shift);
		return (originValue & mask) | ((this.readWord(address) >> shift) & ~mask);
	}
	
	public void writeLeft(int address, int value)
	{
		int shift = (((address ^ this.endianess) & 0x3) << 3);
		int mask = 0xffffff00 << shift;
		this.writeWord(address, ((value >> (24 - shift)) & ~mask) | (this.readWord(address) & mask));
	}
	
	public void writeRight(int address, int value)
	{
		int shift = (((address ^ this.endianess) & 0x3) << 3);
		int mask = 0xffffffff << shift;
		this.writeWord(address, ((value << shift) & mask) | (this.readWord(address) & ~mask));
	}
}
