package mipsAsm.assembler.instruction;

import java.util.HashMap;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.instruction.macro.BranchMacros;
import mipsAsm.assembler.instruction.macro.LambdaMacros;

public final class Instructions
{
	private Instructions() {}
	
	private static final HashMap<String, InstructionParser> mnemonicMap = new HashMap<>();
	
	static
	{
		for(StandardInstrParser i : StandardInstrParser.values())
			mnemonicMap.put(i.name().toLowerCase(), i);
		
		for(RIPairedInstrParser i : RIPairedInstrParser.values())
			mnemonicMap.put(i.name().toLowerCase(), i);
		
		for(BranchInstrParser i : BranchInstrParser.values())
			mnemonicMap.put(i.name().toLowerCase(), i);
		
		for(JumpInstrParser i : JumpInstrParser.values())
		{
			mnemonicMap.put(i.name().toLowerCase(), i);
			mnemonicMap.put(i.alias, i);
		}
		
		for(BranchMacros i : BranchMacros.values())
			mnemonicMap.put(i.name().toLowerCase(), i);
		
		mnemonicMap.put("li", LambdaMacros.LI);
		mnemonicMap.put("la", LambdaMacros.LA);
	}
	
	public static InstructionParser put(String key, InstructionParser value)
	{
		return mnemonicMap.put(key, value);
	}
	
	public static InstructionParser getParser(String mnemonic) throws AsmError
	{
		InstructionParser ret = mnemonicMap.get(mnemonic);
		if(ret == null)
			throw new AsmError("Unknown mnemonic", "Unknown mnemonic \"" + mnemonic + "\".");
		else
			return ret;
	}	
}
