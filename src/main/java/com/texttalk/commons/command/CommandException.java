package com.texttalk.commons.command;

/**
 * Created by Andrew on 16/06/2014.
 */
public class CommandException extends RuntimeException{

    public CommandException() { super(); }
    public CommandException(String message) { super(message); }
    public CommandException(String message, Throwable cause) { super(message, cause); }
    public CommandException(Throwable cause) { super(cause); }
}
