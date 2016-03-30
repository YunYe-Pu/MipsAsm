package mipsAsm;

import mipsAsm.gui.GUIMain;

public class Main
{
	
	public static void main(String[] args)
	{
//		Assembler assembler = new Assembler(System.out);
//		String currFileName = "";
//		Scanner scanner = new Scanner(System.in);
//		do
//		{
//			assembler.clear();
//			try
//			{
//				System.out.println("Enter the input file names in one line, separate with a space:");
//				String input = scanner.nextLine().trim();
//				
//				while(!input.isEmpty())
//				{
//					int beginPos, endPos;
//					if(input.startsWith("\""))
//					{
//						beginPos = 1;
//						endPos = input.indexOf('\"', 1);
//					}
//					else
//					{
//						beginPos = 0;
//						endPos = input.indexOf(' ');
//					}
//					if(endPos < 0) endPos = input.length();
//					currFileName = input.substring(beginPos, endPos);
//					if(endPos >= input.length())
//						input = "";
//					else
//						input = input.substring(endPos + 1).trim();
//					assembler.assemble(new File(currFileName));
//				}
//			
//				BitStream output = assembler.linkGlobal();
//				System.out.println("Enter the output file name:");
//				currFileName = scanner.nextLine().trim();
//				PrintWriter outFile = new PrintWriter(new File(currFileName));
//				outFile.print(output.getAsCOE());
//				outFile.close();
//				System.out.println("Assembly successful.");
//			}
//			catch (AsmError e)
//			{
//				System.out.print(e.getLocalizedMessage());
//				System.out.println("Assembly terminated.");
//			}
//			catch(FileNotFoundException e)
//			{
//				System.out.println("Error: Cannot open file " + currFileName);
//				System.out.println("Assembly terminated.");
//			}
//			System.out.println("Type \"next\" to assemble another program, or anything else to exit.");
//
//		} while(scanner.nextLine().equalsIgnoreCase("next"));
//		
//
//		scanner.close();
		
		
//		int[] progData = {
//			0x27bdfff0,
//			0xafbe000c,
//			0x03a0f025,
//			0xafc40010,
//			0xafc50014,
//			0x1000000f,
//			0x00000000,
//			0x8fc30010,
//			0x8fc20014,
//			0x00000000,
//			0x14400002,
//			0x0062001a,
//			0x000001cd,
//			0x00001010,
//			0xafc20000,
//			0x8fc20014,
//			0x00000000,
//			0xafc20010,
//			0x8fc20000,
//			0x00000000,
//			0xafc20014,
//			0x8fc20014,
//			0x00000000,
//			0x1440ffef,
//			0x00000000,
//			0x8fc20010,
//			0x03c0e825,
//			0x8fbe000c,
//			0x27bd0010,
//			0x03e00008,
//			0x00000000
//		};
//		Simulator simulator = new Simulator();
//		simulator.loadProgram(progData, 0);
//		simulator.reg.set(4, 48);
//		simulator.reg.set(5, 40);
//		simulator.reg.set(29, 0x10000);
//		simulator.reg.set(30, 0x20000);
//		while(simulator.getPC() < ((progData.length - 1) << 2))
//		{
//			System.out.printf("%08x\n", simulator.getCurrInstruction());
//			simulator.step();
//		}
//		for(int i = 1; i < 32; i++)
//			System.out.printf("%08x ", simulator.reg.get(i));
//		System.out.printf("%d\n", simulator.getPC() / 4);

//		System.out.print(Disassembler.disassemble(BinaryType.read(new File("test\\2.bin"), false)));
		
		GUIMain.launch(args);
	}
	
}
