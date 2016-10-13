package mipsAsm.simulator.instruction;

import static mipsAsm.simulator.util.SimExceptionCode.*;

public final class OpRegimm
{
	private OpRegimm() {}
	
	private static final Instruction[] executors = new Instruction[32];

	public static final Instruction executor = (sim, p) -> executors[p[1] & 31].execute(sim, p);

	static
	{
		for(int i = 0; i < 32; i++)
			executors[i] = Instruction.RESERVED;
		
		executors[0] = (sim, p) -> {//bltz
			if(sim.gpr.get(p[0]) < 0) sim.scheduleRelativeJump((p[2] + 1) << 2);};
		
		executors[1] = (sim, p) -> {//bgez
			if(sim.gpr.get(p[0]) >= 0) sim.scheduleRelativeJump((p[2] + 1) << 2);};
		
		executors[2] = (sim, p) -> {//bltzl
			if(sim.gpr.get(p[0]) < 0)
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);};
		
		executors[3] = (sim, p) -> {//bgezl
			if(sim.gpr.get(p[0]) >= 0)
				sim.scheduleRelativeJump((p[2] + 1) << 2);
			else
				sim.scheduleRelativeJump(8, 12);};
		
		executors[8] = (sim, p) -> {//tgei
			if(sim.gpr.get(p[0]) >= p[2]) sim.signalException(Trap);};
		
		executors[9] = (sim, p) -> {//tgeiu
			if(((long)sim.gpr.get(p[0]) & 0xffffffffL) >= ((long)p[2] & 0xffffffffL)) sim.signalException(Trap);};
		
		executors[10] = (sim, p) -> {//tlti
			if(sim.gpr.get(p[0]) < p[2]) sim.signalException(Trap);};
		
		executors[11] = (sim, p) -> {//tltiu
			if(((long)sim.gpr.get(p[0]) & 0xffffffffL) < ((long)p[2] & 0xffffffffL)) sim.signalException(Trap);};
		
		executors[12] = (sim, p) -> {//teqi
			if(sim.gpr.get(p[0]) == p[2]) sim.signalException(Trap);};

		executors[14] = (sim, p) -> {//tnei
			if(sim.gpr.get(p[0]) != p[2]) sim.signalException(Trap);};

		executors[16] = (sim, p) -> {//bltzal
			if(sim.gpr.get(p[0]) < 0)
			{
				sim.scheduleRelativeJump((p[2] + 1) << 2);
				sim.gpr.set(31, sim.getPC() + 8);
			}
		};
		
		executors[17] = (sim, p) -> {//bgezal
			if(sim.gpr.get(p[0]) >= 0)
			{
				sim.scheduleRelativeJump((p[2] + 1) << 2);
				sim.gpr.set(31, sim.getPC() + 8);
			}
		};
		
		executors[18] = (sim, p) -> {//bltzall
			if(sim.gpr.get(p[0]) < 0)
			{
				sim.scheduleRelativeJump((p[2] + 1) << 2);
				sim.gpr.set(31, sim.getPC() + 8);
			}
			else
				sim.scheduleRelativeJump(8, 12);
		};
		
		executors[19] = (sim, p) -> {//bgezall
			if(sim.gpr.get(p[0]) >= 0)
			{
				sim.scheduleRelativeJump((p[2] + 1) << 2);
				sim.gpr.set(31, sim.getPC() + 8);
			}
			else
				sim.scheduleRelativeJump(8, 12);
		};
	}
}
