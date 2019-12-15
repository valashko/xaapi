package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.valashko.xaapi.XaapiException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class XiaomiDoorWindowSensor extends SlaveDevice implements IInteractiveDevice {

    public enum Action {
        Open,
        Close
    }

    private Action lastAction;
    private HashMap<SubscriptionToken, Consumer<String>> actionsCallbacks = new HashMap<>();

    XiaomiDoorWindowSensor(XiaomiGateway gateway, String sid) {
        super(gateway, sid, Type.XiaomiDoorWindowSensor);
    }

    @Override
    void update(String data) {
        try {
            JsonObject o = JSON_PARSER.parse(data).getAsJsonObject();
            if (o.has("status")) {
                String action = o.get("status").getAsString();
                switch(action) {
                    case "open":
                        lastAction = Action.Open;
                        break;
                    case "close":
                        lastAction = Action.Close;
                        break;
                    default:
                        throw new XaapiException("Unexpected action: " + action);
                }
                notifyWithAction(action);
            }
        } catch (XaapiException | JsonSyntaxException e) {
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
}
