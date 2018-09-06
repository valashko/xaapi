package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.function.Consumer;

public class XiaomiGatewayIlluminationSensor extends BuiltinDevice {

    private int illumination;
    private HashMap<IInteractiveDevice.SubscriptionToken, Consumer<Integer>> illuminationChangeCallbacks = new HashMap<>();

    public XiaomiGatewayIlluminationSensor() {
        super(Type.XiaomiGatewayIlluminationSensor);
    }

    @Override
    void update(String data) {
        try {
            JsonObject o = JSON_PARSER.parse(data).getAsJsonObject();
            illumination = o.get("illumination").getAsInt();
            notifyWithIlluminationChange(illumination);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public int getIllumination() {
        return illumination;
    }

    public IInteractiveDevice.SubscriptionToken subscribeForIlluminationChange(Consumer<Integer> callback) {
        IInteractiveDevice.SubscriptionToken token = new IInteractiveDevice.SubscriptionToken();
        illuminationChangeCallbacks.put(token, callback);
        return token;
    }

    public void unsubscribeForIlluminationChange(IInteractiveDevice.SubscriptionToken token) {
        illuminationChangeCallbacks.remove(token);
    }

    private void notifyWithIlluminationChange(int value) {
        for(Consumer<Integer> c : illuminationChangeCallbacks.values()) {
            c.accept(value);
        }
    }
}
