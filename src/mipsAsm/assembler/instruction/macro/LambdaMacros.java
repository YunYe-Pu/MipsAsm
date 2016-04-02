package mipsAsm.assembler.instruction.macro;

import mipsAsm.assembler.exception.OpCountMismatchError;
import mipsAsm.assembler.exception.OpTypeMismatchError;
import mipsAsm.assembler.instruction.InstructionParser;
import mipsAsm.assembler.operand.OpConstant;
import mipsAsm.assembler.operand.OpImmediate;
import mipsAsm.assembler.operand.OpLabel;
import mipsAsm.assembler.operand.OpRegister;
import mipsAsm.assembler.operand.OpSplit;
import mipsAsm.assembler.operand.Operand;
import mipsAsm.assembler.util.AsmWarning;
import mipsAsm.assembler.util.InstructionFmt;
import mipsAsm.assembler.util.LinkType;
import mipsAsm.assembler.util.OperandFmt;

public final class LambdaMacros
{
	private LambdaMacros() {}
	
	public static final InstructionParser LI = (operands, warningHandler, instrList) ->
	{
		OperandFmt.RI.matches(operands);
		Operand[] opLUI = new Operand[3];
		opLUI[0] = new OpConstant(0);
		opLUI[1] = operands[0];
		opLUI[2] = new OpSplit(operands[1], 16);
		
		Operand[] opORI = new Operand[3];
		opORI[0] = operands[0];
		opORI[1] = operands[0];
		opORI[2] = new OpSplit(operands[1], 0);
		
		instrList.add(InstructionFmt.I.newInstance(0x0f, opLUI, null, warningHandler));
		instrList.add(InstructionFmt.I.newInstance(0x0d, opORI, null, warningHandler));
	};
	
	public static final InstructionParser LA = (operands, warningHandler, instrList) ->
	{
		if(operands.length != 2)
			throw new OpCountMismatchError(2, operands.length);
		if(!(operands[0] instanceof OpRegister))
			throw new OpTypeMismatchError(0, operands[0], OpRegister.class);
		
		if(operands[1] instanceof OpImmediate)
		{
			AsmWarning w = new AsmWarning("Deprecated operand", "Providing an immediate for operand 2 is deprecated as it might be incorrect.");
			warningHandler.handleWarning(w);
		}
		else if(!(operands[1] instanceof OpLabel))
			throw new OpTypeMismatchError(1, Operand.getTypeName(operands[1]), "label or immediate");
		
		Operand[] opLUI = new Operand[3];
		opLUI[0] = new OpConstant(0);
		opLUI[1] = operands[0];
		opLUI[2] = new OpSplit(operands[1], 16);
		
		Operand[] opORI = new Operand[3];
		opORI[0] = operands[0];
		opORI[1] = operands[0];
		opORI[2] = new OpSplit(operands[1], 0);
		
		instrList.add(InstructionFmt.I.newInstance(0x0f, opLUI, LinkType.ABSOLUTE_BYTE, warningHandler));
		instrList.add(InstructionFmt.I.newInstance(0x0d, opORI, LinkType.ABSOLUTE_BYTE, warningHandler));
	};

	
	
}
