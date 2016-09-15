package mipsAsm.assembler.instruction;

import java.util.ArrayList;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.operand.OpConstant;
import mipsAsm.assembler.operand.Operand;
import mipsAsm.assembler.util.InstructionFmt;
import mipsAsm.assembler.util.OperandFmt;

/**
 * This class provide some of the standard unlinkable instructions
 * in MIPS32 instruction set. Most of the CPU instructions are
 * included; none of coprocessor-related instructions are included.
 * Also included some assembly idioms, like NOP and MOVE.<br />
 * All the instructions here will assemble into one machine instruction.
 * 
 * @author YunYe Pu
 */
public enum StandardInstrParser implements InstructionParser
{
	ADD     (InstructionFmt.R, OperandFmt.R3 , 0x00, -2, -3, -1, 0, 0x20),
	ADDI    (InstructionFmt.I, OperandFmt.RRI, 0x08, -2, -1, -3),
	ADDIU   (InstructionFmt.I, OperandFmt.RRI, 0x09, -2, -1, -3),
	ADDU    (InstructionFmt.R, OperandFmt.R3 , 0x00, -2, -3, -1, 0, 0x21),
	AND     (InstructionFmt.R, OperandFmt.R3 , 0x00, -2, -3, -1, 0, 0x24),
	ANDI    (InstructionFmt.I, OperandFmt.RRI, 0x0c, -2, -1, -3),
	BREAK   (InstructionFmt.F20_6, OperandFmt.I, 0x00, -1, 0x0d),
	CACHE   (InstructionFmt.I, OperandFmt.IIR, 0x2f, -3, -1, -2),
	CLO     (InstructionFmt.R, OperandFmt.RR,  0x1c, -2, -1, -1, 0, 0x21),
	CLZ     (InstructionFmt.R, OperandFmt.RR,  0x1c, -2, -1, -1, 0, 0x20),
	DIV     (InstructionFmt.R, OperandFmt.RR,  0x00, -1, -2, 0, 0, 0x1a),
	DIVU    (InstructionFmt.R, OperandFmt.RR,  0x00, -1, -2, 0, 0, 0x1b),
	LB      (InstructionFmt.I, OperandFmt.RIR, 0x20, -3, -1, -2),
	LBU     (InstructionFmt.I, OperandFmt.RIR, 0x24, -3, -1, -2),
	LH      (InstructionFmt.I, OperandFmt.RIR, 0x21, -3, -1, -2),
	LHU     (InstructionFmt.I, OperandFmt.RIR, 0x25, -3, -1, -2),
	LL      (InstructionFmt.I, OperandFmt.RIR, 0x30, -3, -1, -2),
	LUI     (InstructionFmt.I, OperandFmt.RI,  0x0f, 0, -1, -2),
	LW      (InstructionFmt.I, OperandFmt.RIR, 0x23, -3, -1, -2),
	LWL     (InstructionFmt.I, OperandFmt.RIR, 0x22, -3, -1, -2),
	LWR     (InstructionFmt.I, OperandFmt.RIR, 0x26, -3, -1, -2),
	MADD    (InstructionFmt.R, OperandFmt.RR,  0x1c, -1, -2, 0, 0, 0),
	MADDU   (InstructionFmt.R, OperandFmt.RR,  0x1c, -1, -2, 0, 0, 0x01),
	MFHI    (InstructionFmt.R, OperandFmt.R,   0x00, 0, 0, -1, 0, 0x10),
	MFLO    (InstructionFmt.R, OperandFmt.R,   0x00, 0, 0, -1, 0, 0x12),
	MOVN    (InstructionFmt.R, OperandFmt.R3,  0x00, -2, -3, -1, 0, 0x0b),
	MOVZ    (InstructionFmt.R, OperandFmt.R3,  0x00, -2, -3, -1, 0, 0x0a),
	MSUB    (InstructionFmt.R, OperandFmt.RR,  0x1c, -1, -2, 0, 0, 0x04),
	MSUBU   (InstructionFmt.R, OperandFmt.RR,  0x1c, -1, -2, 0, 0, 0x05),
	MTHI    (InstructionFmt.R, OperandFmt.R,   0x00, -1, 0, 0, 0, 0x11),
	MTLO    (InstructionFmt.R, OperandFmt.R,   0x00, -1, 0, 0, 0, 0x13),
	MUL     (InstructionFmt.R, OperandFmt.R3,  0x1c, -2, -3, -1, 0, 0x02),
	MULT    (InstructionFmt.R, OperandFmt.RR,  0x00, -1, -2, 0, 0, 0x18),
	MULTU   (InstructionFmt.R, OperandFmt.RR,  0x00, -1, -2, 0, 0, 0x19),
	NOP     (InstructionFmt.R, OperandFmt.NONE,0, 0, 0, 0, 0, 0),
	NOR     (InstructionFmt.R, OperandFmt.R3 , 0x00, -2, -3, -1, 0, 0x27),
	OR      (InstructionFmt.R, OperandFmt.R3 , 0x00, -2, -3, -1, 0, 0x25),
	ORI     (InstructionFmt.I, OperandFmt.RRI, 0x0d, -2, -1, -3),
	PREF    (InstructionFmt.I, OperandFmt.IIR, 0x33, -3, -1, -2),
	SB      (InstructionFmt.I, OperandFmt.RIR, 0x28, -3, -1, -2),
	SC      (InstructionFmt.I, OperandFmt.RIR, 0x38, -3, -1, -2),
	SDBBP   (InstructionFmt.F20_6, OperandFmt.I, 0x1c, -1, 0x3f),
	SH      (InstructionFmt.I, OperandFmt.RIR, 0x29, -3, -1, -2),
	SLL     (InstructionFmt.R, OperandFmt.RRI, 0x00, 0, -2, -1, -3, 0x00),
	SLLV    (InstructionFmt.R, OperandFmt.R3 , 0x00, -3, -2, -1, 0, 0x04),
	SLT     (InstructionFmt.R, OperandFmt.R3 , 0x00, -2, -3, -1, 0, 0x2a),
	SLTI    (InstructionFmt.I, OperandFmt.RRI, 0x0a, -2, -1, -3),
	SLTIU   (InstructionFmt.I, OperandFmt.RRI, 0x0b, -2, -1, -3),
	SLTU    (InstructionFmt.R, OperandFmt.R3 , 0x00, -2, -3, -1, 0, 0x2b),
	SRA     (InstructionFmt.R, OperandFmt.RRI, 0x00, 0, -2, -1, -3, 0x03),
	SRAV    (InstructionFmt.R, OperandFmt.R3 , 0x00, -3, -2, -1, 0, 0x07),
	SRL     (InstructionFmt.R, OperandFmt.RRI, 0x00, 0, -2, -1, -3, 0x02),
	SRLV    (InstructionFmt.R, OperandFmt.R3 , 0x00, -3, -2, -1, 0, 0x06),
	SSNOP   (InstructionFmt.R, OperandFmt.NONE,0, 0, 0, 0, 1, 0),
	SUB     (InstructionFmt.R, OperandFmt.R3 , 0x00, -2, -3, -1, 0, 0x22),
	SUBU    (InstructionFmt.R, OperandFmt.R3 , 0x00, -2, -3, -1, 0, 0x23),
	SW      (InstructionFmt.I, OperandFmt.RIR, 0x2b, -3, -1, -2),
	SWL     (InstructionFmt.I, OperandFmt.RIR, 0x2a, -3, -1, -2),
	SWR     (InstructionFmt.I, OperandFmt.RIR, 0x2e, -3, -1, -2),
	SYNC    (InstructionFmt.R, OperandFmt.I,   0x00, 0, 0, 0, -1, 0x0f),
	SYSCALL (InstructionFmt.F20_6, OperandFmt.I, 0x00, -1, 0x0c),
	TEQ     (InstructionFmt.R, OperandFmt.RR , 0x00, -1, -2, 0, 0, 0x34),
	TEQI    (InstructionFmt.I, OperandFmt.RI , 0x01, -1, 0x0c, -2),
	TGE     (InstructionFmt.R, OperandFmt.RR , 0x00, -1, -2, 0, 0, 0x30),
	TGEI    (InstructionFmt.I, OperandFmt.RI , 0x01, -1, 0x08, -2),
	TGEU    (InstructionFmt.R, OperandFmt.RR , 0x00, -1, -2, 0, 0, 0x31),
	TGEIU   (InstructionFmt.I, OperandFmt.RI , 0x01, -1, 0x09, -2),
	TLT     (InstructionFmt.R, OperandFmt.RR , 0x00, -1, -2, 0, 0, 0x32),
	TLTI    (InstructionFmt.I, OperandFmt.RI , 0x01, -1, 0x0a, -2),
	TLTU    (InstructionFmt.R, OperandFmt.RR , 0x00, -1, -2, 0, 0, 0x33),
	TLTIU   (InstructionFmt.I, OperandFmt.RI , 0x01, -1, 0x0b, -2),
	TNE     (InstructionFmt.R, OperandFmt.RR , 0x00, -1, -2, 0, 0, 0x36),
	TNEI    (InstructionFmt.I, OperandFmt.RI , 0x01, -1, 0x0e, -2),
	XOR     (InstructionFmt.R, OperandFmt.R3 , 0x00, -2, -3, -1, 0, 0x26),
	XORI    (InstructionFmt.I, OperandFmt.RRI, 0x0e, -2, -1, -3),
	
