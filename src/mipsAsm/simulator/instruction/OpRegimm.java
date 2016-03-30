package mipsAsm.simulator.instruction;

import java.util.function.BiConsumer;

import mipsAsm.simulator.Simulator;
import mipsAsm.simulator.util.SimulatorException;

public final class OpRegimm
{
	private OpRegimm() {}
	
	//TODO change field to private before release
	@SuppressWarnings("unchecked")
	public static final BiConsumer<Simulator, int[]>[] executors = new BiConsumer[32];

	public static final BiConsumer<Simulator, int[]> executor = (sim, p) -> executors[p[1] & 31].accept(sim, p);

	static
	{
		BiConsumer<Simulator, int[]> reserved = (sim, p) -> sim.signalException(SimulatorException.ReservedInstruction);
		for(int i = 0; i < 32; i++)
			executors[i] = reserved;
		
		executors[0] = (sim, p) -> {//bltz
			if(sim.reg.get(p[0]) < 0) sim.scheduleRelativeJump((p[2] + 1) << 2);};
		
		executors[1] = (sim, p) -> {//bgez
			if(sim.reg.get(p[0]) >= 0) sim.scheduleRelativeJump((p[2] + 1) << 2);};
		
		executors[2] = (sim, p) -> {//bltzl
			if(sim.reg.get(p[0]) < 0)
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);};
		
		executors[3] = (sim, p) -> {//bgezl
			if(sim.reg.get(p[0]) >= 0)
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);};
		
		executors[8] = (sim, p) -> {//tgei
			if(sim.reg.get(p[0]) >= p[2]) sim.signalException(SimulatorException.Trap);};
		
		executors[9] = (sim, p) -> {//tgeiu
			if(((long)sim.reg.get(p[0]) & 0xffffffffL) >= ((long)p[2] & 0xffffffffL)) sim.signalException(SimulatorException.Trap);};
		
		executors[10] = (sim, p) -> {//tlti
			if(sim.reg.get(p[0]) < p[2]) sim.signalException(SimulatorException.Trap);};
		
		executors[11] = (sim, p) -> {//tltiu
			if(((long)sim.reg.get(p[0]) & 0xffffffffL) < ((long)p[2] & 0xffffffffL)) sim.signalException(SimulatorException.Trap);};
		
		executors[12] = (sim, p) -> {//teqi
			if(sim.reg.get(p[0]) == p[2]) sim.signalException(SimulatorException.Trap);};

		executors[14] = (sim, p) -> {//tnei
			if(sim.reg.get(p[0]) != p[2]) sim.signalException(SimulatorException.Trap);};

		executors[16] = (sim, p) -> {//bltzal
			if(sim.reg.get(p[0]) < 0)
			{
				sim.scheduleRelativeJump((p[2] + 1) << 2);
				sim.reg.set(31, sim.getPC() + 8);
			}
		};
		
		executors[17] = (sim, p) -> {//bgezal
			if(sim.reg.get(p[0]) >= 0)
			{
				sim.scheduleRelativeJump((p[2] + 1) << 2);
				sim.reg.set(31, sim.getPC() + 8);
			}
		};
		
		executors[18] = (sim, p) -> {//bltzall
			if(sim.reg.get(p[0]) < 0)
			{
				sim.scheduleRelativeJump((p[2] + 1) << 2);
				sim.reg.set(31, sim.getPC() + 8);
			}
			else
				sim.scheduleRelativeJump(8, 12);
		};
		
		executors[19] = (sim, p) -> {//bgezall
			if(sim.reg.get(p[0]) >= 0)
			{
				sim.scheduleRelativeJump((p[2] + 1) << 2);
				sim.reg.set(31, sim.getPC() + 8);
			}
			else
				sim.scheduleRelativeJump(8, 12);
		};
	}
}
