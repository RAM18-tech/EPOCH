package com.dtv.dcp.epoch.message;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LogMessagesTest {
	
	@Test
	public void testLogMessages() {
		LogMessages[] values = LogMessages.values();
		assertNotNull(values);
	}

}
