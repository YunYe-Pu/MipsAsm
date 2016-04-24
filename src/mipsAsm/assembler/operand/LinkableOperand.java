package mipsAsm.assembler.operand;

import java.util.HashMap;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.util.LabelOccurence;
import mipsAsm.assembler.util.LinkType;

public abstract class LinkableOperand extends Operand
{
	public abstract void link(HashMap<String, LabelOccurence> labelMap, int sourceAddr, LinkType linkType) throws AsmError;
}
