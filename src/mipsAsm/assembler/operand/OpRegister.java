package mipsAsm.assembler.operand;

import java.util.HashMap;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.util.AsmWarning;

public class OpRegister extends Operand
{
	public static final String format = "\\$\\w*";
	
	protected static final HashMap<String, Integer> regNameMap = new HashMap<>(59);
	
	protected final int regNum;
	protected int mask;
	
	public OpRegister(String token) throws AsmError
	{
		if(token.matches("\\$\\d{1,2}"))
		{
			int num = Integer.parseInt(token.substring(1));
			if(num >= 0 && num < 32)
			{
				this.regNum = num;
				return;
			}
		}
		Integer regNum = regNameMap.get(token);
		if(regNum != null)
		{
			this.regNum = regNum;
			return;
		}
		throw new AsmError("Unknown Register", "Unknown register name \"" + token + "\"");
	}

	@Override
	public int getEncoding()
	{
		return this.regNum & this.mask;
	}
	
	@Override
	public AsmWarning setWidth(int width)
	{
		this.mask = ((1 << width) - 1);
		return null;
	}

	static
	{
		regNameMap.put("$zero", 0);
		regNameMap.put("$at", 1);
		regNameMap.put("$v0", 2);
		regNameMap.put("$v1", 3);
		regNameMap.put("$a0", 4);
		regNameMap.put("$a1", 5);
		regNameMap.put("$a2", 6);
		regNameMap.put("$a3", 7);
		regNameMap.put("$a4", 8);
		regNameMap.put("$a5", 9);
		regNameMap.put("$a6", 10);
		regNameMap.put("$a7", 11);
		regNameMap.put("$t0", 8);
		regNameMap.put("$t1", 9);
		regNameMap.put("$t2", 10);
		regNameMap.put("$t3", 11);
		regNameMap.put("$t4", 12);
		regNameMap.put("$t5", 13);
		regNameMap.put("$t6", 14);
		regNameMap.put("$t7", 15);
		regNameMap.put("$s0", 16);
		regNameMap.put("$s1", 17);
		regNameMap.put("$s2", 18);
		regNameMap.put("$s3", 19);
		regNameMap.put("$s4", 20);
		regNameMap.put("$s5", 21);
		regNameMap.put("$s6", 22);
		regNameMap.put("$s7", 23);
		regNameMap.put("$t8", 24);
		regNameMap.put("$t9", 25);
		regNameMap.put("$k0", 26);
		regNameMap.put("$k1", 27);
		regNameMap.put("$gp", 28);
		regNameMap.put("$sp", 29);
		regNameMap.put("$fp", 30);
		regNameMap.put("$ra", 31);
	}
	
}
