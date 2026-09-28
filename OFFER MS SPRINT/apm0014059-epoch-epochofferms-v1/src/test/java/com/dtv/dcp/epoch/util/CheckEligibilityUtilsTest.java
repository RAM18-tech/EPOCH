package com.dtv.dcp.epoch.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.CheckEligibilityRequest;
import com.dtv.dcp.epoch.model.common.response.CheckEligibiltyResponse;

@ExtendWith(MockitoExtension.class)
class CheckEligibilityUtilsTest {
    @InjectMocks
    CheckEligibilityUtils checkEligibilityUtils;

    @Mock
    RedisCacheHelper redisCacheHelper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(checkEligibilityUtils, "pauseEligiblityMonths", 2);
        ReflectionTestUtils.setField(checkEligibilityUtils, "pauseMonthsIfNoPause", "1,2,3");
        ReflectionTestUtils.setField(checkEligibilityUtils, "maxPauseMonthsfromConfig", 3);
        ReflectionTestUtils.setField(checkEligibilityUtils, "checkforAdditionalInfo", true);
        ReflectionTestUtils.setField(checkEligibilityUtils, "volPauseValidStatus", Arrays.asList("Pause", "Pending Pause"));
        ReflectionTestUtils.setField(checkEligibilityUtils, "validCustomerSegment", Arrays.asList("Residential"));
        ReflectionTestUtils.setField(checkEligibilityUtils, "validContractIndicator", Arrays.asList("EDSP"));
        ReflectionTestUtils.setField(checkEligibilityUtils, "providerNotAllowed", Arrays.asList("DIRECTV"));
        ReflectionTestUtils.setField(checkEligibilityUtils, "iapPlatformNotAllowed", Arrays.asList("GOOGLE"));
    }

    @Test
    void checkEligibilityConditionsIsEligibleForServicePauseTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_CHECK_MONTHS, Constants.OTT)).thenReturn(Arrays.asList("2"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MAX_PAUSE_MONTHS, Constants.OTT)).thenReturn(Arrays.asList("3"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_ADDI_INFO_CHECK, Constants.OTT)).thenReturn(Arrays.asList("true"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CUST_SEG, Constants.OTT)).thenReturn(Arrays.asList("Residential"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_NOT_ALLOWED_PROVIDER, Constants.OTT)).thenReturn(Arrays.asList("DIRECTV"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_IAP_PLATFORM_NOTALLOWED, Constants.OTT)).thenReturn(Arrays.asList("DIRECTV"));

        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\"]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        CheckEligibiltyResponse checkEligibiltyResponse = checkEligibilityUtils.checkEligibiltyConditions(checkEligibilityRequest);
        Assertions.assertNotNull(checkEligibiltyResponse, "Successfully covered the checkEligibilityConditionsTest test");
        Assertions.assertEquals(checkEligibiltyResponse.getPauseEligibilityDetails().getEligibleForServicePause(), true);
    }

    @Test
    void checkEligibilityConditionsWithVolPauseDetailsTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_CHECK_MONTHS, Constants.OTT)).thenReturn(Arrays.asList("2"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MAX_PAUSE_MONTHS, Constants.OTT)).thenReturn(Arrays.asList("3"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_ADDI_INFO_CHECK, Constants.OTT)).thenReturn(Arrays.asList("true"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CUST_SEG, Constants.OTT)).thenReturn(Arrays.asList("Residential"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_NOT_ALLOWED_PROVIDER, Constants.OTT)).thenReturn(Arrays.asList("DIRECTV"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_IAP_PLATFORM_NOTALLOWED, Constants.OTT)).thenReturn(Arrays.asList("DIRECTV"));

        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\",\"DIRECTV\"],\"volPauseDetails\":[{\"status\":\"Pause\",\"startDate\":\"10/24/2024\",\"endDate\":\"01/23/2025\"}]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        CheckEligibiltyResponse checkEligibiltyResponse = checkEligibilityUtils.checkEligibiltyConditions(checkEligibilityRequest);
        Assertions.assertNotNull(checkEligibiltyResponse, "Successfully covered the checkEligibilityConditionsTest test");
        Assertions.assertEquals(checkEligibiltyResponse.getPauseEligibilityDetails().getEligibleForServicePause(), false);
    }

    @Test
    void checkEligibilityConditionsWithAdditionalDetailsTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_CHECK_MONTHS, Constants.OTT)).thenReturn(Arrays.asList("2"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MAX_PAUSE_MONTHS, Constants.OTT)).thenReturn(Arrays.asList("3"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_ADDI_INFO_CHECK, Constants.OTT)).thenReturn(Arrays.asList("true"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CUST_SEG, Constants.OTT)).thenReturn(Arrays.asList("Residential"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_NOT_ALLOWED_PROVIDER, Constants.OTT)).thenReturn(Arrays.asList("DIRECTV"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_IAP_PLATFORM_NOTALLOWED, Constants.OTT)).thenReturn(Arrays.asList("DIRECTV"));

        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\",\"DIRECTV\"],\"volPauseDetails\":[{\"status\":\"Pause\",\"startDate\":\"10/24/2024\",\"endDate\":\"01/23/2025\"}],\"additionalDetails\":[{\"additionalInfoName\":\"serviceSuspendInfo\",\"params\":[{\"paramName\":\"paramName\",\"paramValue\":\"Suspend\"}]}]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        CheckEligibiltyResponse checkEligibiltyResponse = checkEligibilityUtils.checkEligibiltyConditions(checkEligibilityRequest);
        Assertions.assertNotNull(checkEligibiltyResponse, "Successfully covered the checkEligibilityConditionsTest test");
        Assertions.assertEquals(checkEligibiltyResponse.getPauseEligibilityDetails().getEligibleForServicePause(), false);
    }

    @Test
    void validateCheckEligibilityRequestWithOutVolPauseDetailsTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_SALES_CHANNELS, Constants.OTT)).thenReturn(Arrays.asList("opus", "directvOnline", "oemIAPFIRETV", "oemIAPROKUTV", "osprey"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\"]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
    }

    @Test
    void validateCheckEligibilityRequestWithVolPauseDetailsTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_SALES_CHANNELS, Constants.OTT)).thenReturn(Arrays.asList("opus", "directvOnline", "oemIAPFIRETV", "oemIAPROKUTV", "osprey"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\",\"DIRECTV\"],\"volPauseDetails\":[{\"status\":\"Pause\",\"startDate\":\"10/24/2024\",\"endDate\":\"01/23/2025\"}],\"additionalDetails\":[{\"additionalInfoName\":\"serviceSuspendInfo\",\"params\":[{\"paramName\":\"paramName\",\"paramValue\":\"Suspend\"}]}]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
    }

    @Test
    void validateCheckEligibilityRequestWithEmptyNOfferActionTypeTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\"]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }

    @Test
    void validateCheckEligibilityRequestWithEmptySalesChannelTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\"]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }
    @Test
    void validateCheckEligibilityRequestWithInvalidSalesChannelTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"INVALID\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\"]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }

    @Test
    void validateCheckEligibilityRequestWithEmptyOfferProductFamilyTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\"]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }

    @Test
    void validateCheckEligibilityRequestWithEmptyCustomerSegmentsTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\"]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }

    @Test
    void validateCheckEligibilityRequestWithEmptyContractIndicatorTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\"]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }
    @Test
    void validateCheckEligibilityRequestWithEmptyCustomerContextTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"]}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }

    @Test
    void validateCheckEligibilityRequestWithEmptyExistingProductFamilyTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }
    @Test
    void validateCheckEligibilityRequestWithEmptyInstallmentProviderTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }

    @Test
    void validateCheckEligibilityRequestWithVolPauseDetailsHavingEmptyStatusTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\",\"DIRECTV\"],\"volPauseDetails\":[{\"startDate\":\"10/24/2024\",\"endDate\":\"01/23/2025\"}],\"additionalDetails\":[{\"additionalInfoName\":\"serviceSuspendInfo\",\"params\":[{\"paramName\":\"paramName\",\"paramValue\":\"Suspend\"}]}]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }

    @Test
    void validateCheckEligibilityRequestWithVolPauseDetailsInvalidStatusTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\",\"DIRECTV\"],\"volPauseDetails\":[{\"status\":\"Suspend\",\"startDate\":\"10/24/2024\",\"endDate\":\"01/23/2025\"}],\"additionalDetails\":[{\"additionalInfoName\":\"serviceSuspendInfo\",\"params\":[{\"paramName\":\"paramName\",\"paramValue\":\"Suspend\"}]}]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }

    @Test
    void validateCheckEligibilityRequestWithVolPauseDetailsEmptyStartDateTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\",\"DIRECTV\"],\"volPauseDetails\":[{\"status\":\"Pause\",\"endDate\":\"01/23/2025\"}],\"additionalDetails\":[{\"additionalInfoName\":\"serviceSuspendInfo\",\"params\":[{\"paramName\":\"paramName\",\"paramValue\":\"Suspend\"}]}]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }
    @Test
    void validateCheckEligibilityRequestWithVolPauseDetailsInvalidStartDateTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\",\"DIRECTV\"],\"volPauseDetails\":[{\"status\":\"Pause\",\"startDate\":\"24/24/2024\",\"endDate\":\"01/23/2025\"}],\"additionalDetails\":[{\"additionalInfoName\":\"serviceSuspendInfo\",\"params\":[{\"paramName\":\"paramName\",\"paramValue\":\"Suspend\"}]}]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }

    @Test
    void validateCheckEligibilityRequestWithVolPauseDetailsEmptyEndDateTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\",\"DIRECTV\"],\"volPauseDetails\":[{\"status\":\"Pause\",\"startDate\":\"01/24/2024\"}],\"additionalDetails\":[{\"additionalInfoName\":\"serviceSuspendInfo\",\"params\":[{\"paramName\":\"paramName\",\"paramValue\":\"Suspend\"}]}]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }
    @Test
    void validateCheckEligibilityRequestWithVolPauseDetailsInvalidEndDateTest() {
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT)).thenReturn(Arrays.asList("EDSP,TAZ,TAZCONTRACT,TAZBYOD,contract,non-contract,RR"));
        String request = "{\"customerSegments\":\"Residential\",\"offerActionType\":[\"Acquisition\"],\"salesChannel\":\"directvOnline\",\"offerProductFamily\":\"OTT\",\"contractIndicator\":[\"TAZCONTRACT\"],\"customerContext\":{\"existingProductFamily\":[{\"parentSubscriptionDate\":\"07/24/2024\",\"hasActiveInstallments\":true,\"installmentProvider\":[\"AFFIRM\",\"DIRECTV\"],\"volPauseDetails\":[{\"status\":\"Pause\",\"startDate\":\"01/24/2024\",\"endDate\":\"23/23/2025\"}],\"additionalDetails\":[{\"additionalInfoName\":\"serviceSuspendInfo\",\"params\":[{\"paramName\":\"paramName\",\"paramValue\":\"Suspend\"}]}]}]}}";
        CheckEligibilityRequest checkEligibilityRequest = JsonService.getObjectFromJson(request, CheckEligibilityRequest.class);
        assertThrows(ServiceException.class, () -> {
            checkEligibilityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
        });
    }
}