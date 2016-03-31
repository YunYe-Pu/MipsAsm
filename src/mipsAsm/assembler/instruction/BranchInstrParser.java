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
import mipsAsm.assembler.util.InstructionFmt;
import mipsAsm.assembler.util.LinkType;

/**
 * Instruction parsers for branch instructions.
 * 
 * @author YunYe Pu
 */
public enum BranchInstrParser implements InstructionParser
{
	B       (0, 0x04, 0, 0, -1),
	BAL     (1, 0x01, 0, 0x11, -1),
	BEQ     (2, 0x04, -1, -2, -3),
	BEQL    (2, 0x14, -1, -2, -3),
	BGEZ    (1, 0x01, -1, 0x01, -2),
	BGEZAL  (1, 0x01, -1, 0x11, -2),
	BGEZALL (1, 0x01, -1, 0x13, -2),
	BGEZL   (1, 0x01, -1, 0x03, -2),
	BGTZ    (1, 0x07, -1, 0, -2),
	BGTZL   (1, 0x17, -1, 0, -2),
	BLEZ    (1, 0x06, -1, 0, -2),
	BLEZL   (1, 0x16, -1, 0, -2),
	BLTZ    (1, 0x01, -1, 0x00, -2),
	BLTZAL  (1, 0x01, -1, 0x10, -2),
	BLTZALL (1, 0x01, -1, 0x12, -2),
	BLTZL   (1, 0x01, -1, 0x02, -2),
	BNE     (2, 0x05, -1, -2, -3),
	BNEL    (2, 0x15, -1, -2, -3),
	;
	
	private final InstructionFmt instrFormat;
	private final int regCount;
	private final LinkType linkType;
	private final int opCode;
	private final int[] fields;

	private BranchInstrParser(InstructionFmt instrFormat, int regCount, LinkType linkType, int opCode, int... fields)
	{
		this.instrFormat = instrFormat;
		this.regCount = regCount;
		this.linkType = linkType;
		this.opCode = opCode;
		this.fields = fields;
	}
	
	private BranchInstrParser(int regCount, int opCode, int... fields)
	{
		this(InstructionFmt.I, regCount, LinkType.RELATIVE_WORD, opCode, fields);
	}
	
	@Override
	public void parse(Operand[] operands, AsmWarningHandler warningHandler, ArrayList<Instruction> instrList) throws AsmError
	{
		this.checkOpType(operands, warningHandler);
		Operand[] ops = new Operand[this.fields.length];
		for(int i = 0; i < this.fields.length; i++)
		{
			if(this.fields[i] < 0)
				ops[i] = operands[-this.fields[i] - 1];
			else
				ops[i] = new OpConstant(this.fields[i]);
		}
		
		instrList.add(this.instrFormat.newInstance(this.opCode, ops, this.linkType, warningHandler));
	}
	
	private void checkOpType(Operand[] operands, AsmWarningHandler warningHandler) throws AsmError
	{
		if(operands.length != this.regCount + 1)
			throw new OpCountMismatchError(this.regCount + 1, operands.length);
		int i;
		for(i = 0; i < this.regCount; i++)
		{
			if(!(operands[i] instanceof OpRegister))
				throw new OpTypeMismatchError(i, operands[i], OpRegister.class);
		}
		
		if(operands[i] instanceof OpImmediate)
		{
			AsmWarning w = new AsmWarning("Deprecated operand", "Providing an immediate for operand " + (i + 1) + " is deprecated as it might be incorrect.");
			warningHandler.handleWarning(w);
		}
		else if(!(operands[i] instanceof OpLabel))
			throw new OpTypeMismatchError(i, Operand.getTypeName(operands[i]), "label or immediate");
	}

}
