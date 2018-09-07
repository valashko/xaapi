package com.valashko.xaapi.device;

import com.google.gson.JsonParser;
import com.valashko.xaapi.XaapiException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class SlaveDevice {

    public enum Type {
        XiaomiCube,
        XiaomiDoorWindowSensor,
        XiaomiSocket,
        XiaomiMotionSensor,
        XiaomiSwitchButton
    }

    protected static JsonParser JSON_PARSER = new JsonParser();
    protected XiaomiGateway gateway;
    private String sid;
    private Type type;

    public SlaveDevice(XiaomiGateway gateway, String sid, Type type) {
        this.gateway = gateway;
        this.sid = sid;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getSid() {
        return sid;
    }

    public short getShortId() {
        throw new NotImplementedException(); // TODO implement
    }

    abstract void update(String data);

    public XiaomiCube asXiaomiCube() throws XaapiException {
        ensureType(Type.XiaomiCube);
        return (XiaomiCube) this;
    }

    public XiaomiDoorWindowSensor asXiaomiDoorWindowSensor() throws XaapiException {
        ensureType(Type.XiaomiDoorWindowSensor);
        return (XiaomiDoorWindowSensor) this;
    }

    public XiaomiSocket asXiaomiSocket() throws XaapiException {
        ensureType(Type.XiaomiSocket);
        return (XiaomiSocket) this;
    }

    public XiaomiMotionSensor asXiaomiMotionSensor() throws XaapiException {
        ensureType(Type.XiaomiMotionSensor);
        return (XiaomiMotionSensor) this;
    }

    public XiaomiSwitchButton asXiaomiSwitchButton() throws XaapiException {
        ensureType(Type.XiaomiSwitchButton);
        return (XiaomiSwitchButton) this;
    }

    private void ensureType(Type type) throws XaapiException {
        if(getType() != type) {
            throw new XaapiException("Device type mismatch. Expected " + type);
        }
    }
}
