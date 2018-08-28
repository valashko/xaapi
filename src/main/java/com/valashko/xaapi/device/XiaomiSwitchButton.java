package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.valashko.xaapi.XaapiException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class XiaomiSwitchButton extends SlaveDevice implements IInteractiveDevice {

    public enum Action {
        Click,
        DoubleClick
    }

    private Action lastAction;
    private ArrayList<Consumer<String>> actionsCallbacks = new ArrayList<>();

    public XiaomiSwitchButton(String sid) {
        super(sid, Type.XiaomiSwitchButton);
    }

    @Override
    void update(String data) {
        try {
            JsonObject o = JSON_PARSER.parse(data).getAsJsonObject();
            if (o.has("status")) {
                updateWithAction(o.get("status").getAsString());
            }
        } catch (XaapiException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<Consumer<String>> getActionsCallbacks() {
        return actionsCallbacks;
    }

    public Action getLastAction() {
        return lastAction;
    }

    private void updateWithAction(String action) throws XaapiException {
        switch(action) {
            case "click":
                lastAction = Action.Click;
                break;
            case "double_click":
                lastAction = Action.DoubleClick;
                break;
            default:
                throw new XaapiException("Unknown action: " + action);
        }
        notifyWithAction(action);
    }
}
