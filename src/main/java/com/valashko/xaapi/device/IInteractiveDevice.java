package com.valashko.xaapi.device;

import java.util.Collection;
import java.util.function.Consumer;

public interface IInteractiveDevice {
    default void subscribeForActions(Consumer<String> callback) {
        getActionsCallbacks().add(callback);
    }

    default void notifyWithAction(String action) {
        for(Consumer<String> c : getActionsCallbacks()) {
            c.accept(action);
        }
    }

    Collection<Consumer<String>> getActionsCallbacks();
}
