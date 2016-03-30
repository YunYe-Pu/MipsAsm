package mipsAsm.assembler.instruction;

import java.util.HashMap;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.util.LabelOccurence;

public interface LinkableInstruction extends Instruction
{
	void link(HashMap<String, LabelOccurence> labelMap, int addr) throws AsmError;
}
