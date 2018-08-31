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
g.getDevice(blueCubeSid).asXiaomiCube().subscribeForActions((String action) -> System.out.println("Blue cube: " + action));
g.getDevice(blueCubeSid).asXiaomiCube().subscribeForRotation((Double angle) -> System.out.println("Blue cube rotated: " + angle));

String pinkCubeSid = "158d000101782c";
g.getDevice(pinkCubeSid).asXiaomiCube().subscribeForActions((String action) -> System.out.println("Pink cube: " + action));
g.getDevice(pinkCubeSid).asXiaomiCube().subscribeForRotation((Double angle) -> System.out.println("Pink cube rotated: " + angle));

String buttonSid = "158d0001232e95";
g.getDevice(buttonSid).asXiaomiSwitchButton().subscribeForActions((String action) -> System.out.println("Button: " + action));

ExecutorService threadPool = Executors.newFixedThreadPool(1);
g.startReceivingUpdates(threadPool);
threadPool.awaitTermination(0, TimeUnit.SECONDS);
```
