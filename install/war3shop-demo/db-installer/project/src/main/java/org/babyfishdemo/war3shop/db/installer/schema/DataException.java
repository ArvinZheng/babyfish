package org.babyfishdemo.war3shop.db.installer.schema;

/**
 * @author Tao Chen
 */
public class DataException extends RuntimeException {

	private static final long serialVersionUID = 254559256485476426L;

	public DataException() {
		super();
	}

	public DataException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataException(String message) {
		super(message);
	}

	public DataException(Throwable cause) {
		super(cause);
	}
}
