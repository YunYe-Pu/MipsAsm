package mipsAsm.assembler.operand;

import java.util.HashMap;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.util.LabelOccurence;
import mipsAsm.assembler.util.LinkType;

public interface LinkableOperand
{
	public void link(HashMap<String, LabelOccurence> labelMap, int sourceAddr, LinkType linkType) throws AsmError;
}
