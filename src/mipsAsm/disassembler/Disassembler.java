package mipsAsm.disassembler;

import java.util.ArrayList;
import java.util.function.Function;

import mipsAsm.assembler.util.InstructionFmt;
import mipsAsm.disassembler.util.OpFormatter;

public final class Disassembler
{
	private Disassembler() {}
	
	@SuppressWarnings("unchecked")
	private static final Function<Integer, String>[] disasmMap = new Function[64];
	
	private static final ArrayList<Function<Integer, String>> idioms = new ArrayList<>();
	
	public static String disassemble(int binary)
	{
		int opCode = (binary >> 26) & 0x3f;
		String ret = null;
		for(Function<Integer, String> e : idioms)
		{
			ret = e.apply(binary);
			if(ret != null) break;
		}
		if(ret == null && disasmMap[opCode] != null)
			ret = disasmMap[opCode].apply(binary);
		if(ret == null)
			ret = String.format(".word\t0x%08x # Unrecognized instruction", binary);
		return ret;
	}
	
	public static StringBuilder disassemble(int[] binary)
	{
		StringBuilder ret = new StringBuilder();
		for(int i : binary)
		{
			ret.append(disassemble(i));
			ret.append('\n');
		}
		return ret;
	}
	
	static
	{
		for(int i = 0; i < 64; i++)
			disasmMap[i] = null;
		disasmMap[0] = OpSpecial.disassembly;
		disasmMap[1] = OpRegimm.disassembly;
		disasmMap[0x1c] = OpSpecial.disassembly2;
		disasmMap[0x10] = OpCp0.disassembly;
		for(Instructions i : Instructions.values())
			disasmMap[i.opCode] = i;
		
		idioms.add(i -> (i == 0? "nop": null));
		idioms.add(i -> (i == 0x40? "ssnop": null));
		idioms.add(i -> {
			if(((i >> 26) & 0x3f) == 0x04)
			{
				int[] param = InstructionFmt.I.splitBinary(i);
				if(param[0] == param[1])
					return OpFormatter.format("b\t%i", param[2]);
			}
			return null;
		});
		idioms.add(i -> {
			if(((i >> 26) & 0x3f) == 0x00 && (i & 0x3f) == 0x25)
			{
				int[] param = InstructionFmt.R.splitBinary(i);
				if(param[0] == 0)
					return OpFormatter.format("move\t%r, %r", param[2], param[1]);
				else if(param[1] == 0)
					return OpFormatter.format("move\t%r, %r", param[2], param[0]);					
			}
			return null;
		});
	}
}
