package interpreter;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A class that extends the syntax available to the user by translating the
 * code's text into interpreter readable commands. Basically an extremely dumb
 * version of the C preprocessor.
 *
 * @author dimits, alexm
 */
public class Preprocessor {

	/* Don't let anyone instantiate this class */
	private Preprocessor() {}

	/** Enum-strategy to handle different type of Preprocessor commands */
	private enum Token {

		/** Token for a default (non-preprocessor) line */
		DEFAULT("<default>", "", "Non-preprocessor command") {
			@Override
			void handleLine(String line, ToolSet tools) {
				if (!tools.ignore) {

					//split on ',' ':' keeping the tools.symbols separate
					final String[] tokens = line.split(" |(?<=,)|(?=,)|(?<=:)|(?=:)");

					for (final String word : tokens) {

						if (isNotSymbol(word, tools.namedefs))
							tools.codeBuilder.append(word);
						else
							tools.codeBuilder.append(tools.namedefs.get(word));

						tools.codeBuilder.append(' ');
					}

					tools.codeBuilder.append(";\n");
				}
			}

			private boolean isNotSymbol(String word, HashMap<String,String> namedefs) {
				return word.equals(":") || word.equals(",") || word.isBlank()
				        || !namedefs.containsKey(word.strip());
			}
		},

		/** Token for comments */
		COMMENTS("Comments", "/* ... */",
		        "Text surrounded by %s,%s. It is ignored by the Interpreter.",
		        "/*", "*/") {
			@Override
			void handleLine(String line, ToolSet tools) {
				// if a line starts with 'Comments' this would get executed
				// so just call the default line handling.
				DEFAULT.handleLine(line, tools);
			}
		},

		/** Token for #namedef preprocessor command */
		DEFINE("#namedef", "%s [old string] [replacement string];",
		        "Replaces all occuresnces of [old string] with [replacement string].") {
			@Override
			void handleLine(String line, ToolSet tools) {
				if (!tools.ignore) {
					final String[] arguments = line.split(" ");

					Token.checkArgCount(arguments, 3, DEFINE, line);

					if (Command.commands.containsKey(arguments[1]))
						throw new SyntaxException(
						        errorMessage("Commands cannot be overwritten.", line));

					tools.namedefs.put(arguments[1], arguments[2]);
				}
			}
		},

		/** Token for #ifdef preprocessor command */
		IF_DEFINED("#ifdef", "%s [Symbol]",
		        "Starts a conditional block. If [Symbol] has NOT been defined, the commands in the block are ignored.") {
			@Override
			void handleLine(String line, ToolSet tools) {
				if (!tools.ignore) {
					final String[] arguments = line.split(" ");

					Token.checkArgCount(arguments, 2, this, line);

					tools.ignore = !tools.symbols.contains(arguments[1]);
				}
			}
		},

		/** Token for #ifndef preprocessor command */
		IF_NOT_DEFINED("#ifndef", "%s [Symbol]",
		        "Starts a conditional block. If [Symbol] HAS been defined, the commands in the block are ignored.") {
			@Override
			void handleLine(String line, ToolSet tools) {
				if (!tools.ignore) {
					final String[] arguments = line.split(" ");

					Token.checkArgCount(arguments, 2, this, line);

					tools.ignore = tools.symbols.contains(arguments[1]);
				}
			}
		},

		/** Token for #endif preprocessor command */
		END_IF("#endif", "%s", "Ends the conditional block.") {
			@Override
			void handleLine(String line, ToolSet tools) {
				tools.ignore = false;
			}
		},

		/** Token for #define preprocessor command */
		PLACE_SYMBOL("#define", "%s [Symbol]",
		        "Defines a new symbol that is used to form conditional blocks. See %s and %s",
		        IF_DEFINED.identifier, IF_NOT_DEFINED.identifier) {
			@Override
			void handleLine(String line, ToolSet tools) {
				if (!tools.ignore) {
					final String[] arguments = line.split(" ");

					Token.checkArgCount(arguments, 2, this, line);

					tools.symbols.add(arguments[1]);
				}
			}
		};

