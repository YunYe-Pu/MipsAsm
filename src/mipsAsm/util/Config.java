package mipsAsm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import javafx.beans.property.SimpleBooleanProperty;

public class Config
{
	private final File configFile;
	public File initDirectory;
	public final SimpleBooleanProperty endian = new SimpleBooleanProperty(false);
	public HashMap<String, FileFormat> formats = new HashMap<>();
	public ArrayList<FileFormat> formatList = new ArrayList<>();
	
	public Config(File configFile) throws IOException
	{
		this.configFile = configFile;
		InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), "UTF-8");
		JsonObject configObj = (new JsonParser()).parse(reader).getAsJsonObject();
		this.initDirectory = new File(configObj.get("initDirectory").getAsString());
		this.endian.set(configObj.get("endian").getAsBoolean());
		for(JsonElement entry : configObj.get("formats").getAsJsonArray())
		{
			FileFormat fmt = new FileFormat(entry.getAsJsonObject());
			this.formats.put(fmt.extension, fmt);
			this.formatList.add(fmt);
		}
	}
	
	public Config()
	{
		this.configFile = new File("config");
		this.initDirectory = new File(".");
		FileFormat coe = new FileFormat(
			".coe", "COE File",
			"memory_initialization_radix=16;\nmemory_initialization_vector=\n", ",\n", ";\n",
			"[0-9A-Fa-f]{8}[,;]", 0, 0,
			new String[]{"memory_initialization_radix=16;", "memory_initialization_vector="},
			new int[]{Pattern.CASE_INSENSITIVE, Pattern.CASE_INSENSITIVE});

		FileFormat mif = new FileFormat(".mif", "Memory initialization File", 
			"", "\n", "\n",
			"[0-9A-Fa-f]{8}", 0, 0,
			new String[0],
			new int[0]);
		
		this.formats.put("coe", coe);
		this.formats.put("mif", mif);
		this.formatList.add(coe);
		this.formatList.add(mif);
	}
	
	public void saveConfig() throws IOException
	{
		JsonObject rootObj = new JsonObject();
		rootObj.add("initDirectory", new JsonPrimitive(this.initDirectory.getPath()));
		rootObj.add("endian", new JsonPrimitive(this.endian.get()));
		JsonArray fmtArray = new JsonArray();
		for(FileFormat e : this.formatList)
			fmtArray.add(e.toJson());
		rootObj.add("formats", fmtArray);
		char[] jsonText = rootObj.toString().toCharArray();
		//Format the json text
		try(PrintWriter output = new PrintWriter(this.configFile))
		{
			int indent = 0;
			boolean quotation = false;
			char prevChar = 0;
			StringBuilder indentTypes = new StringBuilder(10);
			for(char c : jsonText)
			{
				if(quotation == false)
				{
					switch(c) {
					case '{':
						indent++;
						output.println("{");
						printIndent(indent, output);
						indentTypes.append('{');
						break;
					case '}':
						indent--;
						output.println();
						printIndent(indent, output);
						output.print("}");
						indentTypes.setLength(indentTypes.length() - 1);
						break;
					case ':':
						output.print(" : ");
						break;
					case ',':
						output.print(",");
						if(indentTypes.charAt(indentTypes.length() - 1) == '{')
						{
							output.println();
							printIndent(indent, output);
						}
						break;
					case '[':
						output.print('[');
						indentTypes.append('[');
						break;
					case ']':
						output.print(']');
						indentTypes.setLength(indentTypes.length() - 1);
						break;
					case '\"':
						quotation = true;
					default:
						output.print(c);
					}
				}
				else
				{
					if(prevChar != '\\' && c == '\"')
						quotation = false;
					output.print(c);
				}
				prevChar = c;
			}
			output.println();
		}
	}
	
	private static void printIndent(int indent, PrintWriter output)
	{
		while(indent-- > 0)
			output.print("\t");
	}
	
}
