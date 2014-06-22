package com.texttalk.commons.command;

import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Calendar;

/**
 * Created by Andrew on 15/06/2014.
 */
public class CommandExecutor {

    private final static Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private int timeoutSecs = 10;

    private Integer expectedExitValue = null;

    private String executeAfterExceptionMainCommand = null;

    private InputStream stdin = null;

    private OutputStream stdout = null;

    public OutputStream getErrorStream() {
        return error;
    }

    public void setErrorStream(OutputStream error) {
        this.error = error;
    }

    private OutputStream error = null;

    public Integer getExpectedExitValue() {
        return expectedExitValue;
    }

    public void setExpectedExitValue(Integer expectedExitValue) {
        this.expectedExitValue = expectedExitValue;
    }

    public String getExecuteAfterExceptionCommand() {
        return executeAfterExceptionMainCommand;
    }

    public void setExecuteAfterExceptionCommand(String executeAfterExceptionMainCommand) {
        this.executeAfterExceptionMainCommand = executeAfterExceptionMainCommand;
    }

    public int getTimeoutSecs() {
        return timeoutSecs;
    }

    public void setTimeoutSecs(int timeoutSecs) {
        this.timeoutSecs = timeoutSecs;
    }

    public CommandExecutor setInputStream(InputStream in) {
        stdin = in;
        return this;
    }

    public CommandExecutor setOutputStream(OutputStream out) {
        stdout = out;
        return this;
    }

    public InputStream getInputStream() {
        return stdin;
    }

    public OutputStream getOutputStream() {
        return stdout;
    }

    private void resetInputStream() {
        stdin = null;
    }

    private void resetOutputStream() {
        stdin = null;
    }

    private void resetErrorStream() {
        error = null;
    }

    public CommandExecutor execute(String executeMainCommandIn) throws CommandException {

        return execute(executeMainCommandIn, timeoutSecs, expectedExitValue, executeAfterExceptionMainCommand);
    }

    public CommandExecutor execute(String executeMainCommandIn, Integer timeout) throws CommandException {

        return execute(executeMainCommandIn, timeout, expectedExitValue, executeAfterExceptionMainCommand);
    }

    public CommandExecutor execute(String executeMainCommandIn, Integer timeout, Integer expectedExitValue) throws CommandException {

        return execute(executeMainCommandIn, timeout, expectedExitValue, executeAfterExceptionMainCommand);
    }

    public CommandExecutor execute(String executeMainCommandIn, Integer timeout, Integer expectedExitValue, String executeAfterExceptionMainCommand) throws CommandException {

        ExecuteWatchdog watchdog = null;

        try {
            CommandLine commandLine = CommandLine.parse(executeMainCommandIn);
            DefaultExecutor executor = new DefaultExecutor();

            if(expectedExitValue != null) {
                executor.setExitValue(expectedExitValue);
            }

            if(error == null) {
                error = new ByteArrayOutputStream();
            }

            executor.setStreamHandler(new PumpStreamHandler(stdout, error, stdin));

            watchdog = new CommandWatchdog(timeout, executeAfterExceptionMainCommand);
            executor.setWatchdog(watchdog);
            executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

            Calendar startTime = Calendar.getInstance();

            executor.execute(commandLine);

            Calendar nowTime = Calendar.getInstance();
            Long commandTimeDiff = (nowTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000;

            logger.debug("It took {} seconds to perform the command", commandTimeDiff.toString());

        } catch (CommandException e) {
            logger.error("Command: " + executeMainCommandIn);
            logger.error("Error: " + error.toString());
            throw e;
        } catch (CommandTimeoutException e) {
            logger.error("Command: " + executeMainCommandIn);
            logger.error("Error: " + error.toString());
            throw e;
        } catch (Exception e) {
            logger.error("Command: " + executeMainCommandIn);
            logger.error("Error: " + error.toString());
            // check if the cause of this exception was because of time out or watchdog killed the actual command
            if(watchdog != null) {
                try {
                    watchdog.checkException();
                } catch(Exception wExp) {
                    throw new CommandTimeoutException(e);
                }
            }
            throw new CommandException(e);
        } finally {
            resetInputStream();
        }

        return this;
    }
}