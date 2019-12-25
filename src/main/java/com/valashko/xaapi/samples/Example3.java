package com.valashko.xaapi.samples;

import com.valashko.xaapi.device.XiaomiGateway;
import com.valashko.xaapi.device.XiaomiGatewayLight;

import java.awt.*;

/**
 * Police lights
 *
 * You can find the gateway password here:
 * Mi Home App > Select the Gateway device > ... (dots at the top right of the screen) > About > Wireless communication protocol.
 * If the "Wireless communication protocol" option isn't shown, tap a plugin version number at the bottom of the screen several times.
 */
public class Example3 {
    private static final int SLEEP_TIMEOUT = 150;

    public static void main(String[] args) throws Exception {
        String password = "a22b4b5b6c7cc0d";
        XiaomiGateway gateway = XiaomiGateway.discover();
        gateway.configurePassword(password);
        XiaomiGatewayLight gatewayLight = gateway.getBuiltinLight();

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 3; j++) {
                gatewayLight.setColor(Color.RED);
                Thread.sleep(SLEEP_TIMEOUT);
                gatewayLight.setColor(Color.BLACK);
            }
            for (int j = 0; j < 3; j++) {
                gatewayLight.setColor(Color.BLUE);
                Thread.sleep(SLEEP_TIMEOUT);
                gatewayLight.setColor(Color.BLACK);
            }
        }
    }
}
