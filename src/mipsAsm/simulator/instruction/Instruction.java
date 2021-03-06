package mipsAsm.simulator.instruction;

import mipsAsm.simulator.Simulator;
import mipsAsm.simulator.util.SimException;

import static mipsAsm.simulator.util.SimExceptionCode.*;

public interface Instruction
{
	public void execute(Simulator simulator, int[] param) throws SimException;
	
	Instruction RESERVED = (sim, p) -> sim.signalException(ReservedInstruction);
	Instruction UNIMPLEMENTED = (sim, p) -> sim.signalException(UnimplementedInstruction);
}
