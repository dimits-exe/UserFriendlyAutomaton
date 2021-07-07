# User Friendly Automaton
A project containing data structures for different kinds of automata (DFAs/NFAs), a simple custom language defining operations on them, as well as a fully functional and customizable GUI editor.

![Editor sample](https://media.discordapp.net/attachments/354913879471423492/862292105744875560/editor_display.gif?width=701&height=670)

## Automaton support
Includes high-level implemenation of [DFAs](https://en.wikipedia.org/wiki/Deterministic_finite_automaton) and [NFAs](https://en.wikipedia.org/wiki/Nondeterministic_finite_automaton) but supports easy extensions for other similar theoretical machines, such as [PushDownAutomata](https://en.wikipedia.org/wiki/Pushdown_automaton) or even [Turing Machines](https://en.wikipedia.org/wiki/Turing_machine). Automata are represented as a set of connected nodes (graphs), as in most diagrams showcasing them.

## Custom language support
Includes a basic language defining operations to build, debug, execute and save automata to a file. The language is interpreted with a custom interpreter that can read and execute code either from the console, or from a file. The interpreter handles all syntax and execution errors, printing helpful error messages to the user and preventing the program from crashing at any point. 

The interpreter comes with a preprocessor that extends the language to include if-statements, comments and name definitions in order to give the user more control over his program.

## GUI editor
A windowed application whose main purpose is to enhance user experience while writing and executing code for the interpreter. 

Features:
1. A text area the user can use to write his program, as well as a console to print any output/error messages, distinguished by color.
2. Syntax highlighting.
3. Customization options including different look and feel options, output colors and text color/size/font/style.
4. Standard user services including redo/undo commands, save options and hotkeys.
5. Background real-time error checking.
6. Help tabs providing information about the way the language works.

The application is multithreaded and can handle large workloads.

Special thanks to Rob Camick. The editor is implemented with the (now mostly defunct) Java swing library which leaves a lot to be desired in many usual GUI problems. Camick has openly provided tested, easy to understand classes that solve many of these problems, some of which are present in this project.

