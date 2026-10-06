package com.om.miniredis.store;

import java.util.HashMap;
import java.util.Map;

public class LRUCache {
    private final int capacity;
    private final Map<String, Node> map = new HashMap<>();
    private final Node head;
    private final Node tail;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        head = new Node(null, null);
        tail = new Node(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public String get(String key) {
        Node node = map.get(key);
        if (node == null) return null;
        moveToFront(node);
        return node.value;
    }

    public void put(String key, String value) {
        Node existing = map.get(key);
        if (existing != null) {
            existing.value = value;
            moveToFront(existing);
            return;
        }
        Node node = new Node(key, value);
        map.put(key, node);
        addToFront(node);

        if (map.size() > capacity) {
            Node lru = tail.prev;
            removeNode(lru);
            map.remove(lru.key);
        }
    }

    public boolean remove(String key) {
        Node node = map.remove(key);
        if (node == null) return false;
        removeNode(node);
        return true;
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    private void addToFront(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToFront(Node node) {
        removeNode(node);
        addToFront(node);
    }
}