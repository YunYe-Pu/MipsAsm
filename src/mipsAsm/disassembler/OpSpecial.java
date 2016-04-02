package mipsAsm.disassembler;

import java.util.function.Function;

import mipsAsm.assembler.util.InstructionFmt;
import mipsAsm.disassembler.util.DisassemblyEntry;
import mipsAsm.disassembler.util.OpFormatter;

public final class OpSpecial
{
	private OpSpecial() {}
	
	private static final DisassemblyEntry[] entryMap = new DisassemblyEntry[64];
	private static final DisassemblyEntry[] entryMap2 = new DisassemblyEntry[64];
	
	static
	{
		for(int i = 0; i < 64; i++)
			entryMap[i] = entryMap2[i] = null;
		put("add",  0x20, 2, 0, 1);
		put("addu", 0x21, 2, 0, 1);
		put("and",  0x24, 2, 0, 1);
		put("div",  0x1a, 0, 1);
		put("divu", 0x1b, 0, 1);
		put("jalr", 0x09, 0);
		put("jr",   0x08, 0);
		put("mfhi", 0x10, 2);
		put("mflo", 0x12, 2);
		put("movn", 0x0b, 2, 0, 1);
		put("movz", 0x0a, 2, 0, 1);
		put("mthi", 0x11, 0);
		put("mtlo", 0x13, 0);
		put("mult", 0x18, 0, 1);
		put("multu",0x19, 0, 1);
		put("nor",  0x27, 2, 0, 1);
		put("or",   0x25, 2, 0, 1);
		put("sll",  "%r, %r, %i", 0x00, 2, 1, 3);
		put("sllv", 0x04, 2, 1, 0);
		put("slt",  0x2a, 2, 0, 1);
		put("sltu", 0x2b, 2, 0, 1);
		put("sra",  "%r, %r, %i", 0x03, 2, 1, 3);
		put("srav", 0x07, 2, 1, 0);
		put("srl",  "%r, %r, %i", 0x02, 2, 1, 3);
		put("srlv", 0x06, 2, 1, 0);
		put("sub",  0x22, 2, 0, 1);
		put("subu", 0x23, 2, 0, 1);
		put("sync", "", 0x0f);
		put("teq",  0x34, 0, 1);
		put("tge",  0x30, 0, 1);
		put("tgeu", 0x31, 0, 1);
		put("tlt",  0x32, 0, 1);
		put("tltu", 0x33, 0, 1);
		put("tne",  0x36, 0, 1);
		put("xor",  0x26, 2, 0, 1);
		
		put2("clz\t%r, %r", 0x20, 2, 0);
		put2("clo\t%r, %r", 0x21, 2, 0);
		put2("madd\t%r, %r", 0x00, 0, 1);
		put2("maddu\t%r, %r", 0x01, 0, 1);
		put2("mul\t%r, %r, %r", 0x02, 2, 0, 1);
		put2("msub\t%r, %r", 0x04, 0, 1);
		put2("msubu\t%r, %r", 0x05, 0, 1);
	}
	
	private static void put(String mnemonic, String format, int funcField, int... order)
	{
		entryMap[funcField] = new DisassemblyEntry(mnemonic + "\t" + format, order);
	}
	
	private static void put(String mnemonic, int funcField, int... order)
	{
		int l = order.length - 1;
		String fmt = "%r";
		while(l-- > 0) fmt = "%r, " + fmt;
		entryMap[funcField] = new DisassemblyEntry(mnemonic + "\t" + fmt, order);
	}
	
	private static void put2(String format, int funcField, int... order)
	{
		entryMap2[funcField] = new DisassemblyEntry(format, order);
	}
	
	public static final Function<Integer, String> disassembly = binary -> {
		int[] param = InstructionFmt.R.splitBinary(binary);
		DisassemblyEntry e = entryMap[param[4] & 63];
		if(e == null)
		{
			param = InstructionFmt.F20_6.splitBinary(binary);
			param[1] &= 63;
			if(param[1] == 0x0c)//syscall
				return OpFormatter.format("syscall\t%i", param[0]);
			else if(param[1] == 0x0d)//break
				return OpFormatter.format("break\t%i", param[0]);
			else
				return null;
		}
		else
			return e.apply(param);
	};
	
	public static final Function<Integer, String> disassembly2 = binary -> {
		int[] param = InstructionFmt.R.splitBinary(binary);
		DisassemblyEntry e = entryMap[param[4] & 63];
		if(e == null)
		{
			param = InstructionFmt.F20_6.splitBinary(binary);
			param[1] &= 63;
			if(param[1] == 0x3f)//sdbbp
				return OpFormatter.format("sdbbp\t%i", param[0]);
			else
				return null;
		}
		else
			return e.apply(param);
	};
}
