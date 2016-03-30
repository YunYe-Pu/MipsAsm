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
import mipsAsm.assembler.util.AsmWarningHandler;
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
public class Assembler implements AsmWarningHandler
{
	protected ArrayList<Instruction> instructions = new ArrayList<>();
	protected HashMap<String, LabelOccurence> globalLabelMap = new HashMap<>();

	protected HashSet<String> fileGlobalLabel = new HashSet<>();
	
	protected boolean endianess = false;
	
	//used for warning signaling.
	protected Occurence currProcessing;
	
	protected PrintStream consoleOutput;
	
	public Assembler(PrintStream consoleOutput)
	{
		this.consoleOutput = consoleOutput;
	}
	
	public void clear()
	{
		this.instructions.clear();
		this.globalLabelMap.clear();
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
		boolean concatenate = false;
		boolean nextConcatenate = false;
		ArrayList<String> lineToken = new ArrayList<>();
		ArrayList<Operand> instrOperands = new ArrayList<>();
		ArrayList<Instruction> fileInstruction = new ArrayList<>();
		HashMap<String, LabelOccurence> fileLabelMap = new HashMap<>();
		String currMnemonic = null;
		int instructionAddr = this.instructions.size();
		
		this.fileGlobalLabel.clear();

		currProcessing = new Occurence(input.getName(), 0, "");
		try(Scanner scanner = new Scanner(input))
		{
			while(scanner.hasNextLine())
			{
				currProcessing = new Occurence(currProcessing.fileName, currProcessing.lineNum + 1, scanner.nextLine());
				lineToken.clear();
				//insert spaces
				String processedLine = currProcessing.lineContent.replaceAll(":", ": ").replaceAll("\\$", " \\$").replaceAll("[,\\(\\)]", " ");
				//remove comments
				processedLine = processedLine.replaceAll("#.*", "");
				
				if(processedLine.endsWith("\\"))
				{
					nextConcatenate = true;
					processedLine = processedLine.substring(0, processedLine.length() - 1);
				}
				
				splitTokens(processedLine, lineToken);
				
				for(String token : lineToken)
				{
					if(currMnemonic == null)//allowed token: label declaration, mnemonic
					{
						if(token.matches("[a-zA-Z_][\\w]*:"))//label declaration
						{
							String labelStr = token.substring(0, token.length() - 1);
							if(fileLabelMap.containsKey(labelStr) || this.globalLabelMap.containsKey(labelStr))
								throw new LabelRedeclareError(labelStr, fileLabelMap.get(labelStr).occurence);
							fileLabelMap.put(labelStr, new LabelOccurence(instructionAddr + fileInstruction.size(), currProcessing));
						}
						else if(token.matches("[a-zA-Z_\\.][\\w\\.]*"))//mnemonic
							currMnemonic = token;
						else
							throw new AsmError("Unrecognizable token", "Failed to parse token \"" + token + "\".");
					}
					else//allowed token: operands(arguments)
					{
						Operand operand = Operand.parse(token);
						if(operand instanceof OpLabel)
							((OpLabel)operand).setOccurence(currProcessing);
						instrOperands.add(operand);
					}
				}
				
				if(!concatenate)
				{
					if(currMnemonic != null)
					{
						Operand[] operands = new Operand[instrOperands.size()];
						instrOperands.toArray(operands);
						if(currMnemonic.startsWith("."))
							Directives.getHandler(currMnemonic).handle(operands, this, fileInstruction);
						else
							Instructions.getParser(currMnemonic).parse(operands, this, fileInstruction);
					}
					instrOperands.clear();
					currMnemonic = null;
				}

				concatenate = nextConcatenate;
			}
			
			//link local labels in current file
			for(Instruction i : fileInstruction)
			{
				if(i instanceof LinkableInstruction)
				{
					try
					{
						((LinkableInstruction)i).link(fileLabelMap, instructionAddr);
					}
					catch(LabelNotDeclaredError e)
					{
						if(!this.fileGlobalLabel.contains(e.labelName))
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
			this.consoleOutput.print(e.getLocalizedMessage());
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
		int addr = 0;
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
	
	@Override
	public void handleWarning(AsmWarning e)
	{
		this.consoleOutput.printf("Warning in file %s, line %d: %s\n", currProcessing.fileName, currProcessing.lineNum, e.getType());
		this.consoleOutput.println(e.getMessage());
		this.consoleOutput.println(currProcessing.lineNum + "  " + currProcessing.lineContent);
		this.consoleOutput.println();
	}
	
	private void splitTokens(String processedLine, ArrayList<String> tokens)
	{
		String[] split = processedLine.split("\\s");
		String prevString = null;
		for(String s : split)
		{
			if(s.isEmpty()) continue;
			if(prevString == null)
			{
				if(s.startsWith("\"") && !(s.endsWith("\"") && !s.endsWith("\\\"")))
					prevString = s;
				else
					tokens.add(s);
			}
			else
			{
				if(s.endsWith("\"") && !s.endsWith("\\\""))
					tokens.add(prevString + " " + s);
				else
					prevString = prevString + s;
			}
		}
	}
}
