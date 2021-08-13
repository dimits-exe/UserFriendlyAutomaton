package editor;

import javax.swing.JFrame;
import interpreter.InterpreterInterface;

public class Application {

	public static void main(String[] args) {
		Editor editor = new Editor("Automaton Editor v1", new InterpreterInterface());
		editor.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		editor.setSize(800,700);
		editor.setVisible(true);
		editor.setResizable(true);
	}
}
