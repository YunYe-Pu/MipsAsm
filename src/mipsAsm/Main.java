package mipsAsm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.util.BitStream;
import mipsAsm.disassembler.Disassembler;
import mipsAsm.gui.GUIMain;
import mipsAsm.util.BinaryType;

public class Main
{
	private enum Task { ASSEMBLE, DISASSEMBLE, SIMULATE };
	
	
	public static void main(String[] args)
	{
//		Assembler assembler = new Assembler(System.out);
//		File f = new File("C:/Users/YunYe Pu/Documents/Xilinx/SummerProj/src/asm/main.s");
//		try
//		{
//			assembler.assemble(f);
//			BitStream s = assembler.linkGlobal();
//		}
//		catch(FileNotFoundException | AsmError e)
//		{
//			e.printStackTrace();
//		}
		
		ArrayList<File> input = new ArrayList<>();
		ArrayList<File> output = new ArrayList<>();
		Task task = Task.ASSEMBLE;
		boolean outputOption = false;
		boolean endian = false;
		boolean gui = false;
		
		for(String s : args)
		{
			if(s.equals("-h") || s.equals("--help"))
			{
				printUsage();
				return;
			}
			else if(s.equals("-b") || s.equals("--big-endian"))
				endian = true;
			else if(s.equals("-l") || s.equals("--little-endian"))
				endian = false;
			else if(s.equals("-g") || s.equals("--gui"))
				gui = true;
			else if(s.equals("-a") || s.equals("--assemble"))
				task = Task.ASSEMBLE;
			else if(s.equals("-d") || s.equals("--disassemble"))
				task = Task.DISASSEMBLE;
			else if(s.equals("-s") || s.equals("--simulate"))
				task = Task.SIMULATE;
			else if(s.equals("-i") || s.equals("--input"))
				outputOption = false;
			else if(s.equals("-o") || s.equals("--output"))
				outputOption = true;
			else if(s.startsWith("-"))
			{
				System.out.println("Unknown option " + s + ".");
				printUsage();
				return;
			}
			else
			{
				if(outputOption)
					output.add(new File(s));
				else
					input.add(new File(s));
			}
		}
		
		File[] inputFiles = new File[input.size()];
		input.toArray(inputFiles);
		File[] outputFiles = new File[input.size()];
		output.toArray(outputFiles);
		
		if(gui)
			GUIMain.launchGUI(inputFiles, endian, args);
		else
			launchCLI(task, endian, inputFiles, outputFiles);
	}
	
	public static void printUsage()
	{
		System.out.println("Available options:\n" + 
			"-h, --help           Display this help and exit.\n" +
			"-g, --gui            Launch graphical user interface.\n" +
			"-i, --input <files>  Specify the input source files.\n" +
			"-o, --output <files> Specify the output file. Output type depends on the extension of the file.\n\n" +
			"-a, --assemble       Assemble the input source files.(default)\n" +
			"-d, --disassemble    Disassemble the input binary.\n" +
			"-s, --simulate       Launch simulation on the input binary.\n\n" +
			"-b, --big-endian     Configure the assembler, disassembler, and simulator as big-endian.\n" +
			"-l, --little-endian  Configure the assembler, disassembler, and simulator as little-endian.(default)"
		);
	}
	
	public static void launchCLI(Task task, boolean endian, File[] input, File[] output)
	{
		switch(task) {
		case ASSEMBLE:
			if(input.length == 0)
			{
				System.out.println("No input file. Assembly terminated.");
				return;
			}
			runAssemble(input, output.length == 0? new File("a.coe"): output[0], endian);
			break;
		case DISASSEMBLE:
			if(input.length == 0)
			{
				System.out.println("No input file. Disassembly terminated.");
				return;
			}
			runDisassemble(input, output, endian);
			break;
		case SIMULATE:
			System.out.println("The command line simulation function is not yet implemented.");
			System.out.println("Please launch GUI to use the simulation function.");
			return;
		}
	}
	
	
	private static void runAssemble(File[] input, File output, boolean endian)
	{
		Assembler assembler = new Assembler(System.out);
		assembler.setEndianess(endian);
		
		try
		{
			BitStream s = assembler.assemble(input);
			PrintWriter p;
			switch(BinaryType.getType(output)) {
			case COE:
				p = new PrintWriter(output);
				p.print(s.getAsCOE().toString());
				p.close();
				break;
			case HEX:
				p = new PrintWriter(output);
				p.print(s.getAsHexString().toString());
				p.close();
				break;
			case BIN:
				s.writeBinary(output);
				break;
			}
			System.out.println("Assembly successful.");
		}
		catch(FileNotFoundException e1)
		{
			System.out.println("Failed to open input file: " + e1.getLocalizedMessage());
			System.out.println("Assembly terminated.");
		}
		catch(IOException e2)
		{
			System.err.println("An I/O exception occured when writing output file: " + e2.getLocalizedMessage());
			System.out.println("Assembly terminated.");
		}
	}
	
	private static void runDisassemble(File[] input, File[] output, boolean endian)
	{
		File outputFile;
		try
		{
			for(int i = 0; i < input.length; i++)
			{
				if(i >= output.length)
					outputFile = new File(i + ".s");
				else
					outputFile = output[i];
				
				int[] binary = BinaryType.read(input[i], endian);
				if(binary == null)
				{
					System.out.println("Wrong file format for binary.");
					System.out.println("Disassembly terminated.");
					return;
				}
				PrintWriter p = new PrintWriter(outputFile);
				p.print(Disassembler.disassemble(binary).toString());
				p.close();
			}
			System.out.println("Disassembly successful.");
		}
		catch(FileNotFoundException e1)
		{
			System.out.println("Failed to open input file: " + e1.getLocalizedMessage());
			System.out.println("Disassembly terminated.");
		}
		catch(IOException e2)
		{
			System.out.println("An I/O exception occured when reading input file: " + e2.getLocalizedMessage());
			System.out.println("Disassembly terminated.");
		}
	}
	
}
