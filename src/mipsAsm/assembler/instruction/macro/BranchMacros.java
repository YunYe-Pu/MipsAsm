package mipsAsm.assembler.instruction.macro;

import java.util.ArrayList;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.exception.OpCountMismatchError;
import mipsAsm.assembler.instruction.BranchInstrParser;
import mipsAsm.assembler.instruction.Instruction;
import mipsAsm.assembler.instruction.InstructionParser;
import mipsAsm.assembler.instruction.StandardInstrParser;
import mipsAsm.assembler.operand.OpRegister;
import mipsAsm.assembler.operand.Operand;

/**
 * Convenience branch macro instructions.<br />
 * This class is dependent on {@link StandardInstrParser} and {@link BranchInstrParser} class,
 * but it does not matter in which order these class are registered to the assembler.
 * 
 * @author YunYe Pu
 */
public enum BranchMacros implements InstructionParser
{
	BLT (StandardInstrParser.SLT, BranchInstrParser.BNE),
	BGE (StandardInstrParser.SLT, BranchInstrParser.BEQ),
	BLTU(StandardInstrParser.SLTU, BranchInstrParser.BNE),
	BGEU(StandardInstrParser.SLTU, BranchInstrParser.BEQ),
	;
	
	private final InstructionParser inst1;
	private final InstructionParser inst2;
	
	private BranchMacros(InstructionParser inst1, InstructionParser inst2)
	{
		this.inst1 = inst1;
		this.inst2 = inst2;
	}
	
	@Override
	public void parse(Operand[] operands, Assembler assembler, ArrayList<Instruction> instrList) throws AsmError
	{
		if(operands.length != 3)
			throw new OpCountMismatchError(3, operands.length);
		Operand[] op1 = new Operand[3];
		op1[0] = new OpRegister(1);
		op1[1] = operands[0];
		op1[2] = operands[1];
		
		Operand[] op2 = new Operand[3];
		op2[0] = new OpRegister(1);
		op2[1] = new OpRegister(0);
		op2[2] = operands[2];
		
		this.inst1.parse(op1, assembler, instrList);
		this.inst2.parse(op2, assembler, instrList);
	}

}
