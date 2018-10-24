package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.valashko.xaapi.XaapiException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class XiaomiMotionSensor extends SlaveDevice implements IInteractiveDevice {

    public enum Action {
        Motion
    }

    private Action lastAction;
    private HashMap<SubscriptionToken, Consumer<String>> actionsCallbacks = new HashMap<>();
    private HashMap<SubscriptionToken, Runnable> motionCallbacks = new HashMap<>();

    XiaomiMotionSensor(XiaomiGateway gateway, String sid) {
        super(gateway, sid, Type.XiaomiMotionSensor);
    }

    @Override
    void update(String data) {
        try {
            JsonObject o = JSON_PARSER.parse(data).getAsJsonObject();
            if (o.has("status")) {
                String action = o.get("status").getAsString();
                switch(action) {
                    case "motion":
                        lastAction = Action.Motion;
                        notifyWithMotion();
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

    public SubscriptionToken subscribeForMotion(Runnable callback) {
        SubscriptionToken token = new SubscriptionToken();
        motionCallbacks.put(token, callback);
        return token;
    }

    public void unsubscribeForMotion(SubscriptionToken token) {
        motionCallbacks.remove(token);
    }

    private void notifyWithMotion() {
        for(Runnable r : motionCallbacks.values()) {
            r.run();
        }
    }
}
