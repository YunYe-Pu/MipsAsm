package mipsAsm.assembler.directive;

import java.util.ArrayList;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.exception.OpTypeMismatchError;
import mipsAsm.assembler.instruction.Instruction;
import mipsAsm.assembler.instruction.InstructionParser;
import mipsAsm.assembler.operand.OpImmediate;
import mipsAsm.assembler.operand.OpLabel;
import mipsAsm.assembler.operand.OpString;
import mipsAsm.assembler.operand.Operand;
import mipsAsm.assembler.util.AsmWarning;
import mipsAsm.assembler.util.OperandFmt;

public class Directives
{
	public static final InstructionParser ASCIIZ = (operands, assembler, instructionList) ->
	{
		int opIndex = 0;
		int charIndex = 0;
		int shift = 0;
		boolean bigEndian = assembler.getEndianess();
		int value = 0;
		for(Operand op : operands)
		{
			if(!(op instanceof OpString))
				throw new OpTypeMismatchError(opIndex, op, OpString.class);
			OpString opString = (OpString)op;
			for(charIndex = 0; charIndex < opString.length; charIndex++)
			{
				value |= (((int)opString.content[charIndex]) & 0xff) << (bigEndian? 24 - shift: shift);
				if(shift == 24)
				{
					instructionList.add(new DataInstruction(value));
					shift = 0;
					value = 0;
				}
				else
					shift += 8;
			}
			opIndex++;
		}
		instructionList.add(new DataInstruction(value));
	};
	
	public static final InstructionParser GLOBl = (operands, assembler, instructionList) ->
	{
		int opIndex = 0;
		for(Operand op : operands)
		{
			if(!(op instanceof OpLabel))
				throw new OpTypeMismatchError(opIndex, op, OpLabel.class);
			assembler.addGlobalLabel(((OpLabel)op).labelName);
		}
	};
	
	public static final InstructionParser SPACE = (operands, assembler, instructionList) ->
	{
		OperandFmt.I.matches(operands);
		int i = operands[0].getEncoding();
		if(i <= 0)
			assembler.handleWarning(new AsmWarning("Non-positive spacing", "Provided a non-positive number of spacing."));
		Instruction inst = new DataInstruction(0);
		while(i-- > 0)
			instructionList.add(inst);
	};

	public static class BinaryHandler implements InstructionParser
	{
		private final int width;
		
		public BinaryHandler(int width)
		{
			this.width = width;
		}
		
		@Override
		public void parse(Operand[] operands, Assembler assembler, ArrayList<Instruction> instructionList) throws AsmError
		{
//			int count = 0;
//			int value = 0;
//			boolean bigEndian = assembler.getEndianess();
//			for(int i = 0; i < operands.length; i++)
//			{
//				if(!(operands[i] instanceof OpImmediate))
//					throw new OpTypeMismatchError(i, operands[i], OpImmediate.class);
//				AsmWarning w = operands[i].setWidth(this.width);
//				if(w != null)
//					assembler.handleWarning(w);
//				
//				
//				
//				value = (value << this.width) | operands[i].getEncoding();
//				count += this.width;
//				if(count == 32)
//				{
//					instructionList.add(new DataInstruction(value));
//					count = 0;
//					value = 0;
//				}
//			}
//			
//			if(count != 0)
//				instructionList.add(new DataInstruction(value << (32 - count)));
			int shift = 0;
			int value = 0;
			boolean bigEndian = assembler.getEndianess();
			for(int i = 0; i < operands.length; i++)
			{
				if(!(operands[i] instanceof OpImmediate))
					throw new OpTypeMismatchError(i, operands[i], OpImmediate.class);
				AsmWarning w = operands[i].setWidth(this.width);
				if(w != null)
					assembler.handleWarning(w);
				
				value |= operands[i].getEncoding() << (bigEndian? 32 - this.width - shift: shift);
				shift += this.width;
				if(shift == 32)
				{
					instructionList.add(new DataInstruction(value));
					shift = 0;
					value = 0;
				}
			}
			if(shift != 0)
				instructionList.add(new DataInstruction(value));
		}
	}
	
	
}
