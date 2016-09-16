package mipsAsm.assembler.instruction;

import java.util.ArrayList;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.operand.OpImmediate;
import mipsAsm.assembler.operand.OpRegister;
import mipsAsm.assembler.operand.Operand;

/**
 * The instruction parsers for R-type and I-type paired instructions, like ADD and ADDI.
 * These instructions will assemble into different instruction formats if provided with
 * different types of operands, so the programmer can write the same mnemonic for both
 * instructions.<br />
 * This class is dependent on the {@link StandardInstrParser} class, and when registering instructions
 * to the assembler, instructions here should be registered after all of the instructions
 * in StandardInstrParser class has been registered.
 * 
 * @author YunYe Pu
 */
public enum RIPairedInstrParser implements InstructionParser
{
	ADD (2, StandardInstrParser.ADD , StandardInstrParser.ADDI),
	ADDU(2, StandardInstrParser.ADDU, StandardInstrParser.ADDIU),
	AND (2, StandardInstrParser.AND , StandardInstrParser.ANDI),
	OR  (2, StandardInstrParser.OR  , StandardInstrParser.ORI),
	SLL (2, StandardInstrParser.SLLV, StandardInstrParser.SLL),
	SRL (2, StandardInstrParser.SRLV, StandardInstrParser.SRL),
	SRA (2, StandardInstrParser.SRAV, StandardInstrParser.SRA),
	SLT (2, StandardInstrParser.SLT , StandardInstrParser.SLTI),
	SLTU(2, StandardInstrParser.SLTU, StandardInstrParser.SLTIU),
	XOR (2, StandardInstrParser.XOR , StandardInstrParser.XORI),
	TEQ (1, StandardInstrParser.TEQ , StandardInstrParser.TEQI),
	TGE (1, StandardInstrParser.TGE , StandardInstrParser.TGEI),
	TGEU(1, StandardInstrParser.TGEU, StandardInstrParser.TGEIU),
	TLT (1, StandardInstrParser.TLT , StandardInstrParser.TLTI),
	TLTU(1, StandardInstrParser.TLTU, StandardInstrParser.TLTIU),
	TNE (1, StandardInstrParser.TNE , StandardInstrParser.TNEI),
	;
	
	private int opIndex;
	private int opCount;
	private InstructionParser parser1;
	private InstructionParser parser2;
	
	private RIPairedInstrParser(int opIndex, InstructionParser parserForR, InstructionParser parserForI)
	{
		this(opIndex, opIndex + 1, parserForR, parserForI);
	}
	
	private RIPairedInstrParser(int opIndex, int opCount, InstructionParser parserForR, InstructionParser parserForI)
	{
		this.opIndex = opIndex;
		this.opCount = opCount;
		this.parser1 = parserForR;
		this.parser2 = parserForI;
	}

	@Override
	public void parse(Operand[] operands, Assembler assembler, ArrayList<Instruction> instrList) throws AsmError
	{
		if(operands.length != this.opCount)
			throw new AsmError("Operand mismatch", "Expected " + this.opCount + " operand(s) but provided " + operands.length + ".");
		else if(operands[this.opIndex] instanceof OpRegister)
			this.parser1.parse(operands, assembler, instrList);
		else if(operands[this.opIndex] instanceof OpImmediate)
			this.parser2.parse(operands, assembler, instrList);
		else
			throw new AsmError("Operand mismatch", "Expected register or immediate for operand " + (this.opIndex + 1) + " but provided " + Operand.getTypeName(operands[this.opIndex].getClass()) + ".");
	}

}
