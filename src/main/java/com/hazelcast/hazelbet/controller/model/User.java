package com.hazelcast.hazelbet.controller.model;

import lombok.Value;

import java.io.Serializable;

@Value
public class User implements Serializable {
    Long id;
    String userName;
    double balance;
}
