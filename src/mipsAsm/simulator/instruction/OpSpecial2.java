package mipsAsm.simulator.instruction;

import java.util.function.BiConsumer;

import mipsAsm.simulator.Simulator;
import mipsAsm.simulator.util.SimulatorException;

public final class OpSpecial2
{
	private OpSpecial2() {}
	
	@SuppressWarnings("unchecked")
	private static final BiConsumer<Simulator, int[]>[] executors = new BiConsumer[64];

	public static final BiConsumer<Simulator, int[]> executor = (sim, p) -> executors[p[4] & 63].accept(sim, p);

	static
	{
		BiConsumer<Simulator, int[]> reserved = (sim, p) -> sim.signalException(SimulatorException.ReservedInstruction);
		for(int i = 0; i < 64; i++)
			executors[i] = reserved;
		
		executors[0] = (sim, p) -> {//madd
			long result = ((long)sim.regHI << 32) | ((long)sim.regLO & 0xffffffffL);
			result += (long)sim.reg.get(p[0]) * (long)sim.reg.get(p[1]);
			sim.setHILO((int)(result >> 32), (int)(result & 0xffffff));
		};
		
		executors[1] = (sim, p) -> {//maddu
			long result = ((long)sim.regHI << 32) | ((long)sim.regLO & 0xffffffffL);
			result += ((long)sim.reg.get(p[0]) & 0xffffffffL) * ((long)sim.reg.get(p[1]) & 0xffffffffL);
			sim.setHILO((int)(result >> 32), (int)(result & 0xffffff));
		};
		
		executors[2] = (sim, p) -> //mul
			sim.reg.set(p[2], sim.reg.get(p[0]) * sim.reg.get(p[1]));
		
		executors[4] = (sim, p) -> {//msub
			long result = ((long)sim.regHI << 32) | ((long)sim.regLO & 0xffffffffL);
			result -= (long)sim.reg.get(p[0]) * (long)sim.reg.get(p[1]);
			sim.setHILO((int)(result >> 32), (int)(result & 0xffffff));
		};
			
		executors[5] = (sim, p) -> {//msubu
			long result = ((long)sim.regHI << 32) | ((long)sim.regLO & 0xffffffffL);
			result -= ((long)sim.reg.get(p[0]) & 0xffffffffL) * ((long)sim.reg.get(p[1]) & 0xffffffffL);
			sim.setHILO((int)(result >> 32), (int)(result & 0xffffff));
		};
		
		executors[32] = (sim, p) -> //clz
			sim.reg.set(p[2], Integer.numberOfLeadingZeros(sim.reg.get(p[0])));
		
		executors[33] = (sim, p) -> //clo
			sim.reg.set(p[2], Integer.numberOfLeadingZeros(~sim.reg.get(p[0])));
		
		executors[63] = (sim, p) -> sim.signalException(SimulatorException.DebugBreakpoint);
	}
}
