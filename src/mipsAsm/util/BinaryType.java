package mipsAsm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Pattern;

public enum BinaryType
{
	COE,
	HEX,
	BIN,
	;
	
	public static BinaryType getType(File file)
	{
		String extension = file.getName();
		int i = extension.lastIndexOf('.');
		if(i >= 0)
			extension = extension.substring(i);
		if(".coe".equalsIgnoreCase(extension))
			return COE;
		else if(".hex".equalsIgnoreCase(extension))
			return HEX;
		else
			return BIN;
	}

	private static final Pattern COE_PREFIX1 = Pattern.compile("memory_initialization_radix=16;", Pattern.CASE_INSENSITIVE);
	private static final Pattern COE_PREFIX2 = Pattern.compile("memory_initialization_vector=", Pattern.CASE_INSENSITIVE);
	private static final Pattern COE_VECTOR_PATTERN = Pattern.compile("[0-9A-Fa-f]{8}[,;]");
	
	public static int[] read(File input, boolean endianess) throws IOException
	{
		ArrayList<Integer> binary = new ArrayList<>();
		try
		{
			switch(getType(input)) {
			case COE:
			try(Scanner scanner = new Scanner(input))
			{
				scanner.next(COE_PREFIX1);
				scanner.next(COE_PREFIX2);
				while(scanner.hasNext(COE_VECTOR_PATTERN))
					binary.add(Integer.parseUnsignedInt(scanner.next(COE_VECTOR_PATTERN).substring(0, 8), 16));
			}
			break;
			case HEX:
			try(FileInputStream s = new FileInputStream(input))
			{
				int count = 0;
				int data = 0;
				if(endianess)
				{
					while(s.available() > 0)
					{
						data = data << 4 | (charToInt(s.read()) & 0xf);
						count++;
						if(count == 8)
						{
							binary.add(data);
							count = 0;
						}
					}
				}
				else
				{
					while(s.available() > 1)
					{
						data = ((data >> 8) & 0xffffff) | (charToInt(s.read()) << 28) |  (charToInt(s.read()) << 24);
						count++;
						if(count == 4)
						{
							binary.add(data);
							count = 0;
						}
					}
				}
			}
			break;
			case BIN:
			try(FileInputStream s = new FileInputStream(input))
			{
				int count = 0;
				int data = 0;
				if(endianess)
				{
					while(s.available() > 0)
					{
						data = data << 8 | (s.read() & 0xff);
						count++;
						if(count == 4)
						{
							binary.add(data);
							count = 0;
						}
					}
				}
				else
				{
					while(s.available() > 0)
					{
						data = ((data >> 8) & 0xffffff) | ((s.read() & 0xff) << 24);
						count++;
						if(count == 4)
						{
							binary.add(data);
							count = 0;
						}
					}
				}
			}
			break;
			}
		}
		catch(NumberFormatException | InputMismatchException e)
		{
			return null;
		}
		int[] ret = new int[binary.size()];
		for(int i = 0; i < ret.length; i++)
			ret[i] = binary.get(i);
		return ret;
	}
	
	private static int charToInt(int c)
	{
		if(c >= '0' && c <= '9')
			return c - '0';
		if(c >= 'a' && c <= 'f')
			return c - 'a' + 10;
		if(c >= 'A' && c <= 'F')
			return c - 'A' + 10;
		return 0;
	}
	
}
