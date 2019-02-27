package pers.doublebin.utils.springfox.bridge.core.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BridgeException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private String description;

    private int errorCode = 500;

    public BridgeException(String message)
    {
        super(message);
        this.setDescription(message);
    }

    public BridgeException(String message, int errorCode)
    {
        super(message);
        this.setDescription(message);
        this.setErrorCode(errorCode);
    }

    public BridgeException(String message, String description)
    {
        super(message);
        this.setDescription(description);
    }

    public BridgeException(String message, String description, int errorCode)
    {
        super(message);
        this.setDescription(description);
        this.setErrorCode(errorCode);
    }

    public BridgeException(String message, Throwable cause)
    {
        super(message, cause);
        this.setDescription(message);
    }

    public BridgeException(String message, Throwable cause, int errorCode)
    {
        super(message, cause);
        this.setDescription(message);
        this.setErrorCode(errorCode);
    }

    public BridgeException(String message, String description, Throwable cause)
    {
        super(message, cause);
        this.setDescription(description);
    }

    public BridgeException(String message, String description, Throwable cause, int errorCode)
    {
        super(message, cause);
        this.setDescription(description);
        this.setErrorCode(errorCode);
    }

    public BridgeException(Throwable cause)
    {
        super(cause);
    }

    public BridgeException(Throwable cause, int errorCode)
    {
        super(cause);
        this.setErrorCode(errorCode);
    }
}
