package interpreter;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A class that extends the syntax available to the user by translating the code's text into interpreter readable commands.
 * Basically an extremely dumb version of the C preprocessor.
 *
 */
public class Preprocessor {
	
	public static final String COMMENT_START = "/*";
	public static final String COMMENT_END = "*/";
	public static final String DEFINE = "#namedef";
	public static final String PLACE_SYMBOL = "#define";
	public static final String IF_DEFINED = "#ifdef";
	public static final String IF_NOT_DEFINED = "#ifndef";
	public static final String END_IF = "#endif";
	
	public static final String COMMENT_REGEX = "/\\*(.|\\s)*?\\*/";
	
	private HashMap<String, String> namedefs;
	private HashSet<String> symbols;
	private StringBuilder codeBuilder;	
	
	/**
	 * Returns a list of every command used by the interpreter.
	 */
	public static String[] getCommands() { 
		String[] names = {"Comments", DEFINE, PLACE_SYMBOL, IF_DEFINED, IF_NOT_DEFINED, END_IF};
		return names;
	}

	/**
	 * Returns a help string for the command of the specific name.
	 * @throws IllegalArgumentException if the name provided isn't a class-specific command
	 */
	public static String getCommandDescription(String commandName) throws IllegalArgumentException {
		switch(commandName) {
		case("Comments"):
			return String.format("Text wrapped in classic C-style %s,%s brackets, is ignored by the interpreter", COMMENT_START, COMMENT_END);
		case(DEFINE):
			return String.format("%s [old string] [replacement string];\n "
					+ "Replaces all words matching [old string] with [replacement string].\n "
					+ "Useful to give commands shorter names or give nodes meaningful names (since they normally only accept individual characters).",
					DEFINE);
		case(PLACE_SYMBOL):
			return String.format("%s [Symbol]\n, Defines a new symbol that"
					+ "is used to form conditional statements that ignore or include certain code blocks.\n"
					+"See %s and %s for how to form conditional statements with the preprocessor",
					PLACE_SYMBOL, IF_DEFINED, IF_NOT_DEFINED);
		case(IF_DEFINED):
			return String.format("%s [Symbol]\n"
					+ "Checks to see if [Symbol] has been defined in the lines above the command.\n"
					+ "If it isn't, ignores all commands until the next %s", IF_DEFINED, END_IF);
		case(IF_NOT_DEFINED):
			return String.format("%s [Symbol]\n"
					+ "Checks to see if [Symbol] has been defined in the lines above the command.\n"
					+ "If it is, ignores all commands until the next %s", IF_NOT_DEFINED, END_IF);
		case(END_IF):
			return "Ends the last conditional block. Keep in mind nested blocks aren't supported.\n"; 
		default:
			throw new IllegalArgumentException("Cannot get description: " + commandName + " is not a valid command");	
		}
	} 
	
	Preprocessor() {
		reset();
	}
	
	/**
	 * Returns a string of the text formatted into interpreter readable code.
	 * @param code the original text block
	 * @return a String as executable code
	 * @throws SyntaxException if invalid syntax among the preprocessor commands is detected
	 */
	String process(String code) throws SyntaxException { 
		reset();
		code = code.replaceAll(COMMENT_REGEX, "").replaceAll("(;)+", ";");
		String[] commands = AutomatonInterpreter.splitCommands(code); //remove comments
		boolean ignore = false;
		
		//process every line
		for(String command: commands) { 	
			command = command.strip();
			String[] arguments = command.split(" ");
			
			switch(arguments[0].toLowerCase()) {
			case DEFINE:
				if(!ignore)
					handleDefine(command);
				break;
				
			case PLACE_SYMBOL:
				if(!ignore)
					handleSymbolPlacement(command);
				break;
				
			case IF_DEFINED:
				if(!ignore) {
					if(arguments.length != 2)
						throw new SyntaxException(errorMessage("Invalid number of arguments in " + IF_DEFINED, command));
					if(!symbols.contains(arguments[1]))
						ignore = true;
				}
				break;
				
			case IF_NOT_DEFINED:
				if(!ignore) {
					if(arguments.length != 2)
						throw new SyntaxException(errorMessage("Invalid number of arguments in " + IF_NOT_DEFINED, command));
					if(symbols.contains(arguments[1]))
						ignore = true;
				}
				break;
				
			case END_IF:
				ignore = false;
				break;
				
			default:
				if(!ignore)
					handleLine(command);
			}
		}
		
		if(ignore)
			throw new SyntaxException("No "+ END_IF +" found after preprocessor conditional statement");
			
		return codeBuilder.toString();
	}
	
	private void handleDefine(String command) {
		String[] arguments = command.split(" ");
		
		if(arguments.length != 3)
			throw new SyntaxException(errorMessage("Invalid number of arguments in " + DEFINE, command));		
		else if (Command.commands.containsKey(arguments[1]))
			throw new SyntaxException(errorMessage("Commands cannot be overwritten.", command));
		else
			namedefs.put(arguments[1], arguments[2]);
	}
	
	private void handleSymbolPlacement(String command) {
		String[] arguments = command.split(" ");
		
		if(arguments.length != 2)
			throw new SyntaxException(errorMessage("Invalid number of arguments in " + PLACE_SYMBOL, command));
		else
			symbols.add(arguments[1]);
	}
	
	private void handleLine(String command) {
		for(String word : command.split(" |((?<=,)|(?=,))|((?<=,)|(?=,))|((?<=:))|((?=:))")) { //split on ',' ':' keeping the symbols separate
			
			if(word == ":" || word == "," || word.isBlank() || !namedefs.containsKey(word.strip()))  //if not a symbol
				codeBuilder.append(word);
			else									//if symbol
				codeBuilder.append(namedefs.get(word));
	
			codeBuilder.append(' ');	
		}
	
		codeBuilder.append(";\n");	
	}
	
	private String errorMessage(String message, String command) {
		return String.format("%s\n\t at command %s", message, command);	
	}
	
	private void reset() {
		namedefs = new HashMap<String,String>();
		codeBuilder = new StringBuilder();
		symbols = new HashSet<String>();
	}
}
