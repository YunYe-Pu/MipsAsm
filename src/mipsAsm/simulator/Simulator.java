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
	private int scheduledPC0, scheduledPC1;
	private boolean currInDelaySlot, nextInDelaySlot;
	
	public final RegisterFile gpr = new RegisterFile();
	public final CP0RegisterFile cp0 = new CP0RegisterFile();
	public final Memory mem = new Memory();
	public final TLB tlb = new TLB();
	public int regHI = 0;
	public int regLO = 0;

	private int[] programData;
	private int initPC;
	
	@Deprecated
	private SimExceptionCode pendingException;
	
	public static final Predicate<Simulator> pcOutofRange = sim -> {
		return sim.programCounter < sim.initPC || 
			sim.programCounter >= (sim.programData.length << 2);
	};
	
	public static final Predicate<Simulator> exceptionOccur = sim -> sim.pendingException != null;
	
	@Deprecated
	public void clear()
	{
		this.programCounter = 0;
		this.gpr.clear();
		this.mem.clear();
		this.regHI = this.regLO = 0;
	}
	
	@Deprecated
	public void resetSimProgress()
	{
		this.gpr.clear();
		this.mem.clear();
		this.mem.loadProgram(this.programData);
		this.programCounter = this.initPC;
		this.scheduledPC0 = this.programCounter + 4;
		this.scheduledPC1 = this.programCounter + 8;
		this.currInDelaySlot = this.nextInDelaySlot = false;
		this.currInstr = this.programData[0];
		this.pendingException = null;
	}
	
	public void signalColdReset()
	{
		//TODO
	}
	
	public void signalInterrupt(int intNumber)
	{
		//TODO
	}
	
	@Deprecated
	public void loadProgram(int[] programData, int initPC)
	{
		System.out.println("Load program, init PC = " + initPC);
		this.programData = programData;
		this.initPC = initPC << 2;
		this.resetSimProgress();
	}
	
	public void step()
	{
		try
		{
			this.programCounter = this.scheduledPC0;
			this.scheduledPC0 = this.scheduledPC1;
			this.scheduledPC1 += 4;
			this.currInDelaySlot = this.nextInDelaySlot;
			this.nextInDelaySlot = false;
			if((this.programCounter & 3) != 0)
				this.signalException(SimExceptionCode.AddressError_L, this.programCounter);
			int pAddr = this.tlb.addressTranslation(this, this.programCounter, false);
			this.currInstr = this.mem.readWord(pAddr);
			Instructions.execute(this, this.currInstr);
		}
		catch(SimException e)
		{
			e.excCode.process(this, e.param);
		}
	}
	
	public void runUntil(Predicate<Simulator> terminateCondition)
	{
		while(!terminateCondition.test(this))
			this.step();
	}
	
	@Deprecated
	public SimExceptionCode getLastException()
	{
		return this.pendingException;
	}
	
	@Deprecated
	public void clearException()
	{
		this.pendingException = null;
	}
	
	public void signalException(SimExceptionCode exceptionCode, int param) throws SimException
	{
		throw new SimException(exceptionCode, param);
	}
	
	public void signalException(SimExceptionCode exceptionCode) throws SimException
	{
		this.signalException(exceptionCode, 0);
	}
	
	//Since MIPS processors introduce the concept of branch delay slot, the program counter
	//should be scheduled two instruction cycles ahead.
	
	public void scheduleRelativeJump(int offset1, int offset2)
	{
		this.scheduledPC0 = this.programCounter + (offset1 & -4);
		this.scheduledPC1 = this.programCounter + (offset2 & -4);
	}
	
	public void scheduleRelativeJump(int offset2)
	{
		this.scheduledPC1 = this.programCounter + (offset2 & -4);
		this.nextInDelaySlot = true;
	}
	
	public void scheduleAbsoluteJump(int addr1, int addr2, int mask)
	{
		this.scheduledPC0 = (this.programCounter & ~mask) | (addr1 & mask & -4);
		this.scheduledPC1 = (this.programCounter & ~mask) | (addr2 & mask & -4);
	}
	
	public void scheduleAbsoluteJump(int addr2, int mask)
	{
		this.scheduledPC1 = (this.programCounter & ~mask) | (addr2 & mask & -4);
		this.nextInDelaySlot = true;
	}
	
	public int getPC()
	{
		return this.programCounter;
	}
	
	public boolean inDelaySlot()
	{
		return this.currInDelaySlot;
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
