# XAAPI
Java API for interacting with Aqara-compatible devices
which connect to a gateway through Zigbee.

* Xiaomi Door and Window Sensor
* Xiaomi Button
* Xiaomi Plug (Socket)
* Xiaomi Magic Cube
* Xiaomi Motion Sensor

## Example

```java
XiaomiGateway g = new XiaomiGateway("192.168.1.105");

String blueCubeSid = "158d0001118a81";
IInteractiveDevice.SubscriptionToken blueCubeActionsSubscriptionToken =
        g.getDevice(blueCubeSid).asXiaomiCube().subscribeForActions((String action) -> System.out.println("Blue cube: " + action));
IInteractiveDevice.SubscriptionToken blueCubeRotationSubscriptionToken =
    g.getDevice(blueCubeSid).asXiaomiCube().subscribeForRotation((Double angle) -> System.out.println("Blue cube rotated: " + angle));

String pinkCubeSid = "158d000101782c";
IInteractiveDevice.SubscriptionToken pinkCubeActionsSubscriptionToken =
    g.getDevice(pinkCubeSid).asXiaomiCube().subscribeForActions((String action) -> System.out.println("Pink cube: " + action));
IInteractiveDevice.SubscriptionToken pinkCubeRotationSubscriptionToken =
    g.getDevice(pinkCubeSid).asXiaomiCube().subscribeForRotation((Double angle) -> System.out.println("Pink cube rotated: " + angle));

String buttonSid = "158d0001232e95";
IInteractiveDevice.SubscriptionToken buttonActionsSubscriptionToken =
    g.getDevice(buttonSid).asXiaomiSwitchButton().subscribeForActions((String action) -> System.out.println("Button: " + action));

ExecutorService threadPool = Executors.newFixedThreadPool(1);
g.startReceivingUpdates(threadPool);
threadPool.awaitTermination(60, TimeUnit.SECONDS); // run for 60 seconds

// cancelling subscription is optional
g.getDevice(blueCubeSid).asXiaomiCube().unsubscribeForActions(blueCubeActionsSubscriptionToken);
g.getDevice(blueCubeSid).asXiaomiCube().unsubscribeForRotation(blueCubeRotationSubscriptionToken);

g.getDevice(pinkCubeSid).asXiaomiCube().unsubscribeForActions(pinkCubeActionsSubscriptionToken);
g.getDevice(pinkCubeSid).asXiaomiCube().unsubscribeForRotation(pinkCubeRotationSubscriptionToken);

g.getDevice(buttonSid).asXiaomiSwitchButton().unsubscribeForActions(buttonActionsSubscriptionToken);
```
