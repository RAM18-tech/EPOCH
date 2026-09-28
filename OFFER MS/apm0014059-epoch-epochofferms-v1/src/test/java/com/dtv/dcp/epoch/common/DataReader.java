package com.dtv.dcp.epoch.common;

import java.io.File;
import java.io.IOException;

import org.aspectj.util.FileUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataReader {

	public <T> T readFileToObj(String filename, Class<T> clazz) {
		try {
			return new ObjectMapper().readValue(new File("./src/test/resources/"+filename), clazz);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public <T> T readFileToObj(String filename, TypeReference<T> valueTypeReference) {
		try {
			return new ObjectMapper().readValue(new File("./src/test/resources/"+filename), valueTypeReference);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String readFileAsString(String filename) {
		try {
			return FileUtil.readAsString(new File("./src/test/resources/"+filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
