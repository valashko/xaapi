package com.valashko.xaapi.device;

import com.google.gson.JsonParser;
import com.valashko.xaapi.XaapiException;

public abstract class BuiltinDevice {

    public enum Type {
        XiaomiGatewayLight,
        XiaomiGatewayIlluminationSensor
    }

    protected static JsonParser JSON_PARSER = new JsonParser();
    private String uid;
    private Type type;

    public BuiltinDevice(String uid, Type type) {
        this.uid = uid;
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public Type getType() {
        return type;
    }

    abstract void update(String data);

    public XiaomiGatewayLight asXiaomiGatewayLight() throws XaapiException {
        ensureType(Type.XiaomiGatewayLight);
        return (XiaomiGatewayLight) this;
    }

    public XiaomiGatewayIlluminationSensor asXiaomiGatewayIlluminationSensor() throws XaapiException {
        ensureType(Type.XiaomiGatewayIlluminationSensor);
        return (XiaomiGatewayIlluminationSensor) this;
    }

    private void ensureType(Type type) throws XaapiException {
        if(getType() != type) {
            throw new XaapiException("Device type mismatch. Expected " + type);
        }
    }
}
