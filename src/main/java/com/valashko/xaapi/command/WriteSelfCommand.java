package com.valashko.xaapi.command;

import com.google.gson.JsonObject;
import com.valashko.xaapi.device.XiaomiGateway;

import java.nio.charset.StandardCharsets;

public class WriteSelfCommand implements ICommand {
    private XiaomiGateway gateway;
    private JsonObject data;

    public WriteSelfCommand(XiaomiGateway gateway, JsonObject data, String key) {
        this.gateway = gateway;
        this.data = data;
        data.addProperty("key", key);
    }

    @Override
    public byte[] toBytes() {
        String what = "{{\"cmd\":\"write\", \"sid\":\""+ gateway.getSid() +"\", \"data\":" + data + "}}";
        return what.getBytes(StandardCharsets.US_ASCII);
    }
}
