package com.dtv.dcp.epoch.integration;

import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;

@Service
public class CpopIxpClient {

    private RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(CpopIxpClient.class);


    /**
     * The rest ixp.svc.url.
     */
    @Value("${apiclient.ixp.svc.url}")
    private String ixpSvcUrl;

    /**
     * @param restTemplateFactory
     * @throws Exception
     */
    public CpopIxpClient(RestTemplateBeanFactory restTemplateFactory) throws Exception {
        restTemplate = restTemplateFactory.getObject("CPOPOFFERMS-IXP-API_CLIENT");
    }

  
	/**
     * Check ixp flag
     *
     * @return
     */
    private String getIxpSvcUrl() {
        StringBuilder builder = new StringBuilder(ixpSvcUrl);
        log.info("iXP service flag check URL :: {}", builder.toString());
        return builder.toString();
    }

//    @Retryable(maxAttemptsExpression = "3", value = {RestClientException.class})
//    public boolean isCpopWirelessBackupEnabled() {
//        String ixpSvcURL = getIxpSvcUrl();
//        ResponseEntity<String> ixpResponse = invokeIxpEndpoint(ixpSvcURL, HttpMethod.GET);
//        boolean ixpEnabled = isCpopWirelessBackup(ixpResponse.getBody(), "allocations");
//        log.info(String.format("Ixp enabled %s", ixpEnabled));
//        return ixpEnabled;
//    }

    /**
     * Invoke the End point
     *
     * @param url
     * @param method
     * @return
     * @throws RestClientException
     */
    private ResponseEntity<String> invokeIxpEndpoint(String url, HttpMethod method) throws RestClientException {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Content-Type", "application/json");
        HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> result = restTemplate.exchange(url, method, httpEntity, String.class);
        return result;
    }


    @Recover
    public Boolean recoverCpopWirelessBackupEnabled(RestClientException re) {
        log.error("Exception occured for isCpopWirelessBackupEnabled API::", re);

        if (re != null && re.getRootCause() != null && re.getRootCause() instanceof SocketTimeoutException) {
            throw new ServiceException(ErrorMessages.CTLG_TIMEOUT_ERROR_UNKNOWN, re).addDetail(
                    ErrorMessages.CTLG_TIMEOUT_ERROR_UNKNOWN_DETAILS,
                    Integer.toString(10000));
        } else {
            throw new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, re).addDetail(
                    ErrorMessages.IXP_MS_BACKEND_ERROR, "Invoke ixp-allocation-manager-service For cpopwireless",
                    "IxpClient.isCpopWirelessBackupEnabled");
        }
    }

}
