package mipsAsm.assembler.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import mipsAsm.util.BinaryIOFunction;
import mipsAsm.util.FileFormat;

public class BitStream
{
	private int[] data;
	private int writeIndex;
	private final boolean endianess;
	
	private static final HashMap<String, BinaryIOFunction> ioFunction = new HashMap<>();
	
	public static void init(HashMap<String, FileFormat> map)
	{
		ioFunction.putAll(map);
		ioFunction.put(".hex", BinaryIOFunction.hex);
	}
	
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

	public void write(File target) throws IOException
	{
		String extension = target.getName();
		int i = extension.lastIndexOf('.');
		if(i >= 0)
			extension = extension.substring(i);
		ioFunction.getOrDefault(extension, BinaryIOFunction.binary).write(target, this.data, this.endianess);
	}
	
	public static int[] read(File target, boolean endian) throws IOException
	{
		String extension = target.getName();
		int i = extension.lastIndexOf('.');
		if(i >= 0)
			extension = extension.substring(i);
		List<Integer> list = ioFunction.getOrDefault(extension, BinaryIOFunction.binary).read(target, endian);
		int[] ret = new int[list.size()];
		for(i = 0; i < ret.length; i++)
			ret[i] = list.get(i);
		return ret;
	}
	
}
