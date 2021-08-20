package com.video.transcoder.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.video.transcoder.dto.TranscodeDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Path("/transcode")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TranscodeResource {
    private final String MEDIA_DIRECTORY = "/home/mrl/media/";

    @GET
    public TranscodeDTO get() {
        return new TranscodeDTO();
    }

    @POST
    public Response createTranscodeJob(TranscodeDTO job) throws IOException {
        String mqHost = System.getenv("mqhost");
        String transcodeJobQueue = System.getenv("transcodejobqueue");
        ConnectionFactory mqConnectionFactory = new ConnectionFactory();
        mqConnectionFactory.setHost(mqHost);
        mqConnectionFactory.setPort(5672);
        Connection mqConnection = null;
        try {
            mqConnection = mqConnectionFactory.newConnection();
            Channel channel = mqConnection.createChannel();
            channel.exchangeDeclare("exc", "direct", true);
            channel.queueBind(transcodeJobQueue, "exc", "black");
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(job);
            System.out.println(jsonStr);
            byte[] messageBodyBytes = (jsonStr + System.currentTimeMillis()).getBytes();

            AMQP.BasicProperties amqproperties = new AMQP.BasicProperties();
            channel.basicPublish("exc", "black", amqproperties, messageBodyBytes);
            channel.close();
            mqConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }
}