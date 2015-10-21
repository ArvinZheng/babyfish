package org.babyfishdemo.war3shop.db.installer.schema;

/**
 * @author Tao Chen
 */
public class SchemaException extends RuntimeException {

	private static final long serialVersionUID = -2218599784889977979L;

	public SchemaException() {
		super();
	}

	public SchemaException(String message, Throwable cause) {
		super(message, cause);
	}

	public SchemaException(String message) {
		super(message);
	}

	public SchemaException(Throwable cause) {
		super(cause);
	}
}
