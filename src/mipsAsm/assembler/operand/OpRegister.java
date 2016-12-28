package mipsAsm.assembler.operand;

import java.util.HashMap;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.util.AsmWarning;

public class OpRegister extends Operand
{
	public static final String format = "\\$\\w*";
	
	protected static final HashMap<String, Integer> regNameMap = new HashMap<>(59);
	protected static final HashMap<String, Integer> regNameMapCP0 = new HashMap<>(59);
	protected static final HashMap<String, Integer> regSelMapCP0 = new HashMap<>(10);
	
	protected final int regNum;
	protected int mask;
	
	public OpRegister(int regNum)
	{
		this.regNum = regNum;
		this.mask = -1;
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
	
	public static OpRegister newInstance(String token, Assembler assembler) throws AsmError
	{
		if(token.matches("\\$\\d{1,2}"))
		{
			int num = Integer.parseInt(token.substring(1));
			if(num >= 0 && num < 32)
				return new OpRegister(num);
			else
				throw new AsmError("Unknown Register", "Unknown register name \"" + token + "\"");
		}
		Integer regNum = regNameMap.get(token);
		if(regNum != null)
		{
			if(regNum == 1)//$at
				assembler.handleWarning(new AsmWarning("Deprecated register name",
						"Using $at in assembly code is not recommended."));
			return new OpRegister(regNum);
		}
		regNum = regNameMapCP0.get(token);
		if(regNum != null)
			return new OpCp0Register(regNum, regSelMapCP0.getOrDefault(token, 0));
		throw new AsmError("Unknown Register", "Unknown register name \"" + token + "\"");
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
		
		regNameMapCP0.put("$Index",    0);
		regNameMapCP0.put("$Random",   1);
		regNameMapCP0.put("$EntryLo0", 2);
		regNameMapCP0.put("$EntryLo1", 3);
		regNameMapCP0.put("$Context",  4);
		regNameMapCP0.put("$PageMask", 5);
		regNameMapCP0.put("$Wired",    6);
		regNameMapCP0.put("$BadVAddr", 8);
		regNameMapCP0.put("$Count",    9);
		regNameMapCP0.put("$EntryHi",  10);
		regNameMapCP0.put("$Compare",  11);
		regNameMapCP0.put("$Status",   12);
		regNameMapCP0.put("$Cause",    13);
		regNameMapCP0.put("$EPC",      14);
		regNameMapCP0.put("$PRId",     15);
		regNameMapCP0.put("$Config",   16);
		regNameMapCP0.put("$Config1",  16);
		regNameMapCP0.put("$Config2",  16);
		regNameMapCP0.put("$Config3",  16);
		regNameMapCP0.put("$LLAddr",   17);
		regNameMapCP0.put("$WatchLo",  18);
		regNameMapCP0.put("$WatchHi",  19);
		regNameMapCP0.put("$Debug",    23);
		regNameMapCP0.put("$DEPC",     24);
		regNameMapCP0.put("$PerfCnt",  25);
		regNameMapCP0.put("$ErrCtl",   26);
		regNameMapCP0.put("$CacheErr", 27);
		regNameMapCP0.put("$TagLo",    28);
		regNameMapCP0.put("$DataLo",   28);
		regNameMapCP0.put("$TagHi",    29);
		regNameMapCP0.put("$DataHi",   29);
		regNameMapCP0.put("$ErrorEPC", 30);
		regNameMapCP0.put("$DESAVE",   31);

		regSelMapCP0.put("$Config1", 1);
		regSelMapCP0.put("$Config2", 2);
		regSelMapCP0.put("$Config3", 3);
		regSelMapCP0.put("$DataLo",  1);
		regSelMapCP0.put("$DataHi",  1);
	}
	
	public static class OpCp0Register extends OpRegister
	{
		protected int sel;
		
		public OpCp0Register(int num, int sel)
		{
			super(num);
			this.sel = sel;
		}

		@Override
		public int getEncoding()
		{
			return ((this.regNum << 11) | this.sel) & this.mask;
		}
		
		public void setSelField(int sel)
		{
			this.sel = sel;
		}
	}
	
}
