package com.texttalk.core.encoders;

import com.google.common.io.ByteStreams;
import com.texttalk.commons.command.CommandExecutor;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MP3LameEncoder extends EncoderBase implements Encoder {

    private String lameEncoderCmd = "lame -r -m m --resample 22 -b 32";
    private CommandExecutor cmd = new CommandExecutor();

    public CommandExecutor getCmd() {
        return cmd;
    }

    public MP3LameEncoder setCmd(CommandExecutor cmd) {
        this.cmd = cmd;
        return this;
    }

    public MP3LameEncoder setLameEncoderCmd(String cmd) {
        lameEncoderCmd = cmd;
        return this;
    }

    public String getLameEncoderCmd() {
        return lameEncoderCmd;
    }

    @Override
    public MP3LameEncoder process() throws IOException {

        String input = "-";
        String output = "-";

        if(isInputFileSet()) {
            input = inputFile.getAbsolutePath();
        }

        if(isOutputFileSet()) {
            output = outputFile.getAbsolutePath();
        }

        if(isInputStreamSet()) {
            cmd.setInputStream(inputStream);
        }

        if(isOutputStreamSet()) {
            cmd.setOutputStream(outputStream);
        }

        cmd.execute(lameEncoderCmd + " " + input + " " + output);

        return this;
    }
}