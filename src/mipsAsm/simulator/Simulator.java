package mipsAsm.simulator;

import java.util.function.Predicate;

import mipsAsm.simulator.instruction.Instructions;
import mipsAsm.simulator.util.Memory;
import mipsAsm.simulator.util.SimException;
import mipsAsm.simulator.util.SimExceptionCode;

public class Simulator
{
	private int programCounter;
	private int currInstr;
	private int[] scheduledPC = new int[2];
	
	public final RegisterFile gpr = new RegisterFile();
	public final CP0RegisterFile cp0 = new CP0RegisterFile();
	public final Memory mem = new Memory();
	public int regHI = 0;
	public int regLO = 0;

	private int[] programData;
	private int initPC;
	private SimExceptionCode pendingException;
	
	public static final Predicate<Simulator> pcOutofRange = sim -> {
		return sim.programCounter < sim.initPC || 
			sim.programCounter >= (sim.programData.length << 2);
	};
	
	public static final Predicate<Simulator> exceptionOccur = sim -> sim.pendingException != null;
	
	
	public void clear()
	{
		this.programCounter = 0;
		this.gpr.clear();
		this.mem.clear();
		this.regHI = this.regLO = 0;
	}
	
	public void resetSimProgress()
	{
		this.gpr.clear();
		this.mem.clear();
		this.mem.loadProgram(this.programData);
		this.programCounter = this.initPC;
		this.scheduledPC[0] = this.programCounter + 4;
		this.scheduledPC[1] = this.programCounter + 8;
		this.currInstr = this.programData[0];
		this.pendingException = null;
	}
	
	public void loadProgram(int[] programData, int initPC)
	{
		System.out.println("Load program, init PC = " + initPC);
		this.programData = programData;
		this.initPC = initPC << 2;
		this.resetSimProgress();
	}
	
	public SimExceptionCode step()
	{
		this.pendingException = null;
		try
		{
			Instructions.execute(this, this.currInstr);
		}
		catch(SimException e)
		{
			// TODO Auto-generated catch block
		}

		this.programCounter = this.scheduledPC[0];
		this.scheduledPC[0] = this.scheduledPC[1];
		this.scheduledPC[1] += 4;
		//TODO Add address translation
		this.currInstr = this.mem.readWord(this.programCounter);
		return this.pendingException;
	}
	
	public void runUntil(Predicate<Simulator> terminateCondition)
	{
		while(!terminateCondition.test(this))
			this.step();
	}
	
	public SimExceptionCode getLastException()
	{
		return this.pendingException;
	}
	
	public void clearException()
	{
		this.pendingException = null;
	}
	
	public void signalException(SimExceptionCode exceptionCode, int... params) throws SimException
	{
		//TODO
		this.pendingException = exceptionCode;
	}
	
	//Since MIPS processors introduce the concept of branch delay slot, the program counter
	//should be scheduled two instruction cycles ahead.
	
	public void scheduleRelativeJump(int offset1, int offset2)
	{
		this.scheduledPC[0] = this.programCounter + (offset1 & -4);
		this.scheduledPC[1] = this.programCounter + (offset2 & -4);
	}
	
	public void scheduleRelativeJump(int offset2)
	{
		this.scheduledPC[1] = this.programCounter + (offset2 & -4);
	}
	
	public void scheduleAbsoluteJump(int addr1, int addr2, int mask)
	{
		this.scheduledPC[0] = (this.programCounter & ~mask) | (addr1 & mask & -4);
		this.scheduledPC[1] = (this.programCounter & ~mask) | (addr2 & mask & -4);
	}
	
	public void scheduleAbsoluteJump(int addr2, int mask)
	{
		this.scheduledPC[1] = (this.programCounter & ~mask) | (addr2 & mask & -4);
	}
	
	public int getPC()
	{
		return this.programCounter;
	}
	
	public int getCurrInstruction()
	{
		return this.currInstr;
	}
	
	public void setHILO(int HI, int LO)
	{
		this.regHI = HI;
		this.regLO = LO;
	}
	
}
