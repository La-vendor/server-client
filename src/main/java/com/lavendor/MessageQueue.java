package com.lavendor;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class MessageQueue {

    private final List<String> messages;
    private int currentIndex;

    public MessageQueue(String... messages) {
        this.messages = new ArrayList<>(List.of(messages));
        this.currentIndex = 0;
    }

    public boolean hasNext() {
        return currentIndex < messages.size();
    }

    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Message queue is empty");
        }
        return messages.get(currentIndex++);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }
}
