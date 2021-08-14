package interpreter;

import java.io.PrintStream;
import editor.TranslatorInterface;

/**
 * A wrapper class translating {@link editor.Editor} commands into {@link AutomatonInterpreter} ones.
 * 
 * @author dimits
 */
public class InterpreterInterface implements TranslatorInterface {
	
	private final AutomatonInterpreter interp;
	
	public InterpreterInterface() {
		interp = new AutomatonInterpreter();
	}
	
	public InterpreterInterface(PrintStream out, PrintStream err) {
		interp = new AutomatonInterpreter(out, err);
	}

	@Override
	public boolean testForErrors(String code, PrintStream errorOutput) {
		try(PrintStream noOutputStream = new PrintStream(new editor.NullOutputStream());
				PrintStream textAreaOutputStream = errorOutput) {
			AutomatonInterpreter testInterp = new AutomatonInterpreter(noOutputStream, textAreaOutputStream);
			testInterp.executeBatch(code);
			return testInterp.wasSuccessful();
		} //release resources after exiting
	}

	@Override
	public void execute(String code) {
		interp.executeBatch(code);
	}

	@Override
	public boolean wasSuccessful() {
		return interp.wasSuccessful();
	}

	@Override
	public void reset() {
		interp.reset();
	}

	@Override
	public Iterable<String> getPrimaryCommands() {
		return Command.commands.keySet();
	}

	@Override
	public Iterable<String> getSecondaryCommands() {
		return Preprocessor.getCommands();
	}

	@Override
	public Iterable<String> getReservedWords() {
		return Command.reservedWords;
	}
	
	@Override
	public String getCommentRegex() {
		return Preprocessor.COMMENT_REGEX;	
	}

	@Override
	public String getCommandDescription(String commandName) throws IllegalArgumentException {
		Command c = Command.commands.get(commandName);
		if(c != null) 	//if interpreter command, return description
			return c.description;
		
		else 			//if not an interpreter command, look in preprocessor
			//will throw the exception if it doesn't exist there either
			return Preprocessor.getCommandDescription(commandName);
	}

	@Override
	public void setOutputStream(PrintStream out) {
		interp.setOutStream(out);
	}

	@Override
	public void setErrorStream(PrintStream err) {
		interp.setErrStream(err);
	}

}
