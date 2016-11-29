package mipsAsm.disassembler;

import java.util.function.Function;

import mipsAsm.disassembler.util.OpFormatter;

public final class OpCp0
{
	private OpCp0() {}
	
	public static final Function<Integer, String> disassembly = binary -> {
		if((binary & 0x03e00000) == 0)//mfc0
		{
			return OpFormatter.format("mfc0 %r, %0", (binary >> 16) & 31, (binary >> 11) & 31, binary & 7);
		}
		else if((binary & 0x03e00000) == 0x00800000)//mtc0
		{
			return OpFormatter.format("mtc0 %r, %0", (binary >> 16) & 31, (binary >> 11) & 31, binary & 7);
		}
		else if((binary & 0x02000000) != 0)
		{
			switch(binary & 0x3e) {
			case 1:
				return "tlbr";
			case 2:
				return "tlbwi";
			case 6:
				return "tlbwr";
			case 8:
				return "tlbp";
			case 24:
				return "eret";
			case 32:
				return "wait";
			default:
				return null;
			}
		}
		else
			return null;
	};
}
