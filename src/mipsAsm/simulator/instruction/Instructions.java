package mipsAsm.simulator.instruction;

import mipsAsm.assembler.util.InstructionFmt;
import mipsAsm.simulator.Simulator;
import mipsAsm.simulator.util.SimException;

import static mipsAsm.simulator.util.SimExceptionCode.*;

public final class Instructions
{
	private Instructions() {}
	
	private static final Instruction[] instructionMap = new Instruction[64];
	private static final InstructionFmt[] formatMap = new InstructionFmt[64];
	
	private static final int[] param = new int[10];
	
	public static void execute(Simulator simulator, int instruction) throws SimException
	{
		InstructionFmt format = formatMap[(instruction >> 26) & 63];
		if(format == null)
			instructionMap[(instruction >> 26) & 63].execute(simulator, null);
		else
		{
			format.splitBinary(instruction, param);
			instructionMap[(instruction >> 26) & 63].execute(simulator, param);
		}
	}
	
	static
	{
		put(0, InstructionFmt.R, OpSpecial.executor);//special
		put(1, InstructionFmt.I, OpRegimm.executor);//regimm

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
				sim.signalException(IntegerOverflow);
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

		put(16, InstructionFmt.J, OpCp0.executor);
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
		
		put(32, InstructionFmt.I, (sim, p) -> {//lb
			int addr = sim.tlb.addressTranslation(sim.gpr.get(p[0]) + p[2], false);
			sim.gpr.set(p[1], sim.mem.readByte(addr));
		});
		
		put(33, InstructionFmt.I, (sim, p) -> {//lh
			int addr = sim.gpr.get(p[0]) + p[2];
			if((addr & 1) != 0)
				sim.signalException(AddressError_L, addr);
			addr = sim.tlb.addressTranslation(addr, false);
			sim.gpr.set(p[1], sim.mem.readHalfWord(addr));
		});
		
		put(34, InstructionFmt.I, (sim, p) -> {//lwl
			int addr = sim.tlb.addressTranslation(sim.gpr.get(p[0]) + p[2], false);
			sim.gpr.set(p[1], sim.mem.readLeft(addr, sim.gpr.get(p[1])));
		});
		
		put(35, InstructionFmt.I, (sim, p) -> {//lw
			int addr = sim.gpr.get(p[0]) + p[2];
			if((addr & 3) != 0)
				sim.signalException(AddressError_L, addr);
			addr = sim.tlb.addressTranslation(addr, false);
			sim.gpr.set(p[1], sim.mem.readWord(addr));
		});
		
		put(36, InstructionFmt.I, (sim, p) -> {//lbu
			int addr = sim.tlb.addressTranslation(sim.gpr.get(p[0]) + p[2], false);
			sim.gpr.set(p[1], sim.mem.readByteUnsigned(addr));
		});
		
		put(37, InstructionFmt.I, (sim, p) -> {//lhu
			int addr = sim.gpr.get(p[0]) + p[2];
			if((addr & 1) != 0)
				sim.signalException(AddressError_L, addr);
			addr = sim.tlb.addressTranslation(addr, false);
			sim.gpr.set(p[1], sim.mem.readHalfWordUnsigned(addr));
		});
		
		put(38, InstructionFmt.I, (sim, p) -> {//lwr
			int addr = sim.tlb.addressTranslation(sim.gpr.get(p[0]) + p[2], false);
			sim.gpr.set(p[1], sim.mem.readRight(addr, sim.gpr.get(p[1])));
		});
		
		put(39, null, Instruction.RESERVED);
		
		put(40, InstructionFmt.I, (sim, p) -> {//sb
			int addr = sim.tlb.addressTranslation(sim.gpr.get(p[0]) + p[2], true);
			sim.mem.writeByte(addr, sim.gpr.get(p[1]));
		});
		
		put(41, InstructionFmt.I, (sim, p) -> {//sh
			int addr = sim.gpr.get(p[0]) + p[2];
			if((addr & 1) != 0)
				sim.signalException(AddressError_S, addr);
			addr = sim.tlb.addressTranslation(addr, true);
			sim.mem.writeHalfWord(addr, sim.gpr.get(p[1]));
		});
		
		put(42, InstructionFmt.I, (sim, p) -> {//swl
			int addr = sim.tlb.addressTranslation(sim.gpr.get(p[0]) + p[2], true);
			sim.mem.writeLeft(addr, sim.gpr.get(p[1]));
		});
		
		put(43, InstructionFmt.I, (sim, p) -> {//sw
			int addr = sim.gpr.get(p[0]) + p[2];
			if((addr & 3) != 0)
				sim.signalException(AddressError_S, addr);
			addr = sim.tlb.addressTranslation(addr, true);
			sim.mem.writeWord(addr, sim.gpr.get(p[1]));
		});
		
		put(44, null, Instruction.RESERVED);
		put(45, null, Instruction.RESERVED);
		
		put(46, InstructionFmt.I, (sim, p) -> {//swr
			int addr = sim.tlb.addressTranslation(sim.gpr.get(p[0]) + p[2], true);
			sim.mem.writeRight(addr, sim.gpr.get(p[1]));
		});
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
