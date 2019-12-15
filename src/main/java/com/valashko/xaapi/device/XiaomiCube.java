package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.valashko.xaapi.XaapiException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class XiaomiCube extends SlaveDevice implements IInteractiveDevice {

    public enum Action {
        Flip90,
        Flip180,
        Move,
        TapTwice,
        Shake,
        Swing,
        Alert,
        FreeFall,
        Rotate
    }

    private int charge;
    private Action lastAction;
    private Optional<Double> lastRotationAngle = Optional.empty();
    private HashMap<SubscriptionToken, Consumer<String>> actionsCallbacks = new HashMap<>();
    private HashMap<SubscriptionToken, Consumer<Double>> rotationCallbacks = new HashMap<>();

    public XiaomiCube(XiaomiGateway gateway, String sid) {
        super(gateway, sid, Type.XiaomiCube);
    }

    @Override
    void update(String data) {
        try {
            JsonObject o = JSON_PARSER.parse(data).getAsJsonObject();
            if (o.has("status")) {
                updateWithAction(o.get("status").getAsString());
                resetLastRotationValue();
            }
            if (o.has("rotate")) {
                String angle = o.get("rotate").getAsString().replace(',', '.'); // for some reason they use comma as decimal point
                updateWithRotation(Double.parseDouble(angle));
            }
        } catch(XaapiException | JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<SubscriptionToken, Consumer<String>> getActionsCallbacks() {
        return actionsCallbacks;
    }

    // battery charge in percent between 0 and 100
    public int getCharge() {
        return charge;
    }

    public Action getLastAction() {
        return lastAction;
    }

    public double getLastRotationAngle() throws XaapiException {
        if(lastRotationAngle.isPresent()) {
            return lastRotationAngle.get();
        } else {
            throw new XaapiException("Last rotation value does not exist");
        }
    }

    public SubscriptionToken subscribeForRotation(Consumer<Double> callback) {
        SubscriptionToken token = new SubscriptionToken();
        rotationCallbacks.put(token, callback);
        return token;
    }

    public void unsubscribeForRotation(SubscriptionToken token) {
        rotationCallbacks.remove(token);
    }

    private void updateWithAction(String action) throws XaapiException {
        switch(action) {
            case "flip90":
                lastAction = Action.Flip90;
                break;
            case "flip180":
                lastAction = Action.Flip180;
                break;
            case "move":
                lastAction = Action.Move;
                break;
            case "tap_twice":
                lastAction = Action.TapTwice;
                break;
            case "shake_air":
                lastAction = Action.Shake;
                break;
            case "swing":
                lastAction = Action.Swing;
                break;
            case "alert":
                lastAction = Action.Alert;
                break;
            case "free_fall":
                lastAction = Action.FreeFall;
                break;
            default:
                throw new XaapiException("Unknown action: " + action);
        }
        notifyWithAction(action);
    }

    private void updateWithRotation(double value) {
        lastAction = Action.Rotate;
        lastRotationAngle = Optional.of(value);
        notifyWithAction("rotate");
        notifyWithRotation(value);
    }

    private void resetLastRotationValue() {
        lastRotationAngle = Optional.empty();
    }

    private void notifyWithRotation(double value) {
        for(Consumer<Double> c : rotationCallbacks.values()) {
            c.accept(value);
        }
    }
}
