package mipsAsm.assembler.util;

import java.util.HashMap;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.instruction.Instruction;
import mipsAsm.assembler.instruction.LinkableInstruction;
import mipsAsm.assembler.operand.LinkableOperand;
import mipsAsm.assembler.operand.Operand;

/**
 * This class defines formats of a machine instruction, and provides
 * the method to create an instruction based on that format.
 * 
 * @author YunYe Pu
 */
public class InstructionFmt
{
	public static final InstructionFmt R = new InstructionFmt(5, 5, 5, 5, 6);
	public static final InstructionFmt I = new InstructionFmt(5, 5, 16);
	public static final InstructionFmt J = new InstructionFmt(26);
	public static final InstructionFmt F20_6 = new InstructionFmt(20, 6);
	public static final InstructionFmt MFTC0 = new InstructionFmt(5, 5, 5, 8, 3);
	
	private final int[] fieldLength;

	public InstructionFmt(int... field)
	{
		this.fieldLength = field;
	}
	
	/**
	 * Create a new instruction from the instruction format and operands.
	 * @param opCode Operation code of the instruction.
	 * @param op Operands of the instruction.
	 * @param linkType The link type of the instruction. Provide null to obtain an unlinkable instruction,
	 * 	or an instance to obtain a linkable instruction.
	 * @param warningHandler The assembler to handle the warning.
	 * @return The new instruction generated.
	 * @throws AsmError
	 */
	public Instruction newInstance(int opCode, Operand[] op, LinkType linkType, Assembler warningHandler) throws AsmError
	{
		for(int i = 0; i < this.fieldLength.length; i++)
		{
			AsmWarning e = op[i].setWidth(this.fieldLength[i]);
			if(e != null)
				warningHandler.handleWarning(e);
		}
		if(linkType == null)
			return new StdInstr(opCode, op);
		else
			return new StdLinkableInstr(opCode, op, linkType);
	}
	
	public int[] splitBinary(int opField)
	{
		int[] ret = new int[this.fieldLength.length];
		for(int i = this.fieldLength.length - 1; i >= 0; i--)
		{
			ret[i] = opField & ((1 << this.fieldLength[i]) - 1);
			ret[i] = (ret[i] << (32 - this.fieldLength[i])) >> (32 - this.fieldLength[i]);//sign extend
			opField >>= this.fieldLength[i];
		}
		return ret;
	}

	public class StdInstr implements Instruction
	{
		private Operand[] operands;
		private int opCode;

		protected StdInstr(int opCode, Operand[] operands)
		{
			this.opCode = opCode & 0x3f;
			this.operands = operands;
		}
		
		@Override
		public int toBinary()
		{
			int ret = this.opCode;
			for(int i = 0; i < fieldLength.length; i++)
				ret = (ret << fieldLength[i]) | this.operands[i].getEncoding();
			return ret;
		}
	}
	
	public class StdLinkableInstr implements LinkableInstruction
	{
		private Operand[] operands;
		private int opCode;
		private LinkType linkType;
		private boolean linked;

		protected StdLinkableInstr(int opCode, Operand[] operands, LinkType linkType)
		{
			this.opCode = opCode;
			this.operands = operands;
			this.linkType = linkType;
			this.linked = false;
		}
		
		@Override
		public int toBinary()
		{
			int ret = this.opCode;
			for(int i = 0; i < fieldLength.length; i++)
				ret = (ret << fieldLength[i]) | this.operands[i].getEncoding();
			return ret;
		}

		@Override
		public void link(HashMap<String, LabelOccurence> labelMap, int addr) throws AsmError
		{
			if(this.linked) return;
			for(Operand op : this.operands)
			{
				if(op instanceof LinkableOperand)
					((LinkableOperand)op).link(labelMap, addr, this.linkType);
			}
			this.linked = true;
		}
	}
}
