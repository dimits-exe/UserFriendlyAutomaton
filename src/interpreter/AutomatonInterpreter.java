package interpreter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;

import automaton.Automaton;

/**
 * A class that uses an instance of Automaton to modify / show information about it while also protecting the user from internal errors.
 * Used as a console for the end user. Make sure to modify the 'out' and 'err' streams if used in a non-terminal application.
 *
 * @author dimits
 */
public class AutomatonInterpreter {
	
	Automaton automaton = null;
	/** A list of successful commands inputed in the current session.*/
	LinkedList<String> record; 
	
	private boolean isClosed = false;
	private Set<File> importNames = new HashSet<File>(); //prevents recursive imports
	private PrintStream out, err;
	private boolean executionWasSuccessful;
	
//static methods
	
	static String[] splitCommands(String commands) {
		commands = commands.replace("\n", "").replace("\t", "");
		return commands.split(Character.toString(Command.COMMAND_DELIMETER)); //boo hoo I can't make a regex out of a char boo hoo
	}
	
//member methods
		
	/**
	 * Creates a new Interpreter that writes to the standard output stream.
	 */
	public AutomatonInterpreter() {
		this(System.out, System.err);
	}
	
	/**
	 * Creates a new Interpreter that writes to the specified output stream.
	 * @param out the stream where output messages will be displayed
	 */
	public AutomatonInterpreter(PrintStream out, PrintStream err) {
		record = new LinkedList<String>();
		setOutStream(out);
		setErrStream(err);

		this.out.println("Type 'exit' to terminate the interpreter, or type 'help' for a list of commands.");
		this.out.println("Note: All commands are NOT case-sensitive.");
	}
	
	/**
	 * Returns whether or not the driver is finished. A closed interpreter will not respond to further queries.
	 */
	public boolean isClosed() {return isClosed;}
	
	/**
	 * Whether the last execution of the interpreter was successful.
	 * 
	 * @return true if all commands were properly executed, false otherwise. 
	 */
	public boolean wasSuccessful() {return executionWasSuccessful;}
	
	/**
	 * Closes the interpreter. No more commands will be accepted.
	 */
	public void close() {
		//flush internal data
		out = null;
		err = null;
		automaton = null;
		record = null;
		importNames = null;
		
		isClosed = true;
	}
	
	/**
	 * Deletes the old automaton and flushes internal memory.
	 */
	public void reset() {
		automaton = null;
		record = new LinkedList<String>(); 
		//keep imported files in memory
	}
	
	/**
	 * Changes where the interpreter prints output messages.
	 * @param out the new stream
	 */
	public void setOutStream(PrintStream out) {this.out = out;}
	
	/**
	 * Changes where the interpreter prints error messages.
	 * @param out the new stream
	 */
	public void setErrStream(PrintStream err) {this.err = err;}

	/**
	 * Executes a command on the automaton. Sends any errors in the standard error stream.
	 * @param s containing the command-keyword and followed by any parameters depending on the command.
	 */
	public void executeCommand(String s) {
		String str_com = s.split(" ")[0].toLowerCase().strip(); 		//first word == command keyword
		Command com = Command.commands.get(str_com);
		boolean successful = true;
		
		if(isClosed)
			throw new InterpreterException("The Interpeter can not accept commands when closed");
		
		if(com == null) { 
			err.println(String.format("%s Unknown command \"%s\"", SyntaxException.ERROR_MESSAGE , str_com));
			successful = false;
		}
		else if (automaton == null && com.isMutable) {  //IF QUERY TO CHANGE STATE OF NON-INITIALIZED AUTOMATON
			err.println(InterpreterException.ERROR_MESSAGE + "No defined automaton: See 'help create_new'");	
			successful = false;
		}
		else { 															 
			//run valid command			
			try {
				//send the 2nd argument, removing all whitespace
				String arguments  = s.substring(str_com.length()).toLowerCase().strip();
				String confirmationMessage = com.execute(this, arguments);
				this.out.println(confirmationMessage); 
			}		
			catch(InterpreterException | SyntaxException ie) {
				this.err.println(ie.getMessage());
				successful = false;
			}
			catch(RuntimeException e) {
				this.err.println(InterpreterException.ERROR_MESSAGE + e.getMessage()); 
				successful = false;
			}
		}
		
		if(successful && !isClosed) 
			record.add(s);
		
		executionWasSuccessful = successful;
	}
	
	/**
	 * Executes a series of commands separated by the COMMAND_DELIMETER at once.
	 * @param commands the text including all the commands
	 * @see Command#COMMAND_DELIMETER
	 */
	public void executeBatch(String commands) {
		if(commands.isBlank())  {
			err.println(SyntaxException.ERROR_MESSAGE + "No text found.");
			executionWasSuccessful = false;
			return;
		}
			
		int line = 1;
		String actualCode;
		
		try {
			actualCode = Preprocessor.process(commands);
		} catch(SyntaxException se) {
			err.println(se.getMessage());
			executionWasSuccessful = false;
			return;
		}
		
		for(String command : splitCommands(actualCode)) {
			if(isClosed) {
				executionWasSuccessful = true;
				return;
			}
			
			executeCommand(command);
			if(!executionWasSuccessful) {
				err.printf("\t at command number %d, \"%s\"\n",line, command);
				return;
			} else
				line++;
		}
	}
	
	/**
	 * Initializes the automaton by executing all commands within a file
	 * @param file the file containing the code
	 */
	void importFromFileSystem(File file) throws InterpreterException {
		if(importNames.contains(file))
			return;
		
		importNames.add(file);
		
		try (Scanner in = new Scanner(file)){
			reset(); //wipe previous data, keep data from import
			
			//read lines
			StringBuilder sb = new StringBuilder();
			while(in.hasNext()) 
				sb.append(in.nextLine());
			
			//execute lines
			in.close();
			executeBatch(sb.toString());
			
		} catch (IOException e) {
			reset();
			err.println(e.getMessage().length() == 0 ?
					"An error occured while opening the file" : e.getMessage()); 
		}
	}
	
	/**
	 * Writes all essential commands to an external file
	 * @param file the file to be written
	 */
	void exportToFileSystem(File file) {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			
			for(String s : record) {			//write all commands in the file
				String commandString = s.split(" ")[0].toLowerCase().strip();
				Command com = Command.commands.get(commandString);
				if(com.isExportable) { 			//if writable command
					writer.write(s);
					writer.newLine(); 				//OS independent
				}
			}

		} catch (IOException e) {
			err.println(e.getMessage().length() == 0 ?
					"An error occured while opening the file" : e.getMessage()); //throw an exception that can be caught in the main function
		}
	}

}
