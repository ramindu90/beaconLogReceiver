package org.wso2.carbon.sample.servicestats;

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.sasl.AuthenticationException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.*;

public class Impression {

    private static int sentEventCount = 0;
    public static final String STREAM_NAME1 = "impressionStream";
    public static final String VERSION1 = "1.0.0";

    private static Log log = LogFactory.getLog(Impression.class);



    public static void main(String[] args)
            throws AgentException, MalformedStreamDefinitionException,
                   StreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedURLException,
                   AuthenticationException, NoStreamDefinitionExistException,
                   org.wso2.carbon.databridge.commons.exception.AuthenticationException,
                   TransportException, SocketException {
        System.out.println("Starting Statistics Agent");

        KeyStoreUtil.setTrustStoreParams();

        String host = "localhost";
        String port = "7611";
        String username = "admin";
        String password = "admin";
//        int events = Integer.parseInt(args[4]);

        //create data publisher
        DataPublisher dataPublisher = new DataPublisher("tcp://" + host + ":" + port, username, password);

        String streamId1;


        streamId1 = dataPublisher.defineStream("{\n" +
                "        'name': 'impressionStream',\n" +
                "        'version': '1.0.0',\n" +
                "        'nickName': '',\n" +
                "        'description': '',\n" +
                "        'metaData': [\n" +
                "          {\n" +
                "            'name': 'eventID',\n" +
                "            'type': 'STRING'\n" +
                "          },\n" +
                "          {\n" +
                "            'name': 'tenantID',\n" +
                "            'type': 'STRING'\n" +
                "          },\n" +
                "          {\n" +
                "            'name': 'time',\n" +
                "            'type': 'LONG'\n" +
                "          },\n" +
                "          {\n" +
                "            'name': 'tz',\n" +
                "            'type': 'INT'\n" +
                "          }\n" +
                "        ],\n" +
                "        'payloadData': [\n" +
                "          {\n" +
                "            'name': 'userID',\n" +
                "            'type': 'STRING'\n" +
                "          },\n" +
                "          {\n" +
                "            'name': 'domainID',\n" +
                "            'type': 'STRING'\n" +
                "          },\n" +
                "          {\n" +
                "            'name': 'creativeID',\n" +
                "            'type': 'STRING'\n" +
                "          }\n" +
                "        ]\n" +
                "      }");

        //Publish event for a valid stream
        if (!streamId1.isEmpty()) {
            System.out.println("Stream ID: " + streamId1);

                publishEvents(dataPublisher, streamId1, 2);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                //ignore
            }

            dataPublisher.stop();
        }
    }

    private static void publishEvents(DataPublisher dataPublisher, String streamId, int eventLimit)
            throws AgentException {

        try {
            Random r = new Random();
            Random r2 = new Random();
            Random r3 = new Random();
            Random r4 = new Random();
            int elapsedCount = 10000;
            long count = 0;
            long lastTime = System.currentTimeMillis();
            DecimalFormat decimalFormat = new DecimalFormat("#");
            while (count < eventLimit) {
                Object[] meta  = new Object[4];
                String tenantId = "t"+(r.nextInt(10));
                String eventId = "e"+count;
                long timestamp = System.currentTimeMillis();
                int tz = 0;

                meta[0] = tenantId;
                meta[1] = eventId;
                meta[2] = timestamp;
                meta[3] = tz;

                Object[] payload = new Object[3];
                String userId = "user"+(r2.nextInt(10000));
                String domainId = "domain"+(r3.nextInt(10000));
                String creativeId = "creative"+(r4.nextInt(1000));

                payload[0] = userId;
                payload[1] = domainId;
                payload[2] = creativeId;

                Event event = new Event(streamId, System.currentTimeMillis(), meta, null,
                        payload);

                dataPublisher.publish(event);

                if (count % elapsedCount == 0) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - lastTime;
                    double throughputPerSecond = (((double) elapsedCount) / elapsedTime) * 1000;
                    lastTime = currentTime;
                    log.info("Sent " + elapsedCount + " sensor events in " + elapsedTime +
                            " milliseconds with total throughput of " + decimalFormat.format(throughputPerSecond) +
                            " events per second.");
                }

                    Thread.sleep(2);

                count++;
            }
            Thread.sleep(2000);
            dataPublisher.stop();
        } catch (InterruptedException e) {
            log.error("Thread interrupted while sleeping between eventsH", e);
        }
    }

}

