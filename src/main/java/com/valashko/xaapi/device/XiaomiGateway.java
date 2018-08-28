package com.valashko.xaapi.device;

import com.google.gson.Gson;
import com.valashko.xaapi.XaapiException;
import com.valashko.xaapi.channel.DirectChannel;
import com.valashko.xaapi.channel.IncomingMulticastChannel;
import com.valashko.xaapi.command.GetIdListCommand;
import com.valashko.xaapi.command.ReadCommand;
import com.valashko.xaapi.reply.GetIdListReply;
import com.valashko.xaapi.reply.ReadReply;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class XiaomiGateway {

    private static final String GROUP = "224.0.0.50";
    private static final int PORT = 9898;
    private static final int PORT_DISCOVERY = 4321;

    private static final Gson GSON = new Gson();

    private IncomingMulticastChannel incomingMulticastChannel;
    private DirectChannel directChannel;
    private Map<String, SlaveDevice> knownDevices = new HashMap<>();
    private boolean continueReceivingUpdates;

    public static XiaomiGateway discover() {
        throw new NotImplementedException();
    }

    public XiaomiGateway(String ip) throws IOException, XaapiException {
        this.incomingMulticastChannel = new IncomingMulticastChannel(GROUP, PORT);
        this.directChannel = new DirectChannel(ip, PORT);
        queryDevices();
    }

    private void queryDevices() throws XaapiException {
        try {
            directChannel.send(new GetIdListCommand().toBytes());
            String replyString = new String(directChannel.receive());
            GetIdListReply reply = GSON.fromJson(replyString, GetIdListReply.class);
            for(String sid : GSON.fromJson(reply.data, String[].class)) {
                knownDevices.put(sid, readDevice(sid));
            }
        } catch (IOException e) {
            throw new XaapiException("Unable to query devices: " + e.getMessage());
        }
    }

    public SlaveDevice getDevice(String sid) {
        SlaveDevice device = knownDevices.get(sid);
        assert(device.getSid().equals(sid));
        return device;
    }

    private SlaveDevice readDevice(String sid) throws XaapiException {
        try {
            directChannel.send(new ReadCommand(sid).toBytes());
            String replyString = new String(directChannel.receive());
            ReadReply reply = GSON.fromJson(replyString, ReadReply.class);

            switch(reply.model) {
                case "cube":
                    XiaomiCube cube = new XiaomiCube(sid);
                    cube.update(reply.data);
                    return cube;
                case "magnet":
                    XiaomiDoorWindowSensor magnet = new XiaomiDoorWindowSensor(sid);
                    magnet.update(reply.data);
                    return magnet;
                case "plug":
                    XiaomiSocket plug = new XiaomiSocket(sid);
                    plug.update(reply.data);
                    return plug;
                case "motion":
                    XiaomiMotionSensor motion = new XiaomiMotionSensor(sid);
                    motion.update(reply.data);
                    return motion;
                case "switch":
                    XiaomiSwitchButton button = new XiaomiSwitchButton(sid);
                    button.update(reply.data);
                    return button;
                default:
                    throw new XaapiException("Unsupported device model: " + reply.model);
            }
        } catch (IOException e) {
            throw new XaapiException("Unable to query device " + sid + ": " + e.getMessage());
        }
    }

    public void startReceivingUpdates(Executor executor) {
        continueReceivingUpdates = true;
        executor.execute(() -> {
            while (continueReceivingUpdates) {
                try {
                    handleUpdate(GSON.fromJson(new String(incomingMulticastChannel.receive()), ReadReply.class));
                } catch (SocketTimeoutException e) {
                    // ignore
                } catch (IOException e) {
                    e.printStackTrace();
                    continueReceivingUpdates = false;
                } catch (XaapiException e) {
                    e.printStackTrace();
                    continueReceivingUpdates = false;
                }
            }
        });
    }

    public void stopReceivingUpdates() {
        continueReceivingUpdates = false;
    }

    private void handleUpdate(ReadReply update) throws XaapiException {
        switch(update.cmd) {
            case "report":
                handleReport(update);
                break;
            case "heartbeat":
                handleHeartbeat(update);
                break;
            default:
                throw new XaapiException("Unexpected update command: " + update.cmd);
        }
    }

    private void handleReport(ReadReply update) {
        getDevice(update.sid).update(update.data);
    }

    private void handleHeartbeat(ReadReply update) {
        // TODO implement
    }
}