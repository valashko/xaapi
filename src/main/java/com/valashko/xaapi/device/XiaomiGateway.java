package com.valashko.xaapi.device;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.valashko.xaapi.XaapiException;
import com.valashko.xaapi.channel.*;
import com.valashko.xaapi.command.*;
import com.valashko.xaapi.reply.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

public class XiaomiGateway {

    private static final String GROUP = "224.0.0.50";
    private static final int PORT = 9898;
    private static final int PORT_DISCOVERY = 4321;
    private static final byte[] IV =
        {     0x17, (byte)0x99, 0x6d, 0x09, 0x3d, 0x28, (byte)0xdd, (byte)0xb3,
        (byte)0xba,       0x69, 0x5a, 0x2e, 0x6f, 0x58,       0x56,       0x2e};

    private static final Gson GSON = new Gson();

    private String sid;
    private Optional<String> key = Optional.empty();
    private Cipher cipher;
    private IncomingMulticastChannel incomingMulticastChannel;
    private DirectChannel directChannel;
    private XiaomiGatewayLight builtinLight;
    private XiaomiGatewayIlluminationSensor builtinIlluminationSensor;
    private Map<String, SlaveDevice> knownDevices = new HashMap<>();
    private boolean continueReceivingUpdates;

    public static XiaomiGateway discover() throws IOException, XaapiException {
        // TODO discover more than one gateway
        DirectChannel discoveryChannel = new DirectChannel(GROUP, PORT_DISCOVERY);
        discoveryChannel.send(new WhoisCommand().toBytes());
        String replyString = new String(discoveryChannel.receive());
        WhoisReply reply = GSON.fromJson(replyString, WhoisReply.class);
        if(Integer.parseInt(reply.port) != PORT) {
            throw new XaapiException("Gateway occupies unexpected port: " + reply.port);
        }
        return new XiaomiGateway(reply.ip);
    }

    public XiaomiGateway(String ip) throws IOException, XaapiException {
        this.incomingMulticastChannel = new IncomingMulticastChannel(GROUP, PORT);
        this.directChannel = new DirectChannel(ip, PORT);
        queryDevices();
        configureBuiltinDevices();
    }
    public XiaomiGateway(String ip, String password) throws IOException, XaapiException {
        this(ip);
        configureCipher(password);
    }

    public void configurePassword(String password) throws XaapiException {
        configureCipher(password);
    }

    public Map<String, SlaveDevice> getKnownDevices() {
        return knownDevices;
    }

    private void configureBuiltinDevices() {
        builtinLight = new XiaomiGatewayLight(sid);
        builtinIlluminationSensor = new XiaomiGatewayIlluminationSensor(sid);
    }

    private void configureCipher(String password) throws XaapiException {
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
            final SecretKeySpec keySpec = new SecretKeySpec(password.getBytes(), "AES");
            final IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        } catch (NoSuchAlgorithmException e) {
            throw new XaapiException("Cipher error: " + e.getMessage());
        } catch (NoSuchPaddingException e) {
            throw new XaapiException("Cipher error: " + e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            throw new XaapiException("Cipher error: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new XaapiException("Cipher error: " + e.getMessage());
        }
    }

    private void queryDevices() throws XaapiException {
        try {
            directChannel.send(new GetIdListCommand().toBytes());
            String replyString = new String(directChannel.receive());
            GetIdListReply reply = GSON.fromJson(replyString, GetIdListReply.class);
            sid = reply.sid;
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

    public String getSid() {
        return sid;
    }

    public XiaomiGatewayLight getBuiltinLight() {
        return builtinLight;
    }

    public XiaomiGatewayIlluminationSensor getBuiltinIlluminationSensor() {
        return builtinIlluminationSensor;
    }

    private boolean isMyself(String sid) {
        return sid.equals(this.sid);
    }

    private void updateKey(String token) throws XaapiException {
        if(cipher != null) {
            try {
                String keyAsHexString = Utility.toHexString(cipher.doFinal(token.getBytes(StandardCharsets.US_ASCII)));
                key = Optional.of(keyAsHexString);
            } catch (IllegalBlockSizeException e) {
                throw new XaapiException("Cipher error: " + e.getMessage());
            } catch (BadPaddingException e) {
                throw new XaapiException("Cipher error: " + e.getMessage());
            }
        } else {
            throw new XaapiException("Unable to update key without a cipher. Did you forget to set a password?");
        }
    }

    void sendDataToDevice(SlaveDevice device, JsonObject data) throws XaapiException {
        if(key.isPresent()) {
            try {
                directChannel.send(new WriteCommand(device, data, key.get()).toBytes());
                // TODO add handling for expired key
            } catch (IOException e) {
                throw new XaapiException("Network error: " + e.getMessage());
            }
        } else {
            throw new XaapiException("Unable to control device without a key. Did you forget to set a password?");
        }
    }

    private SlaveDevice readDevice(String sid) throws XaapiException {
        try {
            directChannel.send(new ReadCommand(sid).toBytes());
            String replyString = new String(directChannel.receive());
            ReadReply reply = GSON.fromJson(replyString, ReadReply.class);

            switch(reply.model) {
                case "cube":
                    XiaomiCube cube = new XiaomiCube(this, sid);
                    cube.update(reply.data);
                    return cube;
                case "magnet":
                    XiaomiDoorWindowSensor magnet = new XiaomiDoorWindowSensor(this, sid);
                    magnet.update(reply.data);
                    return magnet;
                case "plug":
                    XiaomiSocket plug = new XiaomiSocket(this, sid);
                    plug.update(reply.data);
                    return plug;
                case "motion":
                    XiaomiMotionSensor motion = new XiaomiMotionSensor(this, sid);
                    motion.update(reply.data);
                    return motion;
                case "switch":
                    XiaomiSwitchButton button = new XiaomiSwitchButton(this, sid);
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
                    String received = new String(incomingMulticastChannel.receive());
                    handleUpdate(GSON.fromJson(received, ReadReply.class), received);
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

    private void handleUpdate(Reply update, String received) throws XaapiException {
        switch(update.cmd) {
            case "report":
                Report report = GSON.fromJson(received, Report.class);
                if(isMyself(update.sid)) {
                    handleBuiltinReport(report);
                } else {
                    handleReport(report);
                }
                break;
            case "heartbeat":
                if(isMyself(update.sid)) {
                    GatewayHeartbeat gatewayHeartbeat = GSON.fromJson(received, GatewayHeartbeat.class);
                    handleGatewayHeartbeat(gatewayHeartbeat);
                } else {
                    SlaveDeviceHeartbeat slaveDeviceHeartbeat = GSON.fromJson(received, SlaveDeviceHeartbeat.class);
                    handleSlaveDeviceHeartbeat(slaveDeviceHeartbeat);
                }
                break;
            default:
                throw new XaapiException("Unexpected update command: " + update.cmd);
        }
    }

    private void handleReport(Report report) {
        getDevice(report.sid).update(report.data);
    }

    private void handleBuiltinReport(Report report) {
        builtinLight.update(report.data);
        builtinIlluminationSensor.update(report.data);
    }

    private void handleGatewayHeartbeat(GatewayHeartbeat gatewayHeartbeat) throws XaapiException {
        if(cipher != null) {
            updateKey(gatewayHeartbeat.token);
        }
    }

    private void handleSlaveDeviceHeartbeat(SlaveDeviceHeartbeat slaveDeviceHeartbeat) {
        // TODO implement
    }
}