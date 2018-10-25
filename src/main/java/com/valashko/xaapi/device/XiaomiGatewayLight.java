package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.valashko.xaapi.XaapiException;

import java.awt.Color;
import java.util.HashMap;
import java.util.function.Consumer;

public class XiaomiGatewayLight extends BuiltinDevice {

    private byte brightness;
    private Color color = Color.BLACK; // TODO decide if this is an appropriate default value

    private HashMap<IInteractiveDevice.SubscriptionToken, Consumer<Byte>> brightnessCallbacks = new HashMap<>();
    private HashMap<IInteractiveDevice.SubscriptionToken, Consumer<Color>> colorCallbacks = new HashMap<>();

    public XiaomiGatewayLight(XiaomiGateway gateway) {
        super(gateway, Type.XiaomiGatewayLight);
    }

    @Override
    void update(String data) {
        try {
            JsonObject o = JSON_PARSER.parse(data).getAsJsonObject();
            int rgb = o.get("rgb").getAsInt();
            byte previousBrightnessValue = brightness;
            brightness = (byte)(rgb >>> 24);
            Color previousColorValue = color;
            color = new Color(rgb & 0x00FFFFFF);
            if(brightness != previousBrightnessValue) {
                notifyWithBrightnessChange(brightness);
            }
            if(! color.equals(previousColorValue)) {
                notifyWithColorChange(color);
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public byte getBrightness() {
        return brightness; // TODO query from device
    }

    public Color getColor() {
        return color; // TODO query from device
    }

    public IInteractiveDevice.SubscriptionToken subscribeForBrightnessChange(Consumer<Byte> callback) {
        IInteractiveDevice.SubscriptionToken token = new IInteractiveDevice.SubscriptionToken();
        brightnessCallbacks.put(token, callback);
        return token;
    }

    public void unsubscribeForBrightnessChange(IInteractiveDevice.SubscriptionToken token) {
        brightnessCallbacks.remove(token);
    }

    private void notifyWithBrightnessChange(byte value) {
        for(Consumer<Byte> c : brightnessCallbacks.values()) {
            c.accept(value);
        }
    }

    public IInteractiveDevice.SubscriptionToken subscribeForColorChange(Consumer<Color> callback) {
        IInteractiveDevice.SubscriptionToken token = new IInteractiveDevice.SubscriptionToken();
        colorCallbacks.put(token, callback);
        return token;
    }

    public void unsubscribeForColorChange(IInteractiveDevice.SubscriptionToken token) {
        colorCallbacks.remove(token);
    }

    private void notifyWithColorChange(Color value) {
        for(Consumer<Color> c : colorCallbacks.values()) {
            c.accept(value);
        }
    }

    public void setBrightness(byte brightness) throws XaapiException {
        writeBrightnessAndColor(brightness, this.color);
        this.brightness = brightness;
    }

    public void setColor(Color color) throws XaapiException {
        writeBrightnessAndColor(this.brightness, color);
        this.color = color;
    }

    private void writeBrightnessAndColor(int brightness, Color color) throws XaapiException {
        JsonObject rgb = new JsonObject();
        int rgbValue = brightness << 24 | color.getRGB();
        rgb.addProperty("rgb", rgbValue);
        gateway.sendDataToDevice(this, rgb);
    }
}
