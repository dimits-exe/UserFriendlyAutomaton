package interpreter;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import automaton.Automaton;
import automaton.DFA;
import automaton.NFA;

/**
 * An enumeration containing information about, defining and implementing all known commands to the interpreter.
 *
 * @author dimits
 *
 */
enum Command {
		
		ADD ("add <node_name>,<node_name>,...\n"+
				"Creates new nodes with the selected names (in uppercase) and adds them to the automaton",true,true){
			@Override
			protected String executeCommand(AutomatonInterpreter interp, String args) {
				StringBuffer sb = new StringBuffer("Created node(s) ");
				for(char c : parseNames(args)) {
					// if all names accepted
					interp.automaton.addNode(c);
					sb.append(c);
					sb.append(", ");
				}
				sb.setLength(sb.length()-2);
				return sb.toString();
			}

		},
		
		EXECUTE( "Returns a nice formatted string informing the user whether or not the supplied word is accepted by the automaton.\n"+
				"A word is accepted if the node on which the last character of the word is considered an 'accept-state' (see 'help make_accept')",true,true) {
			@Override
			protected String executeCommand(AutomatonInterpreter interp, String word) {
				return interp.automaton.testAndGetMessage(word);
			}

		},
		
		SHOW ("show <node_name>, <node_name>\n"+
				"show all\n"+
				"Shows information about each of the provided / all of the nodes.",false,true){
			@Override
			protected String executeCommand(AutomatonInterpreter interp, String tokens) {
				if(tokens.strip().equals("all"))
					return interp.automaton.toString();
				else {
					char[] letters =  parseNames(tokens);
					StringBuffer sb = new StringBuffer();
					for(char c: letters)
						sb.append(interp.automaton.getNodeInfo(c).toString());
					
					return sb.toString();
				}
			}

		},
		
		CONNECT ("connect <node_name> to <letter>:<targetName>, <letter>:<targetName>, ...\n"+
				" connect <node_name> -> <letter>:<targetName>, <letter>:<targetName>, ...\n"+
				"Connects the target node to each of the following nodes according to the alphabet that was provided\n"+
				"For example if alphabet = 'a','b', typing 'connect A to C,D' means  A--(a)--> C, A--(b)-->D.",true,true) {
			
			@Override
			protected String executeCommand(AutomatonInterpreter interp, String args) {
				String delimiter = null;
				if(args.contains("to")) delimiter = "to";
				else if(args.contains("->")) delimiter = "->";
				
				if(delimiter == null)
					throw new SyntaxException("No 'to' or '->' delimiter found.");
				
				String[] tokens = args.split(delimiter);
				parseNodeConnections(tokens[0].charAt(0),tokens[1], interp.automaton);
				return "Nodes succesfully connected";
			}
			
			// connect A -> a:B == aut.connect(A, a, b)
			private void parseNodeConnections(char node, String args, Automaton aut) {
				String[] tokens = args.split(",");
				for(String s : tokens) {
					s = s.replaceAll(" ","");
					if(!s.matches(".:."))
						throw new SyntaxException(String.format("Argument %s does not match format <letter>:<targetName>.",s));
					else {
						//System.out.printf("Node %s connected to  node %c with letter %s", s.charAt(0),)
						aut.connect(node, s.charAt(0) , s.charAt(2));
					}
					
				}
			}

		},
		
		ACCEPT("Node_Accept <node_name>,<node_name>,...\n"+
				"Turns all the provided nodes into accept-state (nodes that when read last, define that the computation is successful).",true,true) {
			
			@Override
			protected String executeCommand(AutomatonInterpreter interp, String args) {
				char[] c_ls = parseNames(args);
				StringBuffer sb = new StringBuffer();
				
				for(char c: c_ls) {
					interp.automaton.makeAcceptState(c);
					sb.append(c);
					sb.append(" ,");
				}
				sb.setLength(sb.length()-2); //remove ','
				
				if(c_ls.length > 1)
					return String.format("Nodes %s are now accept-states.", sb.toString());
				else
					return String.format("Node %s is now an accept-state.", sb.toString());
			}

		},
		
		HELP("help <command_name>\nDisplays a basic description of the given command.",false,false) {
			
			@Override
			protected String executeCommand(AutomatonInterpreter interp,String args) {
				Command com = commands.get(args);
				if(com == null)
					return "Invalid command; " + COMMAND_HELP_LIST;
				else 
					return com.description;
			}

		},
		
		INIT("create_new <type T> a,b,c,...\n"+
				"Creates a new automaton of type T with the alphabet {a,b,c,...}. This needs to be the FIRST command in order to run the other commands",true,false){

			@Override
			protected String executeCommand(AutomatonInterpreter interp, String tokens) throws RuntimeException {
	
				int indexOfDelimeter = tokens.indexOf(' ');
				if(indexOfDelimeter == -1)
					throw new SyntaxException("Invalid number of arguments, specify type and alphabet.");
				
				String type = tokens.substring(0, indexOfDelimeter);
				String letters = tokens.substring(indexOfDelimeter);

				switch(type) { //find type
				//this would normally be within a factory class as the interpreter has no business knowing all the subclasses of Automaton 
					case "nfa":
						interp.automaton = new NFA(parseNames(letters)); 
						break;
					case "dfa":
						interp.automaton = new DFA(parseNames(letters)); 
						break;
					default:
						throw new SyntaxException(type.toUpperCase() + " is not a valid automaton type");
				}

				interp.record = new LinkedList<String>(); //wipe previous data
				return "Succesfully initialized the automaton";
			}
			
		},
		
