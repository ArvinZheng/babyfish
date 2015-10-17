package org.babyfishdemo.war3shop.bll;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public class AuthorizationException extends RuntimeException {

    private static final long serialVersionUID = -2331111173975288556L;

    public AuthorizationException() {
        super();
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(Throwable cause) {
        super(cause);
    }
}