	MOVE    (InstructionFmt.R, OperandFmt.RR , 0x00, -2, 0, -1, 0, 0x25),//implement with OR, as GNU assembler does.
	NOT     (InstructionFmt.R, OperandFmt.RR , 0x00, -2, 0, -1, 0, 0x27),//implement with NOR
	ERET	(InstructionFmt.J, OperandFmt.NONE, 0x10, 0x2000018),
	;
	private final InstructionFmt instrFormat;
	private final OperandFmt operandFormat;
	private final int opCode;
	private final int[] fields;
	
	/**
	 * @param instrFormat The machine instruction format.
	 * @param operandFormat The assembly instruction operand format.
	 * @param opCode The operation code of the instruction.
	 * @param fields The content of fields. Positive values and 0 specifies a field whose value is hard-coded,
	 * and negative values specify the index of operand list, starting from -1. 
	 */
	private StandardInstrParser(InstructionFmt instrFormat, OperandFmt operandFormat, int opCode, int... fields)
	{
		this.instrFormat = instrFormat;
		this.operandFormat = operandFormat;
		this.opCode = opCode;
		this.fields = fields;
	}
	
	@Override
	public void parse(Operand[] operands, Assembler assembler, ArrayList<Instruction> instrList) throws AsmError
	{
		this.operandFormat.matches(operands);
		Operand[] ops = new Operand[this.fields.length];
		for(int i = 0; i < this.fields.length; i++)
		{
			if(this.fields[i] < 0)
				ops[i] = operands[-this.fields[i] - 1];
			else
				ops[i] = new OpConstant(this.fields[i]);
		}

		instrList.add(this.instrFormat.newInstance(this.opCode, ops, null, assembler));
	}

	
	/* 
	 * Register-Immediate: 5, 5, 5, 11
	 * Condition Code, Immediate: 5, 3, 1, 1, 16
	 * Formatted Compare: 5, 5, 5, 3, 2, 6
	 * Register Move: 5, 3, 1, 1, 5, 5, 6
	 * 
	 * Unimplemented instructions:
	 * ABS.fmt
	 * ADD.fmt
	 * C.cond.fmt
	 * CEIL.W.fmt
	 * CVT.D.fmt
	 * CVT.S.fmt
	 * CVT.W.fmt
	 * DIV.fmt
	 * FLOOR.W.fmt
	 * MUL.fmt
	 * NEG.fmt
	 * ROUND.W.fmt
	 * SQRT.fmt
	 * SUB.fmt
	 * TRUCT.W.fmt
	 * MOV.fmt
	 * MOVF
	 * MOVF.fmt
	 * MOVN
	 * MOVN.fmt
	 * MOVT
	 * MOVT.fmt
	 * MOVZ
	 * MOVZ.fmt
	 * BC1F, BC1FL, BC1T, BC1TL, BC2F, BC2FL, BC2T, BC2TL,
	 * CFC1, CFC2, CTC1, CTC2, COP2, DERET, ERET, LDC1, LDC2, LWC1, LWC2,
	 * SDC1, SDC2, SWC1, SWC2, MFC0, MFC1, MFC2, MTC0, MTC1, MTC2, WAIT
	 */
}
