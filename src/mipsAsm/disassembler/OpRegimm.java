package mipsAsm.disassembler;

import java.util.function.Function;

import mipsAsm.assembler.util.InstructionFmt;
import mipsAsm.disassembler.util.DisassemblyEntry;

public final class OpRegimm
{
	private OpRegimm() {}
	
	private static final DisassemblyEntry[] entryMap = new DisassemblyEntry[32];
	
	static
	{
		for(int i = 0; i < 32; i++)
			entryMap[i] = null;
		put("bltz", 0);
		put("bgez", 1);
		put("bltzl", 2);
		put("bgezl", 3);
		put("tgei", 8);
		put("tgeiu", 9);
		put("tlti", 10);
		put("tltiu", 11);
		put("teqi", 12);
		put("tnei", 14);
		put("bltzal", 16);
		put("bgezal", 17);
		put("bltzall", 18);
		put("bgezall", 19);
	}
	
	private static void put(String mnemonic, int rtField)
	{
		entryMap[rtField] = new DisassemblyEntry(mnemonic + "\t%r, %i", 0, 2);
	}
	
	public static final Function<Integer, String> disassembly = binary -> {
		int[] param = InstructionFmt.I.splitBinary(binary);
		DisassemblyEntry e = entryMap[param[1] & 31];
		if(e == null)
			return null;
		else
			return e.apply(param);
	};

}
