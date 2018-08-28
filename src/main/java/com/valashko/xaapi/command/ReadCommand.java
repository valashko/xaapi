package com.valashko.xaapi.command;

import java.nio.charset.StandardCharsets;

public class ReadCommand implements ICommand {
    private String sid;

    public ReadCommand(String sid) {
        this.sid = sid;
    }

    @Override
    public byte[] toBytes() {
        return new String("{\"cmd\":\"read\", \"sid\":\""+ sid +"\"}").getBytes(StandardCharsets.US_ASCII);
    }
}
