package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.valashko.xaapi.XaapiException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class XiaomiSocket extends SlaveDevice implements IInteractiveDevice {

    public enum Action {
        On,
        Off,
        Unknown // probably device is offline
    }

    private Action lastAction;
    private HashMap<SubscriptionToken, Consumer<String>> actionsCallbacks = new HashMap<>();

    XiaomiSocket(XiaomiGateway gateway, String sid) {
        super(gateway, sid, Type.XiaomiSocket);
    }

    @Override
    void update(String data) {
        try {
            JsonObject o = JSON_PARSER.parse(data).getAsJsonObject();
            if (o.has("status")) {
                String action = o.get("status").getAsString();
                switch(action) {
                    case "on":
                        lastAction = Action.On;
                        break;
                    case "off":
                        lastAction = Action.Off;
                        break;
                    case "unknown":
                        lastAction = Action.Unknown;
                        break;
                    default:
                        throw new XaapiException("Unknown action: " + action);
                }
                notifyWithAction(action);
            }
        } catch (XaapiException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<SubscriptionToken, Consumer<String>> getActionsCallbacks() {
        return actionsCallbacks;
    }

    public Action getLastAction() {
        return lastAction;
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
