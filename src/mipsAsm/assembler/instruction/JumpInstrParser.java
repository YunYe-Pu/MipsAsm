package mipsAsm.assembler.instruction;

import java.util.ArrayList;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.exception.OpCountMismatchError;
import mipsAsm.assembler.exception.OpTypeMismatchError;
import mipsAsm.assembler.operand.OpConstant;
import mipsAsm.assembler.operand.OpImmediate;
import mipsAsm.assembler.operand.OpLabel;
import mipsAsm.assembler.operand.OpRegister;
import mipsAsm.assembler.operand.Operand;
import mipsAsm.assembler.util.AsmWarning;
import mipsAsm.assembler.util.AsmWarningHandler;
import mipsAsm.assembler.util.InstrFmt;
import mipsAsm.assembler.util.LinkType;

public enum JumpInstrParser implements InstructionParser
{
	J  ("jr"  , 2, 0, 0, 0, 8),
	JAL("jalr", 3, 0, 0x1f, 0, 0x09);
	
	private int[] data;
	public final String alias;
	
	private JumpInstrParser(String alias, int... data)
	{
		this.data = data;
		this.alias = alias;
	}
	
	@Override
	public void parse(Operand[] operands, AsmWarningHandler warningHandler, ArrayList<Instruction> instrList) throws AsmError
	{
		if(operands.length != 1)
			throw new OpCountMismatchError(1, operands.length);
		
		if(operands[0] instanceof OpImmediate)
		{
			AsmWarning w = new AsmWarning("Deprecated operand", "Providing an immediate for operand 1 is deprecated as it might be incorrect.");
			warningHandler.handleWarning(w);
			instrList.add(InstrFmt.J.newInstance(data[0], operands.clone(), null, warningHandler));
		}
		else if(operands[0] instanceof OpLabel)
			instrList.add(InstrFmt.J.newInstance(data[0], operands.clone(), LinkType.ABSOLUTE_WORD, warningHandler));
		else if(operands[0] instanceof OpRegister)
		{
			Operand[] ops = new Operand[5];
			ops[0] = operands[0];
			ops[1] = new OpConstant(data[1]);
			ops[2] = new OpConstant(data[2]);
			ops[3] = new OpConstant(data[3]);
			ops[4] = new OpConstant(data[4]);
			instrList.add(InstrFmt.R.newInstance(0, ops, null, warningHandler));
		}
		else
			throw new OpTypeMismatchError(0,Operand.getTypeName(operands[0]), "register, immediate, or label");
	}
	
}
