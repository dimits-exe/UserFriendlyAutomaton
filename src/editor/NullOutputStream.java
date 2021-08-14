package editor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that discards all output. Used to discard 
 * output messages in a safe non-nullable way.
 * 
 * @author dimits
 */
public final class NullOutputStream extends OutputStream {

	@Override
	public void write(int b) throws IOException {
		return;
	}
}
