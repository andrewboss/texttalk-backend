package com.texttalk.distrib.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.texttalk.common.Utils;
import com.texttalk.common.command.CommandExecutor;
import com.texttalk.common.model.Message;
import com.texttalk.core.transcriber.PSOLATranscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Created by Andrew on 16/06/2014.
 */
public class TextTranscriptionBolt extends BaseRichBolt {

    private static Logger logger = LoggerFactory.getLogger(TextTranscriptionBolt.class);

    private OutputCollector collector;
    private String transcriberPath = "";
    private Integer timeout = 0;

    public void prepare(Map config, TopologyContext context, OutputCollector collector) {

        this.collector = collector;
        transcriberPath = (String)config.get("transcribers.psola.execPath");
        timeout = ((Long)config.get("transcribers.psola.timeout")).intValue();
    }

    public void execute(Tuple tuple) {

        Message msg = Message.getMessage(tuple.getStringByField("textChunk"));
        String textChunk = msg.getText();
        String inputText = Utils.convertFromUTF8(textChunk, "Windows-1257");
        String transcribedText = "";

        logger.info("Running Transcription bolt...");

        try {

            ByteArrayInputStream in = new ByteArrayInputStream(inputText.getBytes("Windows-1257"));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream error = new ByteArrayOutputStream();

            new PSOLATranscriber()
                    .setSpeed(Integer.parseInt(msg.getSpeed()))
                    .setTone(Integer.parseInt(msg.getTone()))
                    .setCmd(new CommandExecutor().setTimeoutSecs(timeout).setErrorStream(error))
                    .setPSOLATranscribeCmd(transcriberPath)
                    .setInputStream(in)
                    .setOutputStream(out)

                    .process();

            transcribedText = out.toString("UTF-8");

        } catch(Exception e) {
            // TODO: deal with exception
            e.printStackTrace();
        }

        msg.setTranscript(transcribedText);

        this.collector.emit(msg.getSynth(), new Values(Message.getJSON(msg)));
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream("psola", new Fields("transcribedText"));
    }
}
