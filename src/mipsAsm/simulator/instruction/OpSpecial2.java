package mipsAsm.simulator.instruction;

public final class OpSpecial2
{
	private OpSpecial2() {}
	
	private static final Instruction[] executors = new Instruction[64];

	public static final Instruction executor = (sim, p) -> executors[p[4] & 63].execute(sim, p);

	static
	{
		for(int i = 0; i < 64; i++)
			executors[i] = Instruction.RESERVED;
		
		executors[0] = (sim, p) -> {//madd
			long result = ((long)sim.regHI << 32) | ((long)sim.regLO & 0xffffffffL);
			result += (long)sim.gpr.get(p[0]) * (long)sim.gpr.get(p[1]);
			sim.setHILO((int)(result >> 32), (int)(result & 0xffffff));
		};
		
		executors[1] = (sim, p) -> {//maddu
			long result = ((long)sim.regHI << 32) | ((long)sim.regLO & 0xffffffffL);
			result += ((long)sim.gpr.get(p[0]) & 0xffffffffL) * ((long)sim.gpr.get(p[1]) & 0xffffffffL);
			sim.setHILO((int)(result >> 32), (int)(result & 0xffffff));
		};
		
		executors[2] = (sim, p) -> //mul
			sim.gpr.set(p[2], sim.gpr.get(p[0]) * sim.gpr.get(p[1]));
		
		executors[4] = (sim, p) -> {//msub
			long result = ((long)sim.regHI << 32) | ((long)sim.regLO & 0xffffffffL);
			result -= (long)sim.gpr.get(p[0]) * (long)sim.gpr.get(p[1]);
			sim.setHILO((int)(result >> 32), (int)(result & 0xffffff));
		};
			
		executors[5] = (sim, p) -> {//msubu
			long result = ((long)sim.regHI << 32) | ((long)sim.regLO & 0xffffffffL);
			result -= ((long)sim.gpr.get(p[0]) & 0xffffffffL) * ((long)sim.gpr.get(p[1]) & 0xffffffffL);
			sim.setHILO((int)(result >> 32), (int)(result & 0xffffff));
		};
		
		executors[32] = (sim, p) -> //clz
			sim.gpr.set(p[2], Integer.numberOfLeadingZeros(sim.gpr.get(p[0])));
		
		executors[33] = (sim, p) -> //clo
			sim.gpr.set(p[2], Integer.numberOfLeadingZeros(~sim.gpr.get(p[0])));
		
//		executors[63] = (sim, p) -> sim.signalException(SimExceptionCode.DebugBreakpoint);
	}
}
