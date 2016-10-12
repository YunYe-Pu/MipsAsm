package mipsAsm.simulator.instruction;

import mipsAsm.assembler.util.InstructionFmt;
import mipsAsm.simulator.Simulator;
import mipsAsm.simulator.util.SimException;
import mipsAsm.simulator.util.SimExceptionCode;

public class Instructions
{

	private static final Instruction[] instructionMap = new Instruction[64];
	private static final InstructionFmt[] formatMap = new InstructionFmt[64];
	
	public static void execute(Simulator simulator, int instruction) throws SimException
	{
		InstructionFmt format = formatMap[(instruction >> 26) & 63];
		if(format == null)
			instructionMap[(instruction >> 26) & 63].execute(simulator, null);
		else
		{
			int[] param = format.splitBinary(instruction);
			instructionMap[(instruction >> 26) & 63].execute(simulator, param);
		}
	}
	
	static
	{
		put(0, InstructionFmt.R, OpSpecial.executor);//special
		put(1, InstructionFmt.R, OpRegimm.executor);//regimm

		put(2, InstructionFmt.J, (sim, p) -> //j
			sim.scheduleAbsoluteJump(p[0] << 2, 0x0fffffff));

		put(3, InstructionFmt.J, (sim, p) -> {//jal
			sim.scheduleAbsoluteJump(p[0] << 2, 0x0fffffff);
			sim.gpr.set(31, sim.getPC() + 8);
		});

		put(4, InstructionFmt.I, (sim, p) -> {//beq
			if(sim.gpr.get(p[0]) == sim.gpr.get(p[1])) sim.scheduleRelativeJump((p[2] + 1) << 2);
		});

		put(5, InstructionFmt.I, (sim, p) -> {//bne
			if(sim.gpr.get(p[0]) != sim.gpr.get(p[1])) sim.scheduleRelativeJump((p[2] + 1) << 2);
		});

		put(6, InstructionFmt.I, (sim, p) -> {//blez
			if(sim.gpr.get(p[0]) <= 0) sim.scheduleRelativeJump((p[2] + 1) << 2);
		});

		put(7, InstructionFmt.I, (sim, p) -> {//bgtz
			if(sim.gpr.get(p[0]) > 0) sim.scheduleRelativeJump((p[2] + 1) << 2);
		});

		put(8, InstructionFmt.I, (sim, p) -> {//addi
			int rs = sim.gpr.get(p[0]), rt = rs + p[2];
			if((rs > 0 && p[2] > 0 && rt < 0) || (rs < 0 && p[2] < 0 && rt > 0))
				sim.signalException(SimExceptionCode.IntegerOverflow);
			else
				sim.gpr.set(p[1], rt);
		});

		put(9, InstructionFmt.I, (sim, p) -> //addiu
			sim.gpr.set(p[1], sim.gpr.get(p[0]) + p[2]));

		put(10, InstructionFmt.I, (sim, p) -> //slti
			sim.gpr.set(p[1], sim.gpr.get(p[0]) < p[2]? 1: 0));

		put(11, InstructionFmt.I, (sim, p) -> //sltiu
			sim.gpr.set(p[1], ((long)sim.gpr.get(p[0]) & 0xffffffffL) < ((long)p[2] & 0xffffffffL)? 1: 0));

		put(12, InstructionFmt.I, (sim, p) -> //andi
			sim.gpr.set(p[1], sim.gpr.get(p[0]) & (p[2] & 0x0000ffff)));
		
		put(13, InstructionFmt.I, (sim, p) -> //ori
			sim.gpr.set(p[1], sim.gpr.get(p[0]) | (p[2] & 0x0000ffff)));
		
		put(14, InstructionFmt.I, (sim, p) -> //xori
			sim.gpr.set(p[1], sim.gpr.get(p[0]) ^ (p[2] & 0x0000ffff)));
		
		put(15, InstructionFmt.I, (sim, p) -> //lui
			sim.gpr.set(p[1], p[2] << 16));

		put(16, null, Instruction.RESERVED);
		put(17, null, Instruction.RESERVED);
		put(18, null, Instruction.RESERVED);
		put(19, null, Instruction.RESERVED);
		
		put(20, InstructionFmt.I, (sim, p) -> { //beql
			if(sim.gpr.get(p[0]) == sim.gpr.get(p[1]))
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);
		});

