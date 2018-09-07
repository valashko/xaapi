package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.valashko.xaapi.XaapiException;

public class XiaomiSocket extends SlaveDevice {

    public enum Status {
        On,
        Off,
        Unknown // probably device is offline
    }

    private Status lastStatus;

    public XiaomiSocket(XiaomiGateway gateway, String sid) {
        super(gateway, sid, Type.XiaomiSocket);
    }

    @Override
    void update(String data) {
        try {
            JsonObject o = JSON_PARSER.parse(data).getAsJsonObject();
            if (o.has("status")) {
                String status = o.get("status").getAsString();
                switch(status) {
                    case "on":
                        lastStatus = Status.On;
                        break;
                    case "off":
                        lastStatus = Status.Off;
                        break;
                    case "unknown":
                        lastStatus = Status.Unknown;
                        break;
                    default:
                        throw new XaapiException("Unknown status: " + status);
                }
            }
        } catch (XaapiException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public Status getLastStatus() {
        return lastStatus;
    }

    public void turnOn() throws XaapiException {
        JsonObject on = new JsonObject();
        on.addProperty("status", "on");
        gateway.sendDataToDevice(this, on);
    }

    public void turnOff() throws XaapiException {
        JsonObject off = new JsonObject();
        off.addProperty("status", "off");
        gateway.sendDataToDevice(this, off);
    }
}
