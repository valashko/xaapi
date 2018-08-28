package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.valashko.xaapi.XaapiException;

import java.util.ArrayList;
import java.util.Collection;
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
    private ArrayList<Consumer<String>> actionsCallbacks = new ArrayList<>();
    private ArrayList<Consumer<Double>> rotationCallbacks = new ArrayList<>();

    public XiaomiCube(String sid) {
        super(sid, Type.XiaomiCube);
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
        } catch(XaapiException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<Consumer<String>> getActionsCallbacks() {
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

    public void subscribeForRotation(Consumer<Double> callback) {
        rotationCallbacks.add(callback);
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
        for(Consumer<Double> c : rotationCallbacks) {
            c.accept(value);
        }
    }
}
