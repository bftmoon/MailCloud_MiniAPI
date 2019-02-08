package com.nodo.mailcloud_miniapi;

/**
 * Cloud Exceptions handler. Contains type, message and code of error.
 * Types:
 * FileNotFound
 * AccessDenied - Wrong login or password or token recreation required (unlikely)
 * ServerError - Cloud error. Nothing to do
 * ConnectionError - no internet or bad signal
 * Unknown - other types of error. Can be checked by code
 */
public class MailCloudException extends Exception {

    private final Type type;
    private int code;
    MailCloudException(Type type, String message) {
        super("Type: " + type.name() + ". " + message);
        this.type = type;
    }

    MailCloudException(Type type, int code, String message) {
        super("Type: " + type.name() + ", code: " + code + ". " + message);
        this.type = type;
        this.code = code;
    }

    static void checkExceptionForCode(int code, String message) throws MailCloudException {
        if (code >= 200 && code <= 300) return;
        switch (code) {
            case -1:  // Unique for that case
                throw new MailCloudException(Type.ConnectionError, message);
            case 404:
                throw new MailCloudException(Type.FileNotFound, message);
            case 403:
                throw new MailCloudException(Type.AccessDenied, message);
            case 400:
                throw new MailCloudException(Type.FileNotFound, message);
            case 500:
                throw new MailCloudException(Type.ServerError, message);
            default:
                throw new MailCloudException(Type.Unknown, code, message);
        }
    }

    public Type getType() {
        return type;
    }

    public int getCode() {
        return code;
    }

    public enum Type {FileNotFound, AccessDenied, ServerError, ConnectionError, Unknown}
}


