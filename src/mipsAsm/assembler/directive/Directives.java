package mipsAsm.assembler.directive;

import java.util.ArrayList;
import java.util.HashMap;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.exception.OpTypeMismatchError;
import mipsAsm.assembler.instruction.Instruction;
import mipsAsm.assembler.operand.OpImmediate;
import mipsAsm.assembler.operand.OpLabel;
import mipsAsm.assembler.operand.OpString;
import mipsAsm.assembler.operand.Operand;
import mipsAsm.assembler.util.AsmWarning;

public class Directives
{
	public static final DirectiveHandler ASCIIZ = (operands, assembler, instructionList) ->
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
	
	public static final DirectiveHandler GLOBl = (operands, assembler, instructionList) ->
	{
		int opIndex = 0;
		for(Operand op : operands)
		{
			if(!(op instanceof OpLabel))
				throw new OpTypeMismatchError(opIndex, op, OpLabel.class);
			assembler.addGlobalLabel(((OpLabel)op).labelName);
		}
	};

	private static class BinaryHandler implements DirectiveHandler
	{
		private final int width;
		
		private BinaryHandler(int width)
		{
			this.width = width;
		}
		
		@Override
		public void handle(Operand[] operands, Assembler assembler, ArrayList<Instruction> instructionList) throws AsmError
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
	
	private static final HashMap<String, DirectiveHandler> handlerMap = new HashMap<>();
	
	static
	{
		handlerMap.put(".asciiz", ASCIIZ);
		handlerMap.put(".globl", GLOBl);
		handlerMap.put(".byte", new BinaryHandler(8));
		handlerMap.put(".half", new BinaryHandler(16));
		handlerMap.put(".word", new BinaryHandler(32));
	}
	
	public static DirectiveHandler getHandler(String directiveName) throws AsmError
	{
		DirectiveHandler handler = handlerMap.get(directiveName);
		if(handler == null)
			throw new AsmError("Unknown directive", "Unknown directive \"" + directiveName + "\".");
		return handler;
	}
	
}
