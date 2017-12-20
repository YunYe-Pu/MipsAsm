package mipsAsm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public interface BinaryIOFunction
{
	public void write(File target, int[] data, boolean endian) throws IOException;
	public List<Integer> read(File target, boolean endian) throws IOException;
	
	
	public static final BinaryIOFunction binary = new BinaryIOFunction() {
		@Override
		public void write(File target, int[] data, boolean endian) throws IOException
		{
			try(FileOutputStream stream = new FileOutputStream(target))
			{
				if(endian)
				{
					for(int i : data)
					{
						stream.write((i >> 24) & 0xff);
						stream.write((i >> 16) & 0xff);
						stream.write((i >>  8) & 0xff);
						stream.write(i & 0xff);
					}
				}
				else
				{
					for(int i : data)
					{
						stream.write(i & 0xff);
						stream.write((i >>  8) & 0xff);
						stream.write((i >> 16) & 0xff);
						stream.write((i >> 24) & 0xff);
					}
				}
			}
		}
		
		@Override
		public List<Integer> read(File target, boolean endian) throws IOException
		{
			ArrayList<Integer> binary = new ArrayList<>();
			try(FileInputStream s = new FileInputStream(target))
			{
				int count = 0;
				int data = 0;
				if(endian)
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
			return binary;
		}
	};
	
	public static final BinaryIOFunction hex = new BinaryIOFunction() {
		@Override
		public void write(File target, int[] data, boolean endian) throws IOException
		{
			try(PrintWriter output = new PrintWriter(target))
			{
				if(endian)//big-endian, output as normal
				{
					for(int i : data)
					{
						String s = Integer.toHexString(i);
						for(int j = s.length(); j < 8; j++)
							output.print('0');
						output.print(s);
					}
				}
				else//little-endian, output least-significant byte first
				{
					for(int val : data)
					{
						for(int i = 0; i < 4; i++)
						{
							String s = Integer.toHexString(val & 0xff);
							if(s.length() == 1)
								output.print('0');
							output.print(s);
							val >>= 8;
						}
					}
				}
			}
		}
		
		@Override
		public List<Integer> read(File target, boolean endian) throws IOException
		{
			ArrayList<Integer> binary = new ArrayList<>();
			try(FileInputStream s = new FileInputStream(target))
			{
				int count = 0;
				int data = 0;
				if(endian)
				{
					while(s.available() > 0)
					{
						int c = s.read();
						if(c == 10 || c == 13) continue;
						data = data << 4 | (charToInt(c) & 0xf);
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
			return binary;
		}
		
		private int charToInt(int c)
		{
			if(c >= '0' && c <= '9')
				return c - '0';
			if(c >= 'a' && c <= 'f')
				return c - 'a' + 10;
			if(c >= 'A' && c <= 'F')
				return c - 'A' + 10;
			return 0;
		}
	};
	
}
