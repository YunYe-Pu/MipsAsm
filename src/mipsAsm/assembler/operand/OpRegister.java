package mipsAsm.assembler.operand;

import java.util.HashMap;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.util.AsmWarning;

public class OpRegister extends Operand
{
	public static final String format = "\\$\\w*";
	
	protected static final HashMap<String, Integer> regNameMap = new HashMap<>(59);
	
	protected final int regNum;
	protected int mask;
	
	public OpRegister(String token, Assembler assembler) throws AsmError
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
			if(token.equals("$at"))
				assembler.handleWarning(new AsmWarning("Deprecated register name",
						"Using $at in assembly code is not recommended."));
			this.regNum = regNum;
			return;
		}
		throw new AsmError("Unknown Register", "Unknown register name \"" + token + "\"");
	}
	
	public OpRegister(int regNum)
	{
		this.regNum = regNum;
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
		
		regNameMap.put("$Index", 0);
		regNameMap.put("$Random", 1);
		regNameMap.put("$EntryLo0", 2);
		regNameMap.put("$EntryLo1", 3);
		regNameMap.put("$Context", 4);
		regNameMap.put("$PageMask", 5);
		regNameMap.put("$Wired", 6);
		regNameMap.put("$BadVAddr", 8);
		regNameMap.put("$Count", 9);
		regNameMap.put("$EntryHi", 10);
		regNameMap.put("$Compare", 11);
		regNameMap.put("$Status", 12);
		regNameMap.put("$Cause", 13);
		regNameMap.put("$EPC", 14);
		regNameMap.put("$PRId", 15);
		regNameMap.put("$Config", 16);
		regNameMap.put("$LLAddr", 17);
		regNameMap.put("$WatchLo", 18);
		regNameMap.put("$WatchHi", 19);
		regNameMap.put("$Debug", 23);
		regNameMap.put("$DEPC", 24);
		regNameMap.put("$PerfCnt", 25);
		regNameMap.put("$ErrCtl", 26);
		regNameMap.put("$CacheErr", 27);
		regNameMap.put("$CacheLo", 28);//TagLo, DataLo
		regNameMap.put("$CacheHi", 29);//TagHi, DataHi
		regNameMap.put("$ErrorEPC", 30);
		regNameMap.put("$DESAVE", 31);
	}
	
}
