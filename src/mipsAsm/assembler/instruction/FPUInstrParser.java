package mipsAsm.assembler.instruction;

import java.util.ArrayList;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.operand.Operand;
import mipsAsm.assembler.util.AsmWarningHandler;

public enum FPUInstrParser implements InstructionParser
{
	;
	
	@Override
	public void parse(Operand[] operands, AsmWarningHandler warningHandler, ArrayList<Instruction> instrList) throws AsmError
	{
		// TODO Auto-generated method stub

	}
	
	public void register()
	{
		
	}

}
