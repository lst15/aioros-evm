package com.aioros.aioros.interfaces;

public interface IEventSource {
    void notifyListeners(IEvent var1);
    void addListener(IEventListener var1);
}
