package org.babyfishdemo.war3shop.db.installer.shell;

/**
 * @author Tao Chen
 */
public class ShellException extends RuntimeException {

	private static final long serialVersionUID = -7542977413158477448L;

	public ShellException() {
		super();
	}

	public ShellException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShellException(String message) {
		super(message);
	}

	public ShellException(Throwable cause) {
		super(cause);
	}
}
