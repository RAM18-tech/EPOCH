package com.dtv.dcp.epoch.integration.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.SocketTimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopIxpClient;
import com.dtv.dcp.epoch.message.ErrorMessages;

public class CpopIxpClientTest {

	@InjectMocks
	private CpopIxpClient cpopIxpClient;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private RestTemplateBeanFactory restTemplateFactory;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testCpopIxpClient() throws Exception {

		String clientName = "CPOPOFFERMS-IXP-API_CLIENT";
		when(restTemplateFactory.getObject(clientName)).thenReturn(restTemplate);
		try {
			cpopIxpClient = new CpopIxpClient(restTemplateFactory);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertNotNull(cpopIxpClient);
	}

	@Test
	public void testRecoverCpopWirelessBackupEnabled() {

		RestClientException exception = mock(RestClientException.class);
		when(exception.getRootCause()).thenReturn(new SocketTimeoutException("SocketTimeoutException"));
		ServiceException serviceException = assertThrows(ServiceException.class, () -> {
			cpopIxpClient.recoverCpopWirelessBackupEnabled(exception);
		});
		assertThat(serviceException.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CTLG_TIMEOUT_ERROR_UNKNOWN));

	}

//write test cases for recoverCpopWirelessBackupEnabled for the else condition
	@Test
	public void testRecoverCpopWirelessBackupEnabledElse() {

		RestClientException exception = mock(RestClientException.class);
		when(exception.getRootCause()).thenReturn(new Exception("Exception"));
		ServiceException serviceException = assertThrows(ServiceException.class, () -> {
			cpopIxpClient.recoverCpopWirelessBackupEnabled(exception);
		});
		assertThat(serviceException.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.EXTERNAL_PROCESSING_ERROR));
	}
}
