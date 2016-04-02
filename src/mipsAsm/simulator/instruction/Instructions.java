package mipsAsm.simulator.instruction;

import java.util.function.BiConsumer;

import mipsAsm.assembler.util.InstrFmt;
import mipsAsm.simulator.Simulator;
import mipsAsm.simulator.util.SimulatorException;

public class Instructions
{

	private static final DisasmEntry[] instructionMap = new DisasmEntry[64];
	private static final DisasmEntry unimplemented = new DisasmEntry(null, null);
	
	public static void execute(Simulator simulator, int instruction)
	{
		DisasmEntry e = instructionMap[(instruction >> 26) & 63];
		if(e == unimplemented)
			simulator.signalException(SimulatorException.UnimplementedInstruction);
		else if(e != null)
		{
			int[] param = e.format.splitBinary(instruction);
			e.executor.accept(simulator, param);
		}
		else
			simulator.signalException(SimulatorException.ReservedInstruction);
	}
	
	private static class DisasmEntry
	{
		public final InstrFmt format;
		public final BiConsumer<Simulator, int[]> executor;
		
		public DisasmEntry(InstrFmt format, BiConsumer<Simulator, int[]> executor)
		{
			this.format = format;
			this.executor = executor;
		}
	}
	
	static
	{
		instructionMap[0] = new DisasmEntry(InstrFmt.R, OpSpecial.executor);//special
		instructionMap[1] = new DisasmEntry(InstrFmt.R, OpRegimm.executor);//regimm

		instructionMap[2] = new DisasmEntry(InstrFmt.J, (sim, p) -> //j
			sim.scheduleAbsoluteJump(p[1] << 2, 0x0fffffff));

		instructionMap[3] = new DisasmEntry(InstrFmt.J, (sim, p) -> {//jal
			sim.scheduleAbsoluteJump(p[1] << 2, 0x0fffffff);
			sim.reg.set(31, sim.getPC() + 8);
		});

		instructionMap[4] = new DisasmEntry(InstrFmt.I, (sim, p) -> {//beq
			if(sim.reg.get(p[0]) == sim.reg.get(p[1])) sim.scheduleRelativeJump((p[2] + 1) << 2);
		});

		instructionMap[5] = new DisasmEntry(InstrFmt.I, (sim, p) -> {//bne
			if(sim.reg.get(p[0]) != sim.reg.get(p[1])) sim.scheduleRelativeJump((p[2] + 1) << 2);
		});

		instructionMap[6] = new DisasmEntry(InstrFmt.I, (sim, p) -> {//blez
			if(sim.reg.get(p[0]) <= 0) sim.scheduleRelativeJump((p[2] + 1) << 2);
		});

		instructionMap[7] = new DisasmEntry(InstrFmt.I, (sim, p) -> {//bgtz
			if(sim.reg.get(p[0]) > 0) sim.scheduleRelativeJump((p[2] + 1) << 2);
		});

		instructionMap[8] = new DisasmEntry(InstrFmt.I, (sim, p) -> {//addi
			int rs = sim.reg.get(p[0]), rt = rs + p[2];
			if((rs > 0 && p[2] > 0 && rt < 0) || (rs < 0 && p[2] < 0 && rt > 0))
				sim.signalException(SimulatorException.IntegerOverflow);
			else
				sim.reg.set(p[1], rt);
		});

		instructionMap[9] = new DisasmEntry(InstrFmt.I, (sim, p) -> //addiu
			sim.reg.set(p[1], sim.reg.get(p[0]) + p[2]));

		instructionMap[10] = new DisasmEntry(InstrFmt.I, (sim, p) -> //slti
			sim.reg.set(p[1], sim.reg.get(p[0]) < p[2]? 1: 0));

		instructionMap[11] = new DisasmEntry(InstrFmt.I, (sim, p) -> //sltiu
			sim.reg.set(p[1], ((long)sim.reg.get(p[0]) & 0xffffffffL) < ((long)p[2] & 0xffffffffL)? 1: 0));

		instructionMap[12] = new DisasmEntry(InstrFmt.I, (sim, p) -> //andi
			sim.reg.set(p[1], sim.reg.get(p[0]) & (p[2] & 0x0000ffff)));
		
		instructionMap[13] = new DisasmEntry(InstrFmt.I, (sim, p) -> //ori
			sim.reg.set(p[1], sim.reg.get(p[0]) | (p[2] & 0x0000ffff)));
		
		instructionMap[14] = new DisasmEntry(InstrFmt.I, (sim, p) -> //xori
			sim.reg.set(p[1], sim.reg.get(p[0]) ^ (p[2] & 0x0000ffff)));
		
		instructionMap[15] = new DisasmEntry(InstrFmt.I, (sim, p) -> //lui
			sim.reg.set(p[1], p[2] << 16));

		instructionMap[16] = null;
		instructionMap[17] = null;
		instructionMap[18] = null;
		instructionMap[19] = null;
		
		instructionMap[20] = new DisasmEntry(InstrFmt.I, (sim, p) -> { //beql
			if(sim.reg.get(p[0]) == sim.reg.get(p[1]))
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);
		});