		EXPORT("export <your_file_name>\n" +
				"Writes all the successfully executed commands to an external file which can be loaded later to replicate the automaton",false,false){

			@Override
			protected String executeCommand(AutomatonInterpreter interp, String tokens) throws RuntimeException {
				if(tokens.strip().equals(""))
					throw new SyntaxException("No file name given.");
				
				interp.exportToFileSystem(Paths.get(tokens).toFile());
				
				return "Export successful!";
			}
			
		},
		
		IMPORT("import <file_name>\n"+
						"Opens the file with the provided name and executes its commands one by one. File must be in the same directory as the program.",false,false){

			@Override
			protected String executeCommand(AutomatonInterpreter interp, String fileName) throws RuntimeException {
				
				if(fileName.strip().equals(""))
					throw new SyntaxException("No file name given.");
				
				interp.importFromFileSystem(Paths.get(fileName).toFile());
				
				return "File execution successful";
			}
			
		},
		
		EXIT("The final command for the interpreter. No more commands will be accepted after this call and the program should exit.",false,false){
			@Override
			protected String executeCommand(AutomatonInterpreter interp, String tokens) throws RuntimeException {
				interp.close();
				return "Closing interpeter...";
			}

		};
		
	
//Command class
	
	// general static data
		static final HashMap<String,Command> commands = new HashMap<String,Command>();
		
		static final Iterable<String> reservedWords = Arrays.asList(new String[]{"all", "->", "to", ":", "dfa","nfa", "DFA","NFA"});
		
		/**A description of all defined commands, automatically created at program execution*/ 
		static final String COMMAND_HELP_LIST; 
		
		/**The character used to separate multiple commands.*/
		static final char COMMAND_DELIMETER = ';';
	
	// methods
		/**
		 * Executes a method modifying the internal state of the provided automaton based on the parameters given.
		 * @param automaton the automaton to be modified
		 * @param tokens the string containing the parameters
		 * @return a confirmation message
		 * @throws RuntimeException any error that might be caused by faulty parameters or thrown by the automaton itself
		 */
		protected abstract String executeCommand(AutomatonInterpreter automaton, String tokens) throws RuntimeException;
		
		final String execute(AutomatonInterpreter interp, String tokens) {
			String output;
			try {
				output = executeCommand(interp, tokens);
			} catch(ArrayIndexOutOfBoundsException e) {
				throw new SyntaxException("Too few parameters for command call; Use the 'help' command for a valid definition.");
			}
			return output;
		}
		
		private Command(String description, boolean isExportable, boolean isMutable){
			this.description = description;
			this.isExportable = isExportable;
			this.isMutable = isMutable;
		}
		
		/**
		 * Receives a string in the form of "A,B,C,D", returns an array of ['A','B','C','D'] ignoring whitespace.
		 * @throws IllegalArgumentException if the characters within the commas aren't a single character or a special character (null, ?).
		 */
		private static char[] parseNames(String args) {
			String[] tokens = args.replace(" ","").split(",");
			char[] names = new char[tokens.length];
			
			 //fill array with individual characters
			for(int i=0; i<tokens.length; i++) { 
				
				String error = null;
				if(tokens[i].length() > 1)
					error = " node names must be one character long";
				else if (tokens[i].charAt(0) == '\u0000')
					error = " node name cannot be the null character";
				else if (tokens[i].charAt(0) == '?')
					error = " node name cannot be the '?' character";
				
				if(error != null)
					throw new InterpreterException(String.format("Invalid node name: %s\n%s",tokens[i],error));
				else
					names[i] = tokens[i].charAt(0); //insert i-th character
			}
			 
			return names;
		}
		
	// command data
		final String description;
		
		/**Whether or not the command can should written to an external file.*/
		final boolean isExportable;
		
		/** Whether the command needs an already constructed automaton to function.*/
		final boolean isMutable;
	
	// build static data
		static {
			// build command list
			commands.put("add", Command.ADD);
			commands.put("connect", Command.CONNECT);
			commands.put("show",Command.SHOW);
			commands.put("execute", Command.EXECUTE);
			commands.put("node_accept", Command.ACCEPT);
			commands.put("create_new", Command.INIT);
			commands.put("help" ,Command.HELP);
			commands.put("exit", Command.EXIT);
			commands.put("export", Command.EXPORT);
			commands.put("import", Command.IMPORT);	
			
			// build help string
			StringBuilder sb = new StringBuilder("Type 'help <command>' for how to use each command\nCommand list:\n");
			
			LinkedList<String> command_names = new LinkedList<String>(commands.keySet());
			Collections.sort(command_names);
			for(String str_com : command_names) //insert sorted elements
				sb.append(String.format(">%s\n", str_com));
			
			sb.setLength(sb.length()-1);
			COMMAND_HELP_LIST = sb.toString();			
		}
	}
