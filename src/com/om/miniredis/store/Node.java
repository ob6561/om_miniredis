package com.om.miniredis.store;

public class Node {
    String key;
    String value;
    Node prev;
    Node next;
    Node(String key, String value){
        this.key=key;
        this.value = value;
    }
}