		put(21, InstructionFmt.I, (sim, p) -> { //bnel
			if(sim.gpr.get(p[0]) != sim.gpr.get(p[1]))
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);
		});
		
		put(22, InstructionFmt.I, (sim, p) -> { //blezl
			if(sim.gpr.get(p[0]) <= 0)
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);
		});

		put(23, InstructionFmt.I, (sim, p) -> { //bgtzl
			if(sim.gpr.get(p[0]) > 0)
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);
		});
		
		put(24, null, Instruction.RESERVED);
		put(25, null, Instruction.RESERVED);
		put(26, null, Instruction.RESERVED);
		put(27, null, Instruction.RESERVED);
		put(28, InstructionFmt.R, OpSpecial2.executor);
		put(29, null, Instruction.RESERVED);
		put(30, null, Instruction.RESERVED);
		put(31, null, Instruction.RESERVED);
		
		//TODO: Add address translation for load/store
		put(32, InstructionFmt.I, (sim, p) -> //lb
			sim.gpr.set(p[1], sim.mem.readByte(sim.gpr.get(p[0]) + p[2])));
		put(33, InstructionFmt.I, (sim, p) -> //lh
			sim.gpr.set(p[1], sim.mem.readHalfWord(sim.gpr.get(p[0]) + p[2])));
		put(34, InstructionFmt.I, (sim, p) -> //lwl
			sim.gpr.set(p[1], sim.mem.readLeft(sim.gpr.get(p[0]) + p[2], sim.gpr.get(p[1]))));
		put(35, InstructionFmt.I, (sim, p) -> //lw
			sim.gpr.set(p[1], sim.mem.readWord(sim.gpr.get(p[0]) + p[2])));
		put(36, InstructionFmt.I, (sim, p) -> //lbu
			sim.gpr.set(p[1], sim.mem.readByteUnsigned(sim.gpr.get(p[0]) + p[2])));
		put(37, InstructionFmt.I, (sim, p) -> //lhu
			sim.gpr.set(p[1], sim.mem.readHalfWordUnsigned(sim.gpr.get(p[0]) + p[2])));
		put(38, InstructionFmt.I, (sim, p) -> //lwr
			sim.gpr.set(p[1], sim.mem.readRight(sim.gpr.get(p[0]) + p[2], sim.gpr.get(p[1]))));
		put(39, null, Instruction.RESERVED);
		
		put(40, InstructionFmt.I, (sim, p) -> //sb
			sim.mem.writeByte(sim.gpr.get(p[0]) + p[2], sim.gpr.get(p[1])));
		put(41, InstructionFmt.I, (sim, p) -> //sh
			sim.mem.writeHalfWord(sim.gpr.get(p[0]) + p[2], sim.gpr.get(p[1])));
		put(42, InstructionFmt.I, (sim, p) -> //swl
			sim.mem.writeLeft(sim.gpr.get(p[0]) + p[2], sim.gpr.get(p[1])));
		put(43, InstructionFmt.I, (sim, p) -> //sw
			sim.mem.writeWord(sim.gpr.get(p[0]) + p[2], sim.gpr.get(p[1])));
		put(44, null, Instruction.RESERVED);
		put(45, null, Instruction.RESERVED);
		put(46, InstructionFmt.I, (sim, p) -> //swr
			sim.mem.writeRight(sim.gpr.get(p[0]) + p[2], sim.gpr.get(p[1])));
		put(47, null, Instruction.UNIMPLEMENTED);//cache, mark as unimplemented.
		
		for(int i = 48; i < 64; i++)
			put(i, null, Instruction.UNIMPLEMENTED);
		
	}
	
	private static void put(int index, InstructionFmt format, Instruction instr)
	{
		instructionMap[index] = instr;
		formatMap[index] = format;
	}
}
