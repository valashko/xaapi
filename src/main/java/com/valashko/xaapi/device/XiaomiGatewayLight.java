package com.valashko.xaapi.device;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.awt.Color;

public class XiaomiGatewayLight extends BuiltinDevice {

    private byte brightness;
    private Color color;

    public XiaomiGatewayLight() {
        super(Type.XiaomiGatewayLight);
    }

    @Override
    void update(String data) {
        try {
            JsonObject o = JSON_PARSER.parse(data).getAsJsonObject();
            int rgb = o.get("rgb").getAsInt();
            brightness = (byte)(rgb & 0xFF000000);
            color = new Color(rgb & 0x00FFFFFF);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public byte getBrightness() {
        return brightness;
    }

    public Color getColor() {
        return color;
    }

    // TODO implement subscription for brightness and color updates
}
