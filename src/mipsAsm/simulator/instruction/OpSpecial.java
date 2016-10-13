package mipsAsm.simulator.instruction;

import static mipsAsm.simulator.util.SimExceptionCode.*;

public final class OpSpecial
{
	private OpSpecial() {}
	
	private static final Instruction[] executors = new Instruction[64];

	public static final Instruction executor = (sim, p) -> executors[p[4] & 63].execute(sim, p);

	static
	{
		executors[0] = (sim, p) -> //sll
			sim.gpr.set(p[2], sim.gpr.get(p[1]) << p[3]);
		
		executors[1] = Instruction.UNIMPLEMENTED; //movci, unimplemented
		
		executors[2] = (sim, p) -> //srl
			sim.gpr.set(p[2], (int)(((long)sim.gpr.get(p[1]) & 0xffffffffL) >> p[3]));
			
		executors[3] = (sim, p) -> //sra
			sim.gpr.set(p[2], sim.gpr.get(p[1]) >> p[3]);
		
		executors[4] = (sim, p) -> //sllv
			sim.gpr.set(p[2], sim.gpr.get(p[1]) << (sim.gpr.get(p[0]) & 31));
		
		executors[5] = Instruction.RESERVED;
		
		executors[6] = (sim, p) -> //srlv
			sim.gpr.set(p[2], (int)(((long)sim.gpr.get(p[1]) & 0xffffffffL) >> (sim.gpr.get(p[0]) & 31)));
		
		executors[7] = (sim, p) -> //srav
			sim.gpr.set(p[2], sim.gpr.get(p[1]) >> (sim.gpr.get(p[0]) & 31));
		
		executors[8] = (sim, p) -> //jr
			sim.scheduleAbsoluteJump(sim.gpr.get(p[0]), 0xffffffff);
		
		executors[9] = (sim, p) -> {//jalr
			sim.gpr.set(p[2], sim.getPC() + 8);
			sim.scheduleAbsoluteJump(sim.gpr.get(p[0]), 0xffffffff);
		};
		
		executors[10] = (sim, p) -> {//movz
			if(sim.gpr.get(p[1]) == 0) sim.gpr.set(p[2], sim.gpr.get(p[0]));};
		
		executors[11] = (sim, p) -> {//movn
			if(sim.gpr.get(p[1]) != 0) sim.gpr.set(p[2], sim.gpr.get(p[0]));};
		
		executors[12] = (sim, p) -> //syscall
			sim.signalException(SystemCall);
		
		executors[13] = (sim, p) -> //break
			sim.signalException(Breakpoint);
		
		executors[14] = Instruction.RESERVED;
		
		executors[15] = (sim, p) -> {};//sync; since the simulator is single-core, this performs virtually no operation.
		
		executors[16] = (sim, p) -> //mfhi
			sim.gpr.set(p[2], sim.regHI);
		
		executors[17] = (sim, p) -> //mthi
			sim.regHI = sim.gpr.get(p[0]);
		
		executors[18] = (sim, p) -> //mflo
			sim.gpr.set(p[2], sim.regLO);
		
		executors[19] = (sim, p) -> //mtlo
			sim.regLO = sim.gpr.get(p[0]);
		
		executors[20] = Instruction.RESERVED;
		executors[21] = Instruction.RESERVED;
		executors[22] = Instruction.RESERVED;
		executors[23] = Instruction.RESERVED;
		
		executors[24] = (sim, p) -> {//mult
			long result = (long)sim.gpr.get(p[0]) * (long)sim.gpr.get(p[1]);
			sim.setHILO((int)(result >> 32), (int)result);
		};
		
		executors[25] = (sim, p) -> {//multu
			long result = ((long)sim.gpr.get(p[0]) & 0xffffffffL) * ((long)sim.gpr.get(p[1]) & 0xffffffffL);
			sim.setHILO((int)(result >> 32), (int)result);
		};
		
		executors[26] = (sim, p) -> { //div
			int rs = sim.gpr.get(p[0]), rt = sim.gpr.get(p[1]);
			if(rt == 0)
				sim.setHILO(rs, 0);
			else
				sim.setHILO(rs % rt, rs / rt);
		};
		
		executors[27] = (sim, p) -> { //divu
			long rs = sim.gpr.get(p[0]) & 0xffffffffL, rt = sim.gpr.get(p[1]) & 0xffffffffL;
			if(rt == 0)
				sim.setHILO((int)rs, 0);
			else
				sim.setHILO((int)(rs % rt), (int)(rs / rt));
		};
		
		executors[28] = Instruction.RESERVED;
		executors[29] = Instruction.RESERVED;
		executors[30] = Instruction.RESERVED;
		executors[31] = Instruction.RESERVED;
		
		executors[32] = (sim, p) -> { //add
			int rs = sim.gpr.get(p[0]), rt = sim.gpr.get(p[1]), rd = rs + rt;
			if((rs > 0 && rt > 0 && rd < 0) || (rs < 0 && rt < 0 && rd > 0))
				sim.signalException(IntegerOverflow);
			else
				sim.gpr.set(p[2], rd);
		};
		
		executors[33] = (sim, p) -> //addu
			sim.gpr.set(p[2], sim.gpr.get(p[0]) + sim.gpr.get(p[1]));
		
		executors[34] = (sim, p) -> { //sub
			int rs = sim.gpr.get(p[0]), rt = sim.gpr.get(p[1]), rd = rs - rt;
			if((rs > 0 && rt < 0 && rd < 0) || (rs < 0 && rt > 0 && rd > 0))
				sim.signalException(IntegerOverflow);
			else
				sim.gpr.set(p[2], rd);
		};
		
		executors[35] = (sim, p) -> //subu
			sim.gpr.set(p[2], sim.gpr.get(p[0]) - sim.gpr.get(p[1]));
		
		executors[36] = (sim, p) -> //and
			sim.gpr.set(p[2], sim.gpr.get(p[0]) & sim.gpr.get(p[1]));

		executors[37] = (sim, p) -> //or
			sim.gpr.set(p[2], sim.gpr.get(p[0]) | sim.gpr.get(p[1]));

		executors[38] = (sim, p) -> //xor
			sim.gpr.set(p[2], sim.gpr.get(p[0]) ^ sim.gpr.get(p[1]));

		executors[39] = (sim, p) -> //nor
			sim.gpr.set(p[2], ~(sim.gpr.get(p[0]) | sim.gpr.get(p[1])));
		
		executors[40] = Instruction.RESERVED;
		executors[41] = Instruction.RESERVED;
		
		executors[42] = (sim, p) -> //slt
			sim.gpr.set(p[2], sim.gpr.get(p[0]) < sim.gpr.get(p[1])? 1: 0);

		executors[43] = (sim, p) -> //sltu
			sim.gpr.set(p[2], ((long)sim.gpr.get(p[0]) & 0xffffffffL) < ((long)sim.gpr.get(p[1]) & 0xffffffffL)? 1: 0);
		
		executors[44] = Instruction.RESERVED;
		executors[45] = Instruction.RESERVED;
		executors[46] = Instruction.RESERVED;
		executors[47] = Instruction.RESERVED;
		
		executors[48] = (sim, p) -> { //tge
			if(sim.gpr.get(p[0]) >= sim.gpr.get(p[1])) sim.signalException(Trap);};
		
		executors[49] = (sim, p) -> { //tgeu
			if(((long)sim.gpr.get(p[0]) & 0xffffffffL) >= ((long)sim.gpr.get(p[1]) & 0xffffffffL))
				sim.signalException(Trap);};

		executors[50] = (sim, p) -> { //tlt
			if(sim.gpr.get(p[0]) < sim.gpr.get(p[1])) sim.signalException(Trap);};

		executors[51] = (sim, p) -> { //tltu
			if(((long)sim.gpr.get(p[0]) & 0xffffffffL) < ((long)sim.gpr.get(p[1]) & 0xffffffffL))
				sim.signalException(Trap);};

		executors[52] = (sim, p) -> { //teq
			if(sim.gpr.get(p[0]) == sim.gpr.get(p[1])) sim.signalException(Trap);};
		executors[53] = Instruction.RESERVED;
		executors[54] = (sim, p) -> { //tne
			if(sim.gpr.get(p[0]) != sim.gpr.get(p[1])) sim.signalException(Trap);};
		executors[55] = Instruction.RESERVED;
		
		for(int i = 56; i < 64; i++)
			executors[i] = Instruction.RESERVED;
	}

}
