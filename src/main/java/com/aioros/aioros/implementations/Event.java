package com.aioros.aioros.implementations;

import com.aioros.aioros.interfaces.IEvent;
import com.aioros.aioros.interfaces.IEventListener;
import com.aioros.aioros.interfaces.IEventSource;

import java.util.ArrayList;
import java.util.List;

public class Event implements IEvent {
    int type;
    Object data;
    EventSource source;
    long timestamp = System.currentTimeMillis();
    boolean stopPropagation;
    List<IEventListener> notifiedListeners = new ArrayList<>();
    List<IEventSource> notifiedParents = new ArrayList<>();
}
