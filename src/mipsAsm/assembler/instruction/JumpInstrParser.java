package mipsAsm.assembler.instruction;

import java.util.ArrayList;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.exception.OpCountMismatchError;
import mipsAsm.assembler.exception.OpTypeMismatchError;
import mipsAsm.assembler.operand.OpConstant;
import mipsAsm.assembler.operand.OpImmediate;
import mipsAsm.assembler.operand.OpLabel;
import mipsAsm.assembler.operand.OpRegister;
import mipsAsm.assembler.operand.Operand;
import mipsAsm.assembler.util.AsmWarning;
import mipsAsm.assembler.util.InstructionFmt;
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
	public void parse(Operand[] operands, Assembler assembler, ArrayList<Instruction> instrList) throws AsmError
	{
		if(assembler.isInDelaySlot())
			assembler.handleWarning(new AsmWarning("Delay slot misuse", "A jump instruction is placed in the delay slot of another instruction."));
		if(operands.length != 1)
			throw new OpCountMismatchError(1, operands.length);
		
		if(operands[0] instanceof OpImmediate)
		{
			AsmWarning w = new AsmWarning("Deprecated operand", "Providing an immediate for operand 1 is deprecated as it might be incorrect.");
			assembler.handleWarning(w);
			instrList.add(InstructionFmt.J.newInstance(data[0], operands.clone(), null, assembler));
		}
		else if(operands[0] instanceof OpLabel)
			instrList.add(InstructionFmt.J.newInstance(data[0], operands.clone(), LinkType.ABSOLUTE_WORD, assembler));
		else if(operands[0] instanceof OpRegister)
		{
			Operand[] ops = new Operand[5];
			ops[0] = operands[0];
			ops[1] = new OpConstant(data[1]);
			ops[2] = new OpConstant(data[2]);
			ops[3] = new OpConstant(data[3]);
			ops[4] = new OpConstant(data[4]);
			instrList.add(InstructionFmt.R.newInstance(0, ops, null, assembler));
		}
		else
			throw new OpTypeMismatchError(0, Operand.getTypeName(operands[0]), "register, immediate, or label");
		assembler.setNextDelaySlot();
	}
	
}
