package mipsAsm.disassembler.util;

import java.util.function.Function;

public class DisassemblyEntry implements Function<int[], String>
{
	public final String format;
	public final int[] order;

	public DisassemblyEntry(String format, int... order)
	{
		this.format = format;
		this.order = order;
	}

	@Override
	public String apply(int[] param)
	{
		int[] value = new int[this.order.length];
		for(int i = 0; i < this.order.length; i++)
			value[i] = param[this.order[i]];
		return OpFormatter.format(this.format, value);
	}
	
}
