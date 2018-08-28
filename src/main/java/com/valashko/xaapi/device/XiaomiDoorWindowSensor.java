package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.valashko.xaapi.XaapiException;

public class XiaomiDoorWindowSensor extends SlaveDevice {

    public enum Status {
        Open,
        Close
    }

    private Status lastStatus;

    public XiaomiDoorWindowSensor(String sid) {
        super(sid, Type.XiaomiCube);
    }

    @Override
    void update(String data) {
        try {
            JsonObject o = JSON_PARSER.parse(data).getAsJsonObject();
            if (o.has("status")) {
                String status = o.get("status").getAsString();
                switch(status) {
                    case "open":
                        lastStatus = Status.Open;
                        break;
                    case "close":
                        lastStatus = Status.Close;
                        break;
                    default:
                        throw new XaapiException("Unexpected status: " + status);
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
