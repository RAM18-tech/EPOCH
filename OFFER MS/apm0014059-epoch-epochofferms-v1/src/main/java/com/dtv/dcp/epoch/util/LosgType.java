package com.dtv.dcp.epoch.util;

public enum LosgType {
	IPTV("IPTV"), DTVS("DTVS"), HSIA("HSIA"), VOIP("VOIP");

	String losg;

	private LosgType(String losg) {
		this.losg = losg;
	}

	@Override
	public String toString() {
		return losg;
	}

	public static LosgType fromString(String losg) {
		for (LosgType losgType : LosgType.values()) {
			if (losgType.losg.equalsIgnoreCase(losg)) {
				return losgType;
			}
		}
		return null;
	}
}
