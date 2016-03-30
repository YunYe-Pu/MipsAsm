package mipsAsm.assembler.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitStream
{
	private int[] data;
	private int writeIndex;
	private final boolean endianess;
	
	public BitStream(int size, boolean endianess)
	{
		this.data = new int[size];
		this.writeIndex = 0;
		this.endianess = endianess;
	}
	
	public void append(int data)
	{
		this.data[this.writeIndex++] = data;
	}
	
	public int[] getData()
	{
		return this.data;
	}
	
	public StringBuilder getAsHexString()
	{
		StringBuilder buffer = new StringBuilder(this.data.length * 8);
		if(this.endianess)//big-endian, output as normal
		{
			int i, j;
			for(i = 0; i < data.length; i++)
			{
				String s = Integer.toHexString(data[i]);
				for(j = s.length(); j < 8; j++)
					buffer.append('0');
				buffer.append(s);
			}
		}
		else//little-endian, output least-significant byte first
		{
			int i, j, val;
			String s;
			for(i = 0; i < data.length; i++)
			{
				val = data[i];
				for(j = 0; j < 4; j++)
				{
					s = Integer.toHexString(val & 0xff);
					if(s.length() == 1)
						buffer.append('0');
					buffer.append(s);
					val >>= 8;
				}
			}
		}
		return buffer;
	}
	
	private static final String COE_PREFIX = "memory_initialization_radix=16;\nmemory_initialization_vector=\n";
	public StringBuilder getAsCOE()
	{
		StringBuilder buffer = new StringBuilder(COE_PREFIX.length() + this.data.length * 10);
		String strAppend = COE_PREFIX;
		int i, j;
		for(i = 0; i < data.length; i++)
		{
			buffer.append(strAppend);
			String s = Integer.toHexString(data[i]);
			for(j = s.length(); j < 8; j++)
				buffer.append('0');
			buffer.append(s);
			strAppend = ",\n";
		}
		buffer.append(";\n");
		return buffer;
	}
	
	public void writeBinary(File target) throws IOException
	{
		try(FileOutputStream stream = new FileOutputStream(target))
		{
			if(this.endianess)
			{
				for(int i : this.data)
				{
					stream.write((i >> 24) & 0xff);
					stream.write((i >> 16) & 0xff);
					stream.write((i >>  8) & 0xff);
					stream.write(i & 0xff);
				}
			}
			else
			{
				for(int i : this.data)
				{
					stream.write(i & 0xff);
					stream.write((i >>  8) & 0xff);
					stream.write((i >> 16) & 0xff);
					stream.write((i >> 24) & 0xff);
				}
			}
		}
	}
}
