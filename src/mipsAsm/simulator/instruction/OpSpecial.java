package mipsAsm.simulator.instruction;

import java.util.function.BiConsumer;

import mipsAsm.simulator.Simulator;
import mipsAsm.simulator.util.SimulatorException;

public final class OpSpecial
{
	private OpSpecial() {}
	
	@SuppressWarnings("unchecked")
	private static final BiConsumer<Simulator, int[]>[] executors = new BiConsumer[64];

	public static final BiConsumer<Simulator, int[]> executor = (sim, p) -> executors[p[4] & 63].accept(sim, p);

	static
	{
		BiConsumer<Simulator, int[]> reserved = (sim, p) -> sim.signalException(SimulatorException.ReservedInstruction);
		
		executors[0] = (sim, p) -> //sll
			sim.reg.set(p[2], sim.reg.get(p[1]) << p[3]);
		
		executors[1] = (sim, p) -> //movci, unimplemented
			sim.signalException(SimulatorException.UnimplementedInstruction);
		
		executors[2] = (sim, p) -> //srl
			sim.reg.set(p[2], (int)(((long)sim.reg.get(p[1]) & 0xffffffffL) >> p[3]));
			
		executors[3] = (sim, p) -> //sra
			sim.reg.set(p[2], sim.reg.get(p[1]) >> p[3]);
		
		executors[4] = (sim, p) -> //sllv
			sim.reg.set(p[2], sim.reg.get(p[1]) << (sim.reg.get(p[0]) & 31));
		
		executors[5] = reserved;
		
		executors[6] = (sim, p) -> //srlv
			sim.reg.set(p[2], (int)(((long)sim.reg.get(p[1]) & 0xffffffffL) >> (sim.reg.get(p[0]) & 31)));
		
		executors[7] = (sim, p) -> //srav
			sim.reg.set(p[2], sim.reg.get(p[1]) >> (sim.reg.get(p[0]) & 31));
		
		executors[8] = (sim, p) -> //jr
			sim.scheduleAbsoluteJump(sim.reg.get(p[0]), 0xffffffff);
		
		executors[9] = (sim, p) -> {//jalr
			sim.reg.set(p[2], sim.getPC() + 8);
			sim.scheduleAbsoluteJump(sim.reg.get(p[0]), 0xffffffff);
		};
		
		executors[10] = (sim, p) -> {//movz
			if(sim.reg.get(p[1]) == 0) sim.reg.set(p[2], sim.reg.get(p[0]));};
		
		executors[11] = (sim, p) -> {//movn
			if(sim.reg.get(p[1]) != 0) sim.reg.set(p[2], sim.reg.get(p[0]));};
		
		executors[12] = (sim, p) -> //syscall
			sim.signalException(SimulatorException.SystemCall);
		
		executors[13] = (sim, p) -> //break
			sim.signalException(SimulatorException.Breakpoint);
		
		executors[14] = reserved;
		
		executors[15] = (sim, p) -> {};//sync; since the simulator is single-core, this performs virtually no operation.
		
		executors[16] = (sim, p) -> //mfhi
			sim.reg.set(p[2], sim.regHI);
		
		executors[17] = (sim, p) -> //mthi
			sim.regHI = sim.reg.get(p[0]);
		
		executors[18] = (sim, p) -> //mflo
			sim.reg.set(p[2], sim.regLO);
		
		executors[19] = (sim, p) -> //mtlo
			sim.regLO = sim.reg.get(p[0]);
		
		executors[20] = reserved;
		executors[21] = reserved;
		executors[22] = reserved;
		executors[23] = reserved;
		
		executors[24] = (sim, p) -> {//mult
			long result = (long)sim.reg.get(p[0]) * (long)sim.reg.get(p[1]);
			sim.setHILO((int)(result >> 32), (int)result);
		};
		
		executors[25] = (sim, p) -> {//multu
			long result = ((long)sim.reg.get(p[0]) & 0xffffffffL) * ((long)sim.reg.get(p[1]) & 0xffffffffL);
			sim.setHILO((int)(result >> 32), (int)result);
		};
		
		executors[26] = (sim, p) -> { //div
			int rs = sim.reg.get(p[0]), rt = sim.reg.get(p[1]);
			if(rt == 0)
				sim.setHILO(rs, 0);
			else
				sim.setHILO(rs % rt, rs / rt);
		};
		
		executors[27] = (sim, p) -> { //divu
			long rs = sim.reg.get(p[0]) & 0xffffffffL, rt = sim.reg.get(p[1]) & 0xffffffffL;
			if(rt == 0)
				sim.setHILO((int)rs, 0);
			else
				sim.setHILO((int)(rs % rt), (int)(rs / rt));
		};
		
		executors[28] = reserved;
		executors[29] = reserved;
		executors[30] = reserved;
		executors[31] = reserved;
		
		executors[32] = (sim, p) -> { //add
			int rs = sim.reg.get(p[0]), rt = sim.reg.get(p[1]), rd = rs + rt;
			if((rs > 0 && rt > 0 && rd < 0) || (rs < 0 && rt < 0 && rd > 0))
				sim.signalException(SimulatorException.IntegerOverflow);
			else
				sim.reg.set(p[2], rd);
		};
		
		executors[33] = (sim, p) -> //addu
			sim.reg.set(p[2], sim.reg.get(p[0]) + sim.reg.get(p[1]));
		
		executors[34] = (sim, p) -> { //sub
			int rs = sim.reg.get(p[0]), rt = sim.reg.get(p[1]), rd = rs - rt;
			if((rs > 0 && rt < 0 && rd < 0) || (rs < 0 && rt > 0 && rd > 0))
				sim.signalException(SimulatorException.IntegerOverflow);
			else
				sim.reg.set(p[2], rd);
		};
		
		executors[35] = (sim, p) -> //subu
			sim.reg.set(p[2], sim.reg.get(p[0]) - sim.reg.get(p[1]));
		
		executors[36] = (sim, p) -> //and
			sim.reg.set(p[2], sim.reg.get(p[0]) & sim.reg.get(p[1]));

		executors[37] = (sim, p) -> //or
			sim.reg.set(p[2], sim.reg.get(p[0]) | sim.reg.get(p[1]));

		executors[38] = (sim, p) -> //xor
			sim.reg.set(p[2], sim.reg.get(p[0]) ^ sim.reg.get(p[1]));

		executors[39] = (sim, p) -> //nor
			sim.reg.set(p[2], ~(sim.reg.get(p[0]) | sim.reg.get(p[1])));
		
		executors[40] = reserved;
		executors[41] = reserved;
		
		executors[42] = (sim, p) -> //slt
			sim.reg.set(p[2], sim.reg.get(p[0]) < sim.reg.get(p[1])? 1: 0);

		executors[43] = (sim, p) -> //sltu
			sim.reg.set(p[2], ((long)sim.reg.get(p[0]) & 0xffffffffL) < ((long)sim.reg.get(p[1]) & 0xffffffffL)? 1: 0);
		
		executors[44] = reserved;
		executors[45] = reserved;
		executors[46] = reserved;
		executors[47] = reserved;
		
		executors[48] = (sim, p) -> { //tge
			if(sim.reg.get(p[0]) >= sim.reg.get(p[1])) sim.signalException(SimulatorException.Trap);};
		
		executors[49] = (sim, p) -> { //tgeu
			if(((long)sim.reg.get(p[0]) & 0xffffffffL) >= ((long)sim.reg.get(p[1]) & 0xffffffffL))
				sim.signalException(SimulatorException.Trap);};

		executors[50] = (sim, p) -> { //tlt
			if(sim.reg.get(p[0]) < sim.reg.get(p[1])) sim.signalException(SimulatorException.Trap);};

		executors[51] = (sim, p) -> { //tltu
			if(((long)sim.reg.get(p[0]) & 0xffffffffL) < ((long)sim.reg.get(p[1]) & 0xffffffffL))
				sim.signalException(SimulatorException.Trap);};

		executors[52] = (sim, p) -> { //teq
			if(sim.reg.get(p[0]) == sim.reg.get(p[1])) sim.signalException(SimulatorException.Trap);};
		executors[53] = reserved;
		executors[54] = (sim, p) -> { //tne
			if(sim.reg.get(p[0]) != sim.reg.get(p[1])) sim.signalException(SimulatorException.Trap);};
		executors[55] = reserved;
		
		for(int i = 56; i < 64; i++)
			executors[i] = reserved;
	}

}
