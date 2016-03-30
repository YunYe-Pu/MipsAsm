package mipsAsm.disassembler.util;

public class OpFormatter
{
	private static final String[] regNames = {
		"$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
		"$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
		"$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
		"$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"};
	
	private static String asReg(int value)
	{
		return regNames[value & 31];
	}
	
	private static String asImm(int value)
	{
		if(value >= 0)
			return "0x" + Integer.toHexString(value);
		else
			return "-0x" + Integer.toHexString(-value);
	}
	
	public static String format(String format, int... values)
	{
		StringBuilder ret = new StringBuilder();
		boolean f = false;
		int i = 0;
		for(char c : format.toCharArray())
		{
			if(f)
			{
				switch(c) {
				case 'r'://register
					ret.append(asReg(values[i++])); break;
				case 'i'://immediate
					ret.append(asImm(values[i++])); break;
				default:
					ret.append(c);
				}
				f = false;
			}
			else
			{
				if(c == '%')
					f = true;
				else
					ret.append(c);
			}
		}
		return ret.toString();
	}
}