		private final String identifier, syntax, description;

		/**
		 * Constructor that initializes a Preprocessor command.
		 *
		 * @param identifier  the identifier of the command
		 * @param syntax      the syntax of the command
		 * @param description the description of the command. The description will be
		 *                    formated with the {@code args} by calling
		 *                    {@code String.format(description, args)}.
		 * @param args        format arguments for the description
		 */
		Token(String identifier, String syntax, String description, Object... args) {
			this.identifier = identifier;
			this.syntax = syntax;
			this.description = String.format(description, args);
		}

		/**
		 * Handles the preprocessing of a line of code.
		 *
		 * @param line the line
		 */
		abstract void handleLine(String line, ToolSet tools);

		@Override
		public String toString() {
			final String commandSyntax = String.format(syntax, identifier);
			return String.format("%s\n\t%s", commandSyntax, description);
		}

		private static Token getByIdentifier(String identifier) {
			for (final Token t : Token.values())
				if (t.identifier.equals(identifier))
					return t;
			return DEFAULT;
		}

		private static void checkArgCount(String[] args, int count, Token token, String line) {
			if (args.length != count)
				throw new SyntaxException(
				        errorMessage("Invalid number of arguments in " + token.identifier, line));
		}
	}

	/** The regex defining a comment as recognized by the preprocessor */
	public static final String COMMENT_REGEX = "/\\*(.|\\s)*?\\*/";

	/**
	 * Returns a string of the text formatted into interpreter-readable code. ; *
	 *
	 * @param code the original text block
	 *
	 * @return a String as executable code
	 *
	 * @throws SyntaxException if invalid syntax among the preprocessor commands is
	 *                         detected
	 */
	static String process(String code) throws SyntaxException {
		ToolSet tools = new ToolSet();

		final String   bareCode = code.replaceAll(COMMENT_REGEX, "").replaceAll(";+", ";");
		final String[] lines    = AutomatonInterpreter.splitCommands(bareCode);

		for (String line : lines) {
			line = line.strip();
			final String command = line.split(" ")[0].toLowerCase();
			Token.getByIdentifier(command).handleLine(line, tools);
		}

		if (tools.ignore)
			throw new SyntaxException(
			        String.format("Expected %s before end of file as a conditional block is open.",
			                Token.END_IF.identifier));

		return tools.codeBuilder.toString();
	}

	/**
	 * Returns a array containing the identifier of every command used by the
	 * Preprocessor.
	 *
	 * @return the array
	 */
	public static String[] getCommands() {
		final Token[]  tokens   = Token.values();
		final String[] commands = new String[tokens.length - 1];

		int i = 0;
		for (final Token token : tokens)
			if (token != Token.DEFAULT)
				commands[i++] = token.identifier;
		return commands;
	}

	/**
	 * Returns the description of the Preprocessor's command that is identified by
	 * the {@code commandName}.
	 *
	 * @param commandName the identifier of the Preprocessor command
	 *
	 * @return the description of the command
	 */
	public static String getCommandDescription(String commandName) {
		for (final Token t : Token.values())
			if (t.identifier.equals(commandName))
				return t.toString();

		throw new IllegalArgumentException(
		        "Cannot get description: " + commandName + " is not a valid command");
	}

	private static String errorMessage(String message, String command) {
		return String.format("%s\n\t at command %s", message, command);
	}
	
	/**
	 * Creates a set of all data the function {@link Preprocessor#process(String)} saves during execution.
	 * Used to pass said data around other functions until they are extracted by the {@link Preprocessor#process(String)} method.
	 */
	private static class ToolSet {
		final HashMap<String, String> namedefs    = new HashMap<>();
		final HashSet<String>         symbols     = new HashSet<>();
		final StringBuilder           codeBuilder = new StringBuilder();
		boolean                       ignore      = false;
	}
}
