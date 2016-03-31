package mipsAsm.assembler.operand;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.util.AsmWarning;

public class OpImmediate extends Operand
{
	public static final String format = "-?\\d(x[0-9a-fA-F])?[0-9a-fA-F]*";
	
	protected int value;
	
	public OpImmediate(int value)
	{
		this.value = value;
	}
	
	public OpImmediate(String token) throws AsmError
	{
		try
		{
			if(token.matches("0x[0-9a-fA-F]+"))//hex positive
				this.value = Integer.parseUnsignedInt(token.substring(2), 16);
			else if(token.matches("-0x[0-9a-fA-F]+"))//hex negative
				this.value = -Integer.parseUnsignedInt(token.substring(3), 16);
			else if(token.matches("0[0-7]+"))//octal positive
				this.value = Integer.parseUnsignedInt(token.substring(1), 8);
			else if(token.matches("-0[0-7]+"))//octal negative
				this.value = -Integer.parseUnsignedInt(token.substring(2), 8);
			else if(token.matches("-?[1-9]\\d*"))//decimal; the first digit should not be 0 except for zero.
				this.value = Integer.parseInt(token);
			else if(token.matches("-?0"))
				this.value = 0;
			else
				throw new AsmError("Incorrect number format", "Number \"" + token + "\" is in wrong format.");
		}
		catch(NumberFormatException e)
		{
			throw new AsmError("Incorrect number format", "Number \"" + token + "\" is in wrong format.");
		}
	}
	
	@Override
	public AsmWarning setWidth(int width)
	{
		int mask;
		if(width < 32)
			mask = ((1 << width) - 1);
		else
			mask = 0xffffffff;
		int oldValue = this.value;
		this.value &= mask;
		mask = ~mask;
		if((oldValue & mask) != 0 && (oldValue & mask) != mask)
			return new AsmWarning("Immediate truncated", "The immediate is truncated to fit in a " + width + "-bit field.");
		else
			return null;
	}
	
	@Override
	public int getEncoding()
	{
		return this.value;
	}

}
