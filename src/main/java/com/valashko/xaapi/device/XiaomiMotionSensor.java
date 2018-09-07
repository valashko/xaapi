package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.valashko.xaapi.XaapiException;

public class XiaomiMotionSensor extends SlaveDevice {

    public enum Status {
        Motion
    }

    private Status lastStatus;

    public XiaomiMotionSensor(XiaomiGateway gateway, String sid) {
        super(gateway, sid, Type.XiaomiMotionSensor);
    }

    @Override
    void update(String data) {
        try {
            JsonObject o = JSON_PARSER.parse(data).getAsJsonObject();
            if (o.has("status")) {
                String status = o.get("status").getAsString();
                switch(status) {
                    case "motion":
                        lastStatus = Status.Motion;
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
}
