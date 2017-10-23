package mipsAsm.assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import mipsAsm.assembler.directive.Directives;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.exception.LabelNotDeclaredError;
import mipsAsm.assembler.exception.LabelRedeclareError;
import mipsAsm.assembler.instruction.Instruction;
import mipsAsm.assembler.instruction.Instructions;
import mipsAsm.assembler.instruction.LinkableInstruction;
import mipsAsm.assembler.operand.OpLabel;
import mipsAsm.assembler.operand.Operand;
import mipsAsm.assembler.util.AsmWarning;
import mipsAsm.assembler.util.BitStream;
import mipsAsm.assembler.util.LabelOccurence;
import mipsAsm.assembler.util.Occurence;

/**
 * The main assembler class.
 * This class is used for assembling multiple source files into one bit stream,
 * but does not generate individual object files.
 * 
 * @author YunYe Pu
 */
public class Assembler
{
	protected ArrayList<Instruction> instructions = new ArrayList<>();
	protected HashMap<String, LabelOccurence> globalLabelMap = new HashMap<>();

	protected HashSet<String> fileGlobalLabel = new HashSet<>();
	protected HashSet<String> fileExternalLabel = new HashSet<>();
	
	protected boolean endianess = false;
	
	//used for warning signaling.
	protected Occurence currProcessing;
	
	protected PrintStream consoleOutput;
	
	protected boolean delaySlot;
	protected boolean nextDelaySlot;
	protected int initAddr = 0;
	
	public Assembler(PrintStream consoleOutput)
	{
		this.consoleOutput = consoleOutput;
	}
	
	public void clear()
	{
		this.instructions.clear();
		this.globalLabelMap.clear();
		this.delaySlot = this.nextDelaySlot = false;
		this.initAddr = 0;
	}
	
	/**
	 * Assembles one file and link all local labels.
	 * Call this method multiple times to assemble multiple source files
	 * into one bit stream.
	 * 
	 * @param input The input file.
	 */
	public void assemble(File input) throws FileNotFoundException, AsmError
	{
		ArrayList<String> lineToken = new ArrayList<>();
		ArrayList<Operand> instrOperands = new ArrayList<>();
		ArrayList<Instruction> fileInstruction = new ArrayList<>();
		HashMap<String, LabelOccurence> fileLabelMap = new HashMap<>();
		String currMnemonic = null;
		int instructionAddr = this.instructions.size();

		boolean inComment = false;
		boolean inQuotation = false;
		StringBuilder tokenBuffer = new StringBuilder();

		this.fileGlobalLabel.clear();
		this.fileExternalLabel.clear();

		currProcessing = new Occurence(input.getName(), 0, "");
		try(Scanner scanner = new Scanner(input))
		{
			while(scanner.hasNextLine())
			{
				currProcessing = new Occurence(currProcessing.fileName, currProcessing.lineNum + 1, scanner.nextLine());

				//Tokenize current line
				char[] line = currProcessing.lineContent.toCharArray();
				char prevChar = 0;
				tokenBuffer.setLength(0);
				for(char currChar : line)
				{
					if(inComment)
					{
						prevChar = line[line.length - 1];
						break;
					}
					else if(inQuotation)
					{
						if(prevChar != '\\' && currChar == '\"')
						{
							inQuotation = false;
							tokenBuffer.append('\"');
							lineToken.add(tokenBuffer.toString());
							tokenBuffer.setLength(0);
						}
						else
							tokenBuffer.append(currChar);
					}
					else
					{
						switch(currChar) {
						case '#':
							inComment = true;
							if(tokenBuffer.length() > 0)
							{
								lineToken.add(tokenBuffer.toString());
								tokenBuffer.setLength(0);
							}
							break;
						case '\"':
							inQuotation = true;
							if(tokenBuffer.length() > 0)
							{
								lineToken.add(tokenBuffer.toString());
								tokenBuffer.setLength(0);
							}
							tokenBuffer.append('\"');
							break;
						case ':':
							tokenBuffer.append(':');
						case ' ':
						case ',':
						case '(':
						case ')':
						case '\t':
							if(tokenBuffer.length() > 0)
							{
								lineToken.add(tokenBuffer.toString());
								tokenBuffer.setLength(0);
							}
							break;
						default:
							tokenBuffer.append(currChar);
						}
					}
					prevChar = currChar;
				}
				if(inQuotation)
					throw new AsmError("Unclosed quotation", "Reached end of line with an open quotation");
				if(prevChar != '\\')
				{
					inComment = false;
					if(tokenBuffer.length() > 0)
						lineToken.add(tokenBuffer.toString());
				}
				else
					continue;

				for(String token : lineToken)
				{
					if(currMnemonic == null)//allowed token: label declaration, mnemonic
					{
						if(token.matches("[a-zA-Z_][\\w]*:"))//label declaration
						{
							String labelStr = token.substring(0, token.length() - 1);
							if(fileLabelMap.containsKey(labelStr))
								throw new LabelRedeclareError(labelStr, fileLabelMap.get(labelStr).occurence);
							else if(this.globalLabelMap.containsKey(labelStr))
								throw new LabelRedeclareError(labelStr, this.globalLabelMap.get(labelStr).occurence);
							fileLabelMap.put(labelStr, new LabelOccurence(instructionAddr + fileInstruction.size() + this.initAddr, currProcessing));
						}
						else if(token.matches("[a-zA-Z_\\.][\\w\\.]*"))//mnemonic
							currMnemonic = token;
						else
							throw new AsmError("Unrecognizable token", "Failed to parse token \"" + token + "\".");
					}
					else//allowed token: operands(arguments)
					{
						Operand operand = Operand.parse(token, this);
						if(operand instanceof OpLabel)
							((OpLabel)operand).setOccurence(currProcessing);
						instrOperands.add(operand);
					}
				}
				lineToken.clear();
				
				if(currMnemonic != null)
				{
					Operand[] operands = new Operand[instrOperands.size()];
					instrOperands.toArray(operands);
					this.nextDelaySlot = false;
					if(currMnemonic.startsWith("."))
						Directives.getHandler(currMnemonic).parse(operands, this, fileInstruction);
					else
						Instructions.getParser(currMnemonic).parse(operands, this, fileInstruction);
					this.delaySlot = this.nextDelaySlot;
				}
				instrOperands.clear();
				currMnemonic = null;
			}
			
			//link local labels in current file
			for(Instruction i : fileInstruction)
			{
				if(i instanceof LinkableInstruction)
				{
					try
					{
						((LinkableInstruction)i).link(fileLabelMap, instructionAddr + this.initAddr);
					}
					catch(LabelNotDeclaredError e)
					{
						if(!this.fileExternalLabel.contains(e.labelName))
							throw e;
					}
				}
				instructionAddr++;
			}
			
			this.instructions.addAll(fileInstruction);
			for(String s : this.fileGlobalLabel)
			{
				if(fileLabelMap.containsKey(s))
					this.globalLabelMap.put(s, fileLabelMap.get(s));
			}
		}
		catch(AsmError e)
		{
			if(e.getOccurence() == null)
				e.setOccurence(currProcessing);
			throw e;
		}
	}
	