		instructionMap[21] = new DisasmEntry(InstrFmt.I, (sim, p) -> { //bnel
			if(sim.reg.get(p[0]) != sim.reg.get(p[1]))
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);
		});
		
		instructionMap[22] = new DisasmEntry(InstrFmt.I, (sim, p) -> { //blezl
			if(sim.reg.get(p[0]) <= 0)
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);
		});

		instructionMap[23] = new DisasmEntry(InstrFmt.I, (sim, p) -> { //bgtzl
			if(sim.reg.get(p[0]) > 0)
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);
		});
		
		instructionMap[24] = null;
		instructionMap[25] = null;
		instructionMap[26] = null;
		instructionMap[27] = null;
		instructionMap[28] = new DisasmEntry(InstrFmt.R, OpSpecial2.executor);
		instructionMap[29] = null;
		instructionMap[30] = null;
		instructionMap[31] = null;
		
		instructionMap[32] = new DisasmEntry(InstrFmt.I, (sim, p) -> //lb
			sim.reg.set(p[1], sim.mem.readByte(sim.reg.get(p[0]) + p[2])));
		instructionMap[33] = new DisasmEntry(InstrFmt.I, (sim, p) -> //lh
			sim.reg.set(p[1], sim.mem.readHalfWord(sim.reg.get(p[0]) + p[2])));
		instructionMap[34] = new DisasmEntry(InstrFmt.I, (sim, p) -> //lwl
			sim.reg.set(p[1], sim.mem.readLeft(sim.reg.get(p[0]) + p[2], sim.reg.get(p[1]))));
		instructionMap[35] = new DisasmEntry(InstrFmt.I, (sim, p) -> //lw
			sim.reg.set(p[1], sim.mem.readWord(sim.reg.get(p[0]) + p[2])));
		instructionMap[36] = new DisasmEntry(InstrFmt.I, (sim, p) -> //lbu
			sim.reg.set(p[1], sim.mem.readByteUnsigned(sim.reg.get(p[0]) + p[2])));
		instructionMap[33] = new DisasmEntry(InstrFmt.I, (sim, p) -> //lhu
			sim.reg.set(p[1], sim.mem.readHalfWordUnsigned(sim.reg.get(p[0]) + p[2])));
		instructionMap[38] = new DisasmEntry(InstrFmt.I, (sim, p) -> //lwr
			sim.reg.set(p[1], sim.mem.readRight(sim.reg.get(p[0]) + p[2], sim.reg.get(p[1]))));
		instructionMap[39] = null;
		
		instructionMap[40] = new DisasmEntry(InstrFmt.I, (sim, p) -> //sb
			sim.mem.writeByte(sim.reg.get(p[0]) + p[2], sim.reg.get(p[1])));
		instructionMap[41] = new DisasmEntry(InstrFmt.I, (sim, p) -> //sh
			sim.mem.writeHalfWord(sim.reg.get(p[0]) + p[2], sim.reg.get(p[1])));
		instructionMap[42] = new DisasmEntry(InstrFmt.I, (sim, p) -> //swl
			sim.mem.writeRight(sim.reg.get(p[0]) + p[2], sim.reg.get(p[1])));
		instructionMap[43] = new DisasmEntry(InstrFmt.I, (sim, p) -> //sw
			sim.mem.writeWord(sim.reg.get(p[0]) + p[2], sim.reg.get(p[1])));
		instructionMap[44] = null;
		instructionMap[45] = null;
		instructionMap[46] = new DisasmEntry(InstrFmt.I, (sim, p) -> //swr
			sim.mem.writeRight(sim.reg.get(p[0]) + p[2], sim.reg.get(p[1])));
		instructionMap[47] = unimplemented;//cache, mark as unimplemented.
		
		for(int i = 48; i < 64; i++)
			instructionMap[i] = unimplemented;
		
	}
}
