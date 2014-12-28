package com.remotemultimedia;

public class C {
	public static final String IP = "192.168.1.200";
	public static final int PORT = 7000;
	enum Type {
		STATUS, INFO, OTHER// 0, 1, 2
	}
	enum ServerStatus {
		Disconnected, Connecting, Connected
	}
}