	/**
	 * Assemble multiple source files and link, then generate a binary stream.
	 * 
	 * @param input Files to be assembled.
	 * @return A binary stream if assembly successful, or null if any error occur.
	 * @throws FileNotFoundException If unable to open the input file.
	 */
	public BitStream assemble(File[] input) throws FileNotFoundException
	{
		try
		{
			this.clear();
			for(File f : input)
				this.assemble(f);
			BitStream ret = this.linkGlobal();
			this.consoleOutput.println("Assembly successful.");
			return ret;
		}
		catch(AsmError e)
		{
			this.consoleOutput.print(e.getErrorMessage());
			this.consoleOutput.println("Assembly terminated.");
			return null;
		}
	}
	
	/**
	 * Links all the global labels and generate a binary stream.
	 */
	public BitStream linkGlobal() throws AsmError
	{
		BitStream buffer = new BitStream(this.instructions.size(), this.endianess);
		int addr = this.initAddr;
		for(Instruction i : this.instructions)
		{
			if(i instanceof LinkableInstruction)
				((LinkableInstruction)i).link(this.globalLabelMap, addr);
			buffer.append(i.toBinary());
			addr++;
		}
		return buffer;
	}
	
	public void addGlobalLabel(String label)
	{
		this.fileGlobalLabel.add(label);
	}
	
	public void addExternalLabel(String label)
	{
		this.fileExternalLabel.add(label);
	}
	
	public void setNextDelaySlot()
	{
		this.nextDelaySlot = true;
	}
	
	public boolean isInDelaySlot()
	{
		return this.delaySlot;
	}
	
	public boolean isEmpty()
	{
		return this.instructions.isEmpty();
	}
	
	public void setInitAddr(int initAddr)
	{
		this.initAddr = (initAddr >> 2) & 0x3fffffff;
	}
	
	/**
	 * Used to determine the byte ordering of instructions and directives.
	 * @return false for little-endian, true for big-endian.
	 */
	public boolean getEndianess()
	{
		return this.endianess;
	}
	
	public void setEndianess(boolean endianess)
	{
		this.endianess = endianess;
	}
	
	public void handleWarning(AsmWarning e) throws AsmError
	{
		this.consoleOutput.printf("Warning in file %s, line %d: %s\n", currProcessing.fileName, currProcessing.lineNum, e.getType());
		this.consoleOutput.println(e.getMessage());
		this.consoleOutput.println(currProcessing.lineNum + "  " + currProcessing.lineContent);
		this.consoleOutput.println();
	}
	
}
