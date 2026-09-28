/**
 *
 */
package com.dtv.dcp.epoch.processor.satellite;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.integration.CpopUCCClientHelper;
import com.dtv.dcp.epoch.model.common.request.CartContext;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.IneligibleOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.product.Price;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.request.Pagination;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.sales.SatelliteSegmentationProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessor;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author ap778g
 */
@ExtendWith(MockitoExtension.class)
public class SatelliteCTOffersProcessorTest {

    @InjectMocks
    private SatelliteCTOffersProcessor satelliteCTOffersProcessor;

    @Mock
    CpopClientHelper cpopClient;

    @Mock
    SatelliteServicesOffersProcessor satelliteServicesOffersProcessor;

    @Mock
    OffersUtils offersUtils;

    @Mock
    private DMALookUpService dmaLookUpService;

    @Mock
    private PnpGroupUtils pnpGroupUtils;

    @Mock
    private FeatureManagerHelper featureHelper;

    @Mock
    private CpopUCCClientHelper cpopUCCClientHelper;

    @Mock
    SatelliteSegmentationProcessor satelliteSegmentationProcessor;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    private static final String OFFER_REQUEST_RETENTION_DISPUTE = "stms/offer_request_retention_dispute.json";
    private static final String OFFER_REQUEST_RETENTION_DISPUTE_BCODE = "stms/offer_request_retention_dispute_bcode_exclusion.json";
    private static final String RETENTION_DISPUTE_OFFER_RESP = "stms/retention_dispute_offer_response.json";
    private static final String OFFER_REQUEST_MSC = "acquisition/msc_offer_request.json";
    private static final String OFFER_RESPONSE_MSC = "acquisition/msc_offer_response.json";

    private static final String OFFER_REQUEST_ORDER_MOD_BP = "stms/offer_request_order_mod_bp.json";
    private static final String OFFER_REQUEST_SECOND_ORDER_MOD_BP = "stms/offer_request_second_order_mod_bp.json";
    private static final String OFFER_RESPONSE_ORDER_MOD_BP = "stms/offer_response_order_mod_bp.json";

    private static final String OFFER_REQUEST_ORDER_MOD_ADDON = "stms/offer_request_order_mod_addon.json";
    private static final String OFFER_RESPONSE_ORDER_MOD_ADDON = "stms/offer_response_order_mod_addon.json";

    private static final String OFFER_REQUEST_ORDER_MOD_ADDON_ESPN = "stms/offer_request_order_mod_addon_espn.json";
    private static final String OFFER_REQUEST_ORDER_MOD_ADDON_ESPN_BP_CHANGE = "stms/offer_request_order_mod_addon_espn_bp_change.json";
    private static final String OFFER_RESPONSE_ORDER_MOD_ADDON_ESPN = "stms/offer_response_order_mod_addon_espn.json";

    private static final String OFFER_RESPONSE_ORDER_MOD_SELECTED_BP_PREMIER = "stms/offer_response_order_mod_selected_bp_premier.json";

    private static final String OFFER_REQUEST_ORDER_MOD_ADDON_4k = "stms/offer_request_order_mod_addon_4k.json";

    private static final String OFFER_REQUEST_ORDER_MOD_ADDON_ESP = "stms/offer_request_order_mod_addon_esp.json";

    private static final String OFFER_REQUEST_ORDER_MOD_PREMIUM_ADDON = "stms/offer_request_order_mod_premium_addon.json";
    private static final String OFFER_RESPONSE_ORDER_MOD_PREMIUM_ADDON = "stms/offer_response_order_mod_premium_addon.json";

    private static final String OFFER_RESPONSE_ORDER_MOD_SELECTED_BP = "stms/offer_response_order_mod_selected_bp.json";

    private static final String OFFER_REQUEST_ORDER_MOD_INSURANCE = "stms/offer_request_order_mod_insurance.json";
    private static final String OFFER_RESPONSE_ORDER_MOD_INSURANCE = "stms/offer_response_order_mod_insurance.json";

    private static final String OFFER_REQUEST_ENABLER_SALES_NO_PP = "enabler/offer_request_enabler_sales_no_pp.json";
    private static final String OFFER_RESPONSE_SALES_PNP = "enabler/offer_response_sales_pnp.json";

    private static final String OFFER_REQUEST_STMS_SALES_LOCALS = "stms/offer_request_stms_sales_locals.json";
    private static final String OFFER_REQUEST_STMS_SALES_NO_LOCALS = "stms/offer_request_stms_sales_no_locals.json";
    private static final String OFFER_RESPONSE_SALES_LNL = "stms/offer_response_sales_lnl.json";

    private static final String ORDERMOD_LNL_ADDON_REQUEST = "stms/ordermod_lnl_addon_request.json";
    private static final String ORDERMOD_LNL_ADDON_RESPONSE = "stms/ordermod_lnl_addon_response.json";
    private static final String SUBCATEGORY_ADDON_REQUEST = "stms/subcategory_filter_addon_request.json";
    private static final String SUBCATEGORY_ADDON_RESPONSE = "stms/subcategory_filter_addon_response.json";

    private static final String OFFER_REQUEST_STMS_SALES_LOCALS_LCC = "stms/offer_request_stms_sales_locals_lcc.json";
    private static final String OFFER_REQUEST_STMS_SALES_NO_LOCALS_LCC = "stms/offer_request_stms_sales_no_locals_lcc.json";
    private static final String OFFER_RESPONSE_SALES_LCC = "stms/offer_response_sales_lcc.json";

    private static final String OFFER_REQUEST_SALES_BP_LOCALS_RR = "stms/offer_request_sales_bp_locals_rr.json";
    private static final String OFFER_REQUEST_SALES_BP_NO_LOCALS_RR = "stms/offer_request_sales_bp_no_locals_rr.json";
    private static final String OFFER_RESPONSE_SALES_BP_RR = "stms/offer_response_sales_bp_rr.json";
    private static final String OFFER_REQUEST_SALES_BP_LOCALS_NON_CONTRACT = "stms/offer_request_sales_bp_locals_non_contract.json";

    private static final String OFFER_REQUEST_SALES_ADDON_LOCALS_RR = "stms/offer_request_sales_addon_locals_rr.json";
    private static final String OFFER_REQUEST_SALES_ADDON_NO_LOCALS_RR = "stms/offer_request_sales_addon_no_locals_rr.json";
    private static final String OFFER_RESPONSE_SALES_ADDON_RR = "stms/offer_response_sales_addon_rr.json";

    private static final String OFFER_REQUEST_DEVICE_MDUDTH_24MO = "acquisition/offer_request_device_mdudth_24mo.json";
    private static final String OFFER_REQUEST_DEVICE_AGENT = "acquisition/offer_request_device_agent.json";
    private static final String OFFER_REQUEST_INSURANCE_MDUDTH = "acquisition/offer_request_insurance_mdudth.json";
    private static final String OFFER_RESPONSE_INSURANCE = "acquisition/offer_response_insurance.json";

    private static final String PRICE_SUBCATEGORY_ADDON_RESPONSE = "stms/price_subcategory_filter_addon_response.json";


    private static String OFFER_REQUEST_DEVICE = "{\r\n" +
            "  \"customerEligibility\": {\r\n" +
            "    \"zipCode\": [\r\n" +
            "      \"00058\"\r\n" +
            "    ],\r\n" +
            "    \"county\": [\r\n" +
            "      \"17000\"\r\n" +
            "    ]\r\n" +
            "  },\r\n" +
            "  \"offerActionType\": [\r\n" +
            "    \"Acquisition\"\r\n" +
            "  ],\r\n" +
            "  \"salesChannel\": [\r\n" +
            "    \"online\"\r\n" +
            "  ],\r\n" +
            "  \"offerProductTypes\": [\r\n" +
            "	\"fee\",\r\n" +
            "    \"video-device\"\r\n" +
            "  ],\r\n" +
            "  \"offerProductFamily\": [\r\n" +
            "    \"satellite\"\r\n" +
            "  ],\r\n" +
            "  \"cartContext\": {\r\n" +
            "    \"epochOfferCodes\": [\r\n" +
            "      \"OF_BASE-PREMIER-ALL-INCLUDED_sales_satellite\"\r\n" +
            "    ]\r\n" +
            "  }\r\n" +
            "}\r\n" +
            "";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetVideoAddonOffers() throws Exception {

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductTypes = new ArrayList<>();
        offerProductTypes.add(Constants.VIDEO_ADDON);
        offerRequest.setOfferProductType(offerProductTypes);
        List<String> offerActionType = new ArrayList<>();
        offerActionType.add(Constants.RETENTION_ACTION_TYPE);

        offerRequest.setOfferActionType(offerActionType);
        List<String> addOnType = new ArrayList<>();
        addOnType.add("Programming-Bolt-on");

        offerRequest.setAddOnType(addOnType);

        CustomerEligibility customerEligibility = new CustomerEligibility();
        List<String> custClassification = new ArrayList<>();
        custClassification.add("existingCustomer");
        customerEligibility.setCustClassification(custClassification);

        offerRequest.setCustomerEligibility(customerEligibility);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        List<CTOffer> responseList = new ArrayList<>();
        CTOffer ctOffer = new CTOffer();
        OfferAttributes attributes = new OfferAttributes();
        attributes.setOfferProductSubtype(Constants.BASE);
        ctOffer.setAttributes(attributes);
        responseList.add(ctOffer);
        ctOfferResponse.setOffers(responseList);
        offerRequestWrapper.setOfferRequest(offerRequest);

        doReturn(ctOfferResponse).when(cpopClient).getOffers(Mockito.any());

        CTOfferResponse offersResponse = satelliteCTOffersProcessor.getVideoAddonOffers(offerRequestWrapper);
        assertNotNull(offersResponse);
    }

    @Test
    public void testGetVideoAddonOffersAllAttribuites() throws Exception {

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductTypes = new ArrayList<>();
        offerProductTypes.add(Constants.BASE);
        offerRequest.setOfferProductType(offerProductTypes);
        List<String> offerActionType = new ArrayList<>();
        offerActionType.add(Constants.RETENTION_ACTION_TYPE);

        offerRequest.setOfferActionType(offerActionType);

        List<String> addOnType = new ArrayList<>();
        addOnType.add("Programming-Bolt-on");

        offerRequest.setAddOnType(addOnType);

        List<String> salesChannel = new ArrayList<>();
        salesChannel.add("online");

        offerRequest.setSalesChannel(salesChannel);

        CustomerEligibility customerEligibility = new CustomerEligibility();
        List<String> custClassification = new ArrayList<>();
        custClassification.add("existingCustomer");
        customerEligibility.setCustClassification(custClassification);

        offerRequest.setCustomerEligibility(customerEligibility);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        List<CTOffer> responseList = new ArrayList<>();
        CTOffer ctOffer = new CTOffer();
        OfferAttributes attributes = new OfferAttributes();
        attributes.setOfferProductSubtype(Constants.BASE);
        ctOffer.setAttributes(attributes);
        responseList.add(ctOffer);
        ctOfferResponse.setOffers(responseList);
        offerRequestWrapper.setOfferRequest(offerRequest);

        doReturn(ctOfferResponse).when(cpopClient).getOffers(Mockito.any());

        CTOfferResponse offersResponse = satelliteCTOffersProcessor.getVideoAddonOffers(offerRequestWrapper);
        assertNotNull(offersResponse);
    }

    @Test
    public void testGetVideoAddonOffersNull() throws Exception {

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

            OfferRequest offerRequest = new OfferRequest();
            List<String> offerProductTypes = new ArrayList<>();
            offerProductTypes.add(Constants.BASE);
            offerRequest.setOfferProductType(offerProductTypes);
            List<String> offerActionType = new ArrayList<>();
            offerActionType.add(Constants.RETENTION_ACTION_TYPE);

            offerRequest.setOfferActionType(offerActionType);

            List<String> addOnType = new ArrayList<>();
            addOnType.add("Programming-Bolt-on");

            offerRequest.setAddOnType(addOnType);

            List<String> salesChannel = new ArrayList<>();
            salesChannel.add("online");
            offerRequest.setSalesChannel(salesChannel);

            CustomerEligibility customerEligibility = new CustomerEligibility();
            List<String> custClassification = new ArrayList<>();
            custClassification.add("existingCustomer");
            customerEligibility.setCustClassification(custClassification);

            offerRequest.setCustomerEligibility(customerEligibility);

            CTOfferResponse ctOfferResponse = new CTOfferResponse();
            List<CTOffer> responseList = new ArrayList<>();
            CTOffer ctOffer = new CTOffer();
            OfferAttributes attributes = new OfferAttributes();
            attributes.setOfferProductSubtype(Constants.BASE);
            ctOffer.setAttributes(attributes);
            responseList.add(ctOffer);

            ctOfferResponse = null;
            offerRequestWrapper.setOfferRequest(offerRequest);

            doReturn(ctOfferResponse).when(cpopClient).getOffers(Mockito.any());
            satelliteCTOffersProcessor.getVideoAddonOffers(offerRequestWrapper);

        });
        assertTrue(StringUtils.isNotEmpty(exception.getMessage()));

    }

    @Test
    public void testAssociateFeeWithDeviceSTMSOnline() {
        List<CTOffer> ctSelectedOffer = new ArrayList<CTOffer>();
        CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device.json", CTOfferResponse.class);
        CTOfferResponse feeOffers = new DataReader().readFileToObj("acquisition/tv-access-fee.json", CTOfferResponse.class);
        CTOffer videoPlanSelectedOffer = new DataReader().readFileToObj("acquisition/video-plan-selected.json", CTOffer.class);
        ctSelectedOffer.add(videoPlanSelectedOffer);

        when(offersUtils.filterDeviceOffersByDeviceType(any(), any())).thenCallRealMethod();

        List<CTOffer> offers = satelliteCTOffersProcessor.associateFeeWithDevice(videoDeviceOffers.getOffers(), feeOffers, ctSelectedOffer);
        assertNotNull(offers);
        assertEquals(7, offers.size());

        Optional<CTOffer> ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-HD-DVR_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE_sales_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 0 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V1_satellite".equals(offer.getAttributes().getAssociatedFee()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V1_satellite".equals(offer.getAttributes().getAssociatedFee()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE_sales_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 0 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V2_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE_sales_satellite".equals(offer.getAttributes().getAssociatedFee()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V1_satellite".equals(offer.getAttributes().getAssociatedFee()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V1_satellite".equals(offer.getAttributes().getAssociatedFee()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes())
                        && (Objects.isNull(offer.getAttributes().getMinSelected())
                        || Objects.isNull(offer.getAttributes().getMaxSelected())))
                .findAny();
        assertFalse(ctOffer.isPresent());

    }

    @Test
    public void testAssociateFeeWithDeviceEnablerOnlineGEMINI() {
        List<CTOffer> ctSelectedOffer = new ArrayList<CTOffer>();
        CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-enabler-gemini.json", CTOfferResponse.class);
        CTOfferResponse feeOffers = new DataReader().readFileToObj("acquisition/tv-access-fee.json", CTOfferResponse.class);
        CTOffer videoPlanSelectedOffer = new DataReader().readFileToObj("acquisition/video-plan-selected.json", CTOffer.class);
        ctSelectedOffer.add(videoPlanSelectedOffer);

        when(offersUtils.filterDeviceOffersByDeviceType(any(), any())).thenCallRealMethod();

        List<CTOffer> offers = satelliteCTOffersProcessor.associateFeeWithDevice(videoDeviceOffers.getOffers(), feeOffers, ctSelectedOffer);
        assertNotNull(offers);
        assertEquals(8, offers.size());

        Optional<CTOffer> ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-HD-DVR_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE_sales_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 0 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V1_satellite".equals(offer.getAttributes().getAssociatedFee()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V1_satellite".equals(offer.getAttributes().getAssociatedFee()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V2_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE_sales_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 0 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GM4K-GEMINI_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE_sales_satellite".equals(offer.getAttributes().getAssociatedFee()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V1_satellite".equals(offer.getAttributes().getAssociatedFee()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V1_satellite".equals(offer.getAttributes().getAssociatedFee()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GM4K-GEMINI-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V1_satellite".equals(offer.getAttributes().getAssociatedFee()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes())
                        && (Objects.isNull(offer.getAttributes().getMinSelected())
                        || Objects.isNull(offer.getAttributes().getMaxSelected())))
                .findAny();
        assertFalse(ctOffer.isPresent());

    }

    @Test
    public void testGetVideoPlanOffersForRetention() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_RETENTION_DISPUTE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(RETENTION_DISPUTE_OFFER_RESP, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
        when(cpopClient.getOffers(ArgumentMatchers.any())).thenReturn(ctOfferResponse);
        CTOfferResponse retentionOffers = satelliteCTOffersProcessor.getVideoPlanOffersForRetention(offerRequestWrapper);
        assertNotNull(retentionOffers);
        assertEquals(14, retentionOffers.getOffers().size());
    }

    @Test
    public void testGetVideoPlanOffersForRetentionBCodeExclusion() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_RETENTION_DISPUTE_BCODE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(RETENTION_DISPUTE_OFFER_RESP, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
        when(cpopClient.getOffers(ArgumentMatchers.any())).thenReturn(ctOfferResponse);
        CTOfferResponse retentionOffers = satelliteCTOffersProcessor.getVideoPlanOffersForRetention(offerRequestWrapper);
        assertNotNull(retentionOffers);
        assertEquals(13, retentionOffers.getOffers().size());
    }

    @Test
    public void testGetOffersVideoPlanWithMsc() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_MSC, OfferRequest.class);
            offerRequest.setHasLocalChannels(true);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_MSC, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(offersUtils.hasLocalChannels(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isEligibleForServedMarket(Mockito.any())).thenCallRealMethod();
        when(cpopClient.getOffers(ArgumentMatchers.any())).thenReturn(ctOfferResponse);
        CTOfferResponse mscOffers = satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
        assertNotNull(mscOffers);
        assertEquals(mscOffers.getOffers().size(), 11);

    }

    @Test
    public void testApplyOrderModChangesVideoPlan() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_BP, OfferRequest.class);
            offerRequest.setHasLocalChannels(false);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_BP, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.hasLocalChannels(Mockito.any())).thenCallRealMethod();

        CTOfferResponse videoPlanOffers = satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
        assertNotNull(videoPlanOffers);

        Optional<CTOffer> ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BASE-PREMIER-ALL-INCLUDED-NO-LOCALS_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        long count = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .count();
        assertEquals(1, count);

        assertEquals(0, videoPlanOffers.getIneligibleOffers().size());
    }

    @Test
    public void testApplyOrderModChangesVideoAddon() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_ADDON, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_ADDON, CTOfferResponse.class);

            selectedBp = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_SELECTED_BP, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(Mockito.any(), Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isLocalChannelVideoAddon(any())).thenCallRealMethod();

        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, selectedBp.getOffers());
        assertNotNull(videoAddonOffers);

        Optional<CTOffer> ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-HBO-SHOW-STARZ-CINE-EPIX-V1_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        List<Product> products = ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts();
        long prodCount = products.stream().filter(Objects::nonNull).filter(prod -> prod.getObj().getVariants().get(0).getPrices().size() == 1).count();
        assertEquals(2, prodCount);

        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-MOVIES-EXTRA-PACK-V2_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        long count = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .count();
        assertEquals(2, count);

        assertEquals(1, videoAddonOffers.getIneligibleOffers().size());

        Optional<IneligibleOffer> ineligibleOffer = videoAddonOffers.getIneligibleOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "R120110".equals(offer.getProductId())
                        && "NFL SUNDAY TICKET 2022".equals(offer.getProductName()))
                .findAny();
        assertTrue(ineligibleOffer.isPresent());

    }

    @Test
    public void testApplyOrderModChangesVideoAddon4k() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_ADDON_4k, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_ADDON, CTOfferResponse.class);

            selectedBp = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_SELECTED_BP, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(Mockito.any(), Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isLocalChannelVideoAddon(any())).thenCallRealMethod();

        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, selectedBp.getOffers());
        assertNotNull(videoAddonOffers);

        Optional<CTOffer> ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-HALLMARK-MOVIES-NOW_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-FOX-NATION_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        long count = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .count();
        assertEquals(2, count);

        assertEquals(2, videoAddonOffers.getIneligibleOffers().size());

        Optional<IneligibleOffer> ineligibleOffer = videoAddonOffers.getIneligibleOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "R120114".equals(offer.getProductId())
                        && "NFL SUNDAY TICKET MAX 2022".equals(offer.getProductName()))
                .findAny();
        assertTrue(ineligibleOffer.isPresent());

        ineligibleOffer = videoAddonOffers.getIneligibleOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "P106642".equals(offer.getProductId())
                        && "Epix".equals(offer.getProductName()))
                .findAny();
        assertTrue(ineligibleOffer.isPresent());

        ineligibleOffer = videoAddonOffers.getIneligibleOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "B103750".equals(offer.getProductId())
                        && "4K Service".equals(offer.getProductName()))
                .findAny();
        assertFalse(ineligibleOffer.isPresent());

    }

    @Test
    public void testApplyOrderModChangesVideoAddonESP() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_ADDON_ESP, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_ADDON, CTOfferResponse.class);

            selectedBp = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_SELECTED_BP, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(Mockito.any(), Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isLocalChannelVideoAddon(any())).thenCallRealMethod();

        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, selectedBp.getOffers());
        assertNotNull(videoAddonOffers);

        Optional<CTOffer> ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-DIRECTV-SPORTS-PACK_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        long count = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .count();
        assertEquals(1, count);

        assertEquals(0, videoAddonOffers.getIneligibleOffers().size());

    }

    @Test
    public void testApplyOrderModChangesPremiumAddon() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_PREMIUM_ADDON, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_PREMIUM_ADDON, CTOfferResponse.class);

            selectedBp = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_SELECTED_BP, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(Mockito.any(), Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isLocalChannelVideoAddon(any())).thenCallRealMethod();

        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, selectedBp.getOffers());
        assertNotNull(videoAddonOffers);

        Optional<CTOffer> ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-RTVI_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-HBO-MAX_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-STARZ_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-SHOWTIME_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-CINEMAX_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-EPIX_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-DOG-TV_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-FOX-NATION_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-HBO-SHOW-STARZ-CINE-EPIX-V2_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        long count = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .count();
        assertEquals(9, count);

        assertEquals(1, videoAddonOffers.getIneligibleOffers().size());

        Optional<IneligibleOffer> ineligibleOffer = videoAddonOffers.getIneligibleOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "R120144".equals(offer.getProductId())
                        && "MLS DIRECT KICK 2022".equals(offer.getProductName()))
                .findAny();
        assertTrue(ineligibleOffer.isPresent());

    }

    @Test
    public void testApplyOrderModChangesInsurance() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_INSURANCE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_INSURANCE, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }

        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(featureHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)).thenReturn(false);
        when(featureHelper.isEnabled(Constants.FEATURE_MDU_DTH_ENABLED)).thenReturn(true);
        when(offersUtils.isNotValidBasedOnIneligibleSalesSubChannel(any(), any())).thenCallRealMethod();
        when(offersUtils.isNotValidBasedOnCommitmentDuration(any(), any(), any())).thenCallRealMethod();

        CTOfferResponse insuranceOffers = satelliteCTOffersProcessor.getInsuranceOffers(offerRequestWrapper, null);
        assertNotNull(insuranceOffers);

        Optional<CTOffer> ctOffer = insuranceOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_INSURANCE-PROTECTION-PLAN_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        long count = insuranceOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .count();
        assertEquals(1, count);

        assertEquals(0, insuranceOffers.getIneligibleOffers().size());

    }

    @Test
    public void testServerDateOverrideGetOffersSalesFlow() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj("stms/serverDate_getOffers_sales_request.json", OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj("stms/serverDate_getOffers_sales_ctresp.json", CTOfferResponse.class);

            selectedBp = new DataReader().readFileToObj("stms/serverDate_getOffers_sales_ctresp_selected.json", CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(Mockito.any(), Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isLocalChannelVideoAddon(any())).thenCallRealMethod();

        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, selectedBp.getOffers());
        assertNotNull(videoAddonOffers);

        Optional<CTOffer> ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-MLB-EXTRA-INNINGS-2023-V1_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ProductObj product = ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj();
        List<Price> prices = product.getVariants().get(0).getPrices();
        assertEquals(1, prices.size());

    }

    @Test
    public void testApplyOrderModChangesVideoPlanSecond() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SECOND_ORDER_MOD_BP, OfferRequest.class);
            offerRequest.setHasLocalChannels(true);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_BP, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.hasLocalChannels(Mockito.any())).thenCallRealMethod();

        CTOfferResponse videoPlanOffers = satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
        assertNotNull(videoPlanOffers);

        Optional<CTOffer> ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BASE-ULTIMATE-ALL-INCLUDED_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .findAny();
        assertTrue(ctOffer.isPresent());

        long count = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes())
                        && offer.getAttributes().getIsSelected() == Boolean.TRUE)
                .count();
        assertEquals(1, count);

        assertEquals(0, videoPlanOffers.getIneligibleOffers().size());
    }

    @Test
    public void testGetOffersVideoPlanSalesNoPP() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ENABLER_SALES_NO_PP, OfferRequest.class);
            offerRequest.setHasLocalChannels(true);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_PNP, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.hasLocalChannels(Mockito.any())).thenCallRealMethod();

        CTOfferResponse videoPlanOffers = satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
        assertNotNull(videoPlanOffers);

        Optional<CTOffer> ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BASE-PREFERRED-CHOICE-ALL-INCLUDED_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertNotNull(ctOffer.get());

        ProductObj product = ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj();
        List<Price> prices = product.getVariants().get(0).getPrices();
        assertEquals(1, prices.size());
        assertEquals("06", prices.get(0).getCustomerGroup());
    }

    @Test
    public void testGetOffersVideoPlanSalesLocals() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_STMS_SALES_LOCALS, OfferRequest.class);
            offerRequest.setHasLocalChannels(true);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_LNL, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.hasLocalChannels(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isEligibleForServedMarket(Mockito.any())).thenCallRealMethod();

        CTOfferResponse videoPlanOffers = satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
        assertNotNull(videoPlanOffers);

        Optional<CTOffer> ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BASE-PREMIER-ALL-INCLUDED-NO-LOCALS-LNL_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS-LNL_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

    }

    @Test
    public void testGetOffersVideoPlanSalesNoLocals() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_STMS_SALES_NO_LOCALS, OfferRequest.class);
            offerRequest.setHasLocalChannels(false);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_LNL, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.hasLocalChannels(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isEligibleForServedMarket(Mockito.any())).thenCallRealMethod();

        CTOfferResponse videoPlanOffers = satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
        assertNotNull(videoPlanOffers);

        Optional<CTOffer> ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BASE-PREMIER-ALL-INCLUDED-NO-LOCALS-LNL_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertFalse(ctOffer.isPresent());

        ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS-LNL_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertFalse(ctOffer.isPresent());

    }

    @Test
    public void testGetOffersVideoAddonForOrderModWithLNLBP() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(ORDERMOD_LNL_ADDON_REQUEST, OfferRequest.class);
            offerRequest.setHasLocalChannels(false);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(ORDERMOD_LNL_ADDON_RESPONSE, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();

        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, ctOfferResponse.getOffers());
        assertNotNull(videoAddonOffers);

        Optional<IneligibleOffer> ctOffer = videoAddonOffers.getIneligibleOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getProductId().equalsIgnoreCase("B120668")).findAny();

        assertTrue(ctOffer.isEmpty());

    }

    @Test
    public void testGetOfferByIdsIndirectNFFL() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj("stms/bom_request_stms_indirect_nffl.json", OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);

        CTOfferResponse offers = satelliteCTOffersProcessor.getOfferByIds(offerRequestWrapper, true, true);
        assertNotNull(offers);

    }

    @Test
    public void testGetOfferByIdsIndirectFFL() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj("stms/bom_request_stms_indirect_ffl.json", OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);

        CTOfferResponse offers = satelliteCTOffersProcessor.getOfferByIds(offerRequestWrapper, true, true);
        assertNotNull(offers);

    }

    @Test
    public void testGetOfferByIdsAgent() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj("stms/bom_request_stms_agent.json", OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);

        CTOfferResponse offers = satelliteCTOffersProcessor.getOfferByIds(offerRequestWrapper, true, true);
        assertNotNull(offers);

    }

    @Test
    public void testGetOfferByIdsOnline() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj("stms/bom_request_stms_online.json", OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);

        CTOfferResponse offers = satelliteCTOffersProcessor.getOfferByIds(offerRequestWrapper, true, true);
        assertNotNull(offers);

    }

    @Test
    public void testGetOffersVideoPlanSalesLocalsLCC() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_STMS_SALES_LOCALS_LCC, OfferRequest.class);
            offerRequest.setHasLocalChannels(true);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_LCC, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.hasLocalChannels(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isEligibleForServedMarket(Mockito.any())).thenCallRealMethod();

        CTOfferResponse videoPlanOffers = satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
        assertNotNull(videoPlanOffers);

        Optional<CTOffer> ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().contains("LCC") && offer.getAttributes().getIsLCCBasePackage() == true)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().contains("LNL"))
                .findAny();
        assertFalse(ctOffer.isPresent());

    }

    @Test
    public void testGetOffersVideoPlanSalesNoLocalsLCC() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_STMS_SALES_NO_LOCALS_LCC, OfferRequest.class);
            offerRequest.setHasLocalChannels(false);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_LCC, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.hasLocalChannels(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isEligibleForServedMarket(Mockito.any())).thenCallRealMethod();

        CTOfferResponse videoPlanOffers = satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
        assertNotNull(videoPlanOffers);

        Optional<CTOffer> ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().contains("LCC"))
                .findAny();
        assertFalse(ctOffer.isPresent());

        ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().contains("LNL"))
                .findAny();
        assertFalse(ctOffer.isPresent());

    }

    @Test
    public void testAssociateFeeWithDeviceSTMSOnlineGEMWH() {
        List<CTOffer> ctSelectedOffer = new ArrayList<CTOffer>();
        CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-stms-gemwh.json", CTOfferResponse.class);
        CTOfferResponse feeOffers = new DataReader().readFileToObj("acquisition/tv-access-fee-stms.json", CTOfferResponse.class);
        CTOffer videoPlanSelectedOffer = new DataReader().readFileToObj("acquisition/video-plan-selected.json", CTOffer.class);
        ctSelectedOffer.add(videoPlanSelectedOffer);

        when(offersUtils.filterDeviceOffersByDeviceType(any(), any())).thenCallRealMethod();
        when(featureHelper.isEnabled(Constants.FEATURE_GEM_WHOLE_HOME_ENABLED)).thenReturn(true);

        List<CTOffer> offers = satelliteCTOffersProcessor.associateFeeWithDevice(videoDeviceOffers.getOffers(), feeOffers, ctSelectedOffer);
        assertNotNull(offers);
        assertEquals(11, offers.size());

        Optional<CTOffer> ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-HD-DVR_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V4_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 0 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V5_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 1 && offer.getAttributes().getMaxSelected() == 3)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V5_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 4 && offer.getAttributes().getMaxSelected() == 5)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V4_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 0 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V3_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V4_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 0 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V4_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 1 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V2_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V4_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 1 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V5_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 2 && offer.getAttributes().getMaxSelected() == 4)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V5_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 5 && offer.getAttributes().getMaxSelected() == 8)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V5_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 2 && offer.getAttributes().getMaxSelected() == 4)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V5_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 5 && offer.getAttributes().getMaxSelected() == 8)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes())
                        && (Objects.isNull(offer.getAttributes().getMinSelected())
                        || Objects.isNull(offer.getAttributes().getMaxSelected())))
                .findAny();
        assertFalse(ctOffer.isPresent());

    }

    @Test
    public void testGetVideoDeviceSTMSOnlineCreateOrder() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-stms-gemwh.json", CTOfferResponse.class);
        OfferRequest offerRequest = new DataReader().readFileToObj("acquisition/video-device-req-create-order.json", OfferRequest.class);
        offerRequestWrapper.setOfferRequest(offerRequest);

        when(featureHelper.isEnabled(Constants.FEATURE_GEM_WHOLE_HOME_ENABLED)).thenReturn(true);

        when(cpopClient.getOffers(Mockito.any())).thenReturn(videoDeviceOffers);

        CTOfferResponse offers = satelliteCTOffersProcessor.getVideoDeviceOffers(offerRequestWrapper, null);
        assertNotNull(offers);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-HD-DVR_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-V1_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V3_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertFalse(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V2_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertFalse(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertFalse(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V1_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertFalse(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS-V1_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

    }

    @Test
    public void testGetVideoDeviceSTMSOnlineOrderMod() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-stms-gemwh.json", CTOfferResponse.class);
        OfferRequest offerRequest = new DataReader().readFileToObj("acquisition/video-device-req-order-mod.json", OfferRequest.class);
        offerRequestWrapper.setOfferRequest(offerRequest);

        when(featureHelper.isEnabled(Constants.FEATURE_GEM_WHOLE_HOME_ENABLED)).thenReturn(true);

        when(cpopClient.getOffers(Mockito.any())).thenReturn(videoDeviceOffers);

        CTOfferResponse offers = satelliteCTOffersProcessor.getVideoDeviceOffers(offerRequestWrapper, null);
        assertNotNull(offers);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-HD-DVR_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-V1_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V3_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V2_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V1_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS-V1_sales_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

    }

    @Test
    public void testGetSatelliteRewardCardOffers() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj("acquisition/offer_request_reward.json", OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj("acquisition/offer_response_reward.json", CTOfferResponse.class);

            selectedBp = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_SELECTED_BP, CTOfferResponse.class);

        } catch (Exception e) {
            // do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        Mockito.lenient().when(satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(Mockito.any(), Mockito.any()))
                .thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();

        CTOfferResponse rewardCardOffers = satelliteCTOffersProcessor.getSatelliteRewardCardOffers(offerRequestWrapper,
                selectedBp.getOffers());
        assertNotNull(rewardCardOffers);
        /**
         assertEquals(2, rewardCardOffers.getOffers().size());

         Optional<CTOffer> ctOffer = rewardCardOffers.getOffers().stream().filter(Objects::nonNull)
         .filter(offer -> "BASE-ENTERTAINMENT-ALL-INCLUDED_satellite".equals(offer.getCode()))
         .findAny();
         assertTrue(ctOffer.isPresent());
         **/
    }

    @Test
    public void testApplyOrderModChangesForCredit() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj("stms/offer_request_second_order_mod_device.json", OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_order_mod_device.json", CTOfferResponse.class);

            selectedBp = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_SELECTED_BP, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(Mockito.any(), Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        Mockito.lenient().when(offersUtils.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();

        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getSatelliteClosingOffers(offerRequestWrapper, selectedBp.getOffers());
        assertNotNull(videoAddonOffers);
        /**
         Optional<CTOffer> ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
         .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
         && offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 1)
         .findAny();
         assertTrue(ctOffer.isPresent());

         assertEquals(2, videoAddonOffers.getOffers().size());
         **/
    }

    @Test
    public void testFilterOffersBasedOnDMA() {

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequest offerRequest = null;
        offerRequest = new DataReader().readFileToObj("acquisition/offer_request_reward.json", OfferRequest.class);
        offerRequestWrapper.setOfferRequest(offerRequest);

        CustomerEligibility customerEligibility = new CustomerEligibility();
        List<String> custClassification = new ArrayList<>();
        custClassification.add("existingCustomer");
        customerEligibility.setCustClassification(custClassification);
        List<String> l1 = new ArrayList<String>();
        l1.add("ChoiceAll");
        customerEligibility.setDma(l1);
        ctOfferRequest.setCustomerEligibility(customerEligibility);
        offerRequestWrapper.setCtOfferRequest(ctOfferRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        List<CTOffer> responseList = new ArrayList<>();
        CTOffer ctOffer = new CTOffer();
        OfferAttributes attributes = new OfferAttributes();
        attributes.setOfferProductSubtype(Constants.BASE);
        ctOffer.setAttributes(attributes);
        responseList.add(ctOffer);
        ctOfferResponse.setOffers(responseList);

        satelliteCTOffersProcessor.filterOffersBasedOnDMA(offerRequestWrapper, ctOfferResponse);
        //Mockito.verify(responseList).removeIf(any());

    }

    @Test
    public void testFilterOffersBasedNonDMA() {

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequest offerRequest = null;
        offerRequest = new DataReader().readFileToObj("acquisition/offer_request_reward.json", OfferRequest.class);
        offerRequestWrapper.setOfferRequest(offerRequest);

        CustomerEligibility customerEligibility = new CustomerEligibility();
        List<String> custClassification = new ArrayList<>();
        custClassification.add("existingCustomer");
        customerEligibility.setCustClassification(custClassification);
        ctOfferRequest.setCustomerEligibility(customerEligibility);
        offerRequestWrapper.setCtOfferRequest(ctOfferRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        List<CTOffer> responseList = new ArrayList<>();
        CTOffer ctOffer = new CTOffer();
        OfferAttributes attributes = new OfferAttributes();
        attributes.setOfferProductSubtype(Constants.BASE);
        ctOffer.setAttributes(attributes);
        responseList.add(ctOffer);
        ctOfferResponse.setOffers(responseList);

        satelliteCTOffersProcessor.filterOffersBasedOnDMA(offerRequestWrapper, ctOfferResponse);
        //Mockito.verify(responseList).removeIf(any());
    }

    @Test
    public void testGetSatelliteFeeOffers_NonAcquisition() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();

        try {
            offerRequest = new DataReader().readFileToObj("acquisition/stms_bom_sales_multipleTVFee_request.json", OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj("stms/allFeeOffersResponse.json", CTOfferResponse.class);

        } catch (Exception e) {
            // do nothing
        }
        when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
        when(cpopClient.getOffers(ArgumentMatchers.any())).thenReturn(ctOfferResponse);
        List<CTOffer> offers = new ArrayList<>();
        CTOfferResponse offer = new DataReader().readFileToObj("stms/stms_deviceOffers_response.json",
                CTOfferResponse.class);
        offers.addAll(offer.getOffers());
        CTOfferResponse settliteFeeOffers = satelliteCTOffersProcessor.getSatelliteFeeOffers(offerRequestWrapper, offers);
        assertNotNull(settliteFeeOffers);
    }

    @Test
    public void testGetSatelliteFeeOffers_NonAcquisition_BomFlag_True() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setOfferProductFamily(Stream.of("OTT").collect(Collectors.toList()));
        offerRequest.setSalesChannel(Stream.of("online").collect(Collectors.toList()));
        offerRequest.setOfferIds(Stream.of("784446dd-1e27-423f-8bf5-8ca9c87896b2").collect(Collectors.toList()));
        offerRequest.setBundleProductIds(Stream.of("1234").collect(Collectors.toList()));
        offerRequest.setCustomerSegments(Arrays.asList(Constants.EMPLOYEE));
        offerRequest.setOfferActionType(Arrays.asList("Retention"));
        offerRequest.setBusinessSegment(Arrays.asList("aaa"));
        Pagination pagination = new Pagination();
        pagination.setLimit(300);
        pagination.setPage(1);
        offerRequest.setPagination(pagination);
        offerRequestWrapper.setOfferRequest(offerRequest);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();

        try {

            ctOfferResponse = new DataReader().readFileToObj("stms/allFeeOffersResponse.json", CTOfferResponse.class);

        } catch (Exception e) {
            // do nothing
        }

        Mockito.lenient().when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(Arrays.asList("682"));
        Mockito.lenient().when(cpopClient.getOffers(ArgumentMatchers.any())).thenReturn(ctOfferResponse);
        List<CTOffer> offers = new ArrayList<>();
        CTOfferResponse offer = new DataReader().readFileToObj("stms/stms_deviceOffers_response.json",
                CTOfferResponse.class);
        offers.addAll(offer.getOffers());
        CTOfferResponse settliteFeeOffers = satelliteCTOffersProcessor.getSatelliteFeeOffers(offerRequestWrapper,
                offers);
        assertNotNull(settliteFeeOffers);
    }

    @Test
    public void testGetOfferByIds_BomFlag_False() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setOfferProductFamily(Stream.of("OTT").collect(Collectors.toList()));
        offerRequest.setSalesChannel(Stream.of("online").collect(Collectors.toList()));
        offerRequest.setOfferIds(Stream.of("784446dd-1e27-423f-8bf5-8ca9c87896b2").collect(Collectors.toList()));
        offerRequest.setBundleProductIds(Stream.of("1234").collect(Collectors.toList()));
        offerRequest.setCustomerSegments(Arrays.asList(Constants.EMPLOYEE));
        offerRequest.setOfferActionType(Arrays.asList(Constants.ACQUISITION));
        offerRequest.setBusinessSegment(Arrays.asList("aaa"));
        Pagination pagination = new Pagination();
        pagination.setLimit(300);
        pagination.setPage(1);
        offerRequest.setPagination(pagination);
        CartContext cartContext = new CartContext();
        List<CartOffer> cartOffers = new ArrayList<>();
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS_satellite");
        cartOffer.setQuantity(1);
        cartOffers.add(cartOffer);
        cartContext.setCartOffers(cartOffers);
        offerRequest.setCartContext(cartContext);
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();

        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);

        CTOfferResponse offers = satelliteCTOffersProcessor.getOfferByIds(offerRequestWrapper, false, true);
        assertNotNull(offers);

    }

    @Test
    public void testGetOfferByIds_ElseConditions() {
        OfferRequest offerRequest = new OfferRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        try {
            offerRequest = objectMapper.readValue(OFFER_REQUEST_DEVICE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
        } catch (Exception e) {
        }

        CTOfferResponse ctOfferResponse = new CTOfferResponse();

        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);

        CTOfferResponse offers = satelliteCTOffersProcessor.getOfferByIds(offerRequestWrapper, false, true);
        assertNotNull(offers);

    }

    @Test
    public void testCalculateBestPrice() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj("stms/offer_request_second_order_mod_device.json", OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_order_mod_device.json", CTOfferResponse.class);

            selectedBp = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_SELECTED_BP, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        List<CTOffer> offers = new ArrayList<>();
        CTOfferResponse offer = new DataReader().readFileToObj("stms/stms_deviceOffers_response.json",
                CTOfferResponse.class);
        offers.addAll(offer.getOffers());
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(Mockito.any(), Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        Mockito.lenient().when(offersUtils.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        satelliteCTOffersProcessor.calculateBestPrice(offers, offerRequestWrapper, false);
        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getSatelliteClosingOffers(offerRequestWrapper, selectedBp.getOffers());
        assertNotNull(videoAddonOffers);

    }

    @Test
    public void testGetOffersVideoAddonForSubCategoryFilter() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(SUBCATEGORY_ADDON_REQUEST, OfferRequest.class);
            offerRequest.setHasLocalChannels(false);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(SUBCATEGORY_ADDON_RESPONSE, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        doCallRealMethod().when(offersUtils).processOffers(any(), any());
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();

        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, ctOfferResponse.getOffers());
        assertNotNull(videoAddonOffers);
        Optional<CTOffer> ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-SHOWTIME_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes()))
                .findAny();
        assertEquals("Popular", ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts()
                .get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getSubCategory());
    }

    @Test
    public void testGetOffersLocalsRoadrunner() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SALES_BP_LOCALS_RR, OfferRequest.class);
            offerRequest.setHasLocalChannels(true);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_BP_RR, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.isRoadrunner(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.hasLocalChannels(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isEligibleForServedMarket(Mockito.any())).thenCallRealMethod();
        when(featureHelper.isEnabled(Constants.FEATURE_ROADRUNNER_ENABLED)).thenReturn(true);
        when(featureHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)).thenReturn(false);

        CTOfferResponse offers = satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
        assertNotNull(offers);

        long count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes().getOfferProductType().equals(Constants.VIDEO_PLAN))
                .count();
        assertEquals(12, count);

        count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offersUtils.isRoadrunner(offer))
                .count();
        assertEquals(12, count);

    }

    @Test
    public void testGetOffersNoLocalsRoadrunner() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SALES_BP_NO_LOCALS_RR, OfferRequest.class);
            offerRequest.setHasLocalChannels(false);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_BP_RR, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.isRoadrunner(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.hasLocalChannels(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isEligibleForServedMarket(Mockito.any())).thenCallRealMethod();
        when(featureHelper.isEnabled(Constants.FEATURE_ROADRUNNER_ENABLED)).thenReturn(true);
        when(featureHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)).thenReturn(false);

        CTOfferResponse offers = satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
        assertNotNull(offers);

        long count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes().getOfferProductType().equals(Constants.VIDEO_PLAN))
                .count();
        assertEquals(12, count);

        count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offersUtils.isRoadrunner(offer))
                .count();
        assertEquals(12, count);

    }

    @Test
    public void testGetOffersLocalsAddonRoadrunner() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse bpOffers = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SALES_ADDON_LOCALS_RR, OfferRequest.class);
            offerRequest.setHasLocalChannels(true);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_ADDON_RR, CTOfferResponse.class);

            bpOffers = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_BP_RR, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.isLocalChannelVideoAddon(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();

        CTOfferResponse offers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, bpOffers.getOffers());
        assertNotNull(offers);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().equals("OF_BOLTON-LOCAL-CHANNELS-INCLUDED_satellite"))
                .findAny();
        assertTrue(ctOffer.isPresent());

    }

    @Test
    public void testGetOffersNoLocalsAddonRoadrunner() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SALES_ADDON_NO_LOCALS_RR, OfferRequest.class);
            offerRequest.setHasLocalChannels(false);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_ADDON_RR, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.isLocalChannelVideoAddon(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();

        CTOfferResponse offers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, null);
        assertNotNull(offers);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().equals("OF_BOLTON-LOCAL-CHANNELS-INCLUDED_satellite"))
                .findAny();
        assertFalse(ctOffer.isPresent());

    }

    @Test
    public void testAssociateFeeWithDeviceSTMSOnlineRoadrunner() {
        List<CTOffer> ctSelectedOffer = new ArrayList<CTOffer>();
        CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-stms-gemwh.json", CTOfferResponse.class);
        CTOfferResponse feeOffers = new DataReader().readFileToObj("acquisition/tv-access-fee-roadrunner.json", CTOfferResponse.class);
        CTOffer videoPlanSelectedOffer = new DataReader().readFileToObj("acquisition/video-plan-roadrunner.json", CTOffer.class);
        ctSelectedOffer.add(videoPlanSelectedOffer);

        when(offersUtils.filterDeviceOffersByDeviceType(any(), any())).thenCallRealMethod();
        when(offersUtils.isRoadrunner(any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(any())).thenCallRealMethod();
        when(featureHelper.isEnabled(Constants.FEATURE_GEM_WHOLE_HOME_ENABLED)).thenReturn(true);

        List<CTOffer> offers = satelliteCTOffersProcessor.associateFeeWithDevice(videoDeviceOffers.getOffers(), feeOffers, ctSelectedOffer);
        assertNotNull(offers);
        assertEquals(11, offers.size());

        Optional<CTOffer> ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-HD-DVR_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 0 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 1 && offer.getAttributes().getMaxSelected() == 3)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 4 && offer.getAttributes().getMaxSelected() == 5)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 0 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V3_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 0 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 1 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V2_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 1 && offer.getAttributes().getMaxSelected() == 1)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 2 && offer.getAttributes().getMaxSelected() == 4)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 5 && offer.getAttributes().getMaxSelected() == 8)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 2 && offer.getAttributes().getMaxSelected() == 4)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GEMINI-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
                        && "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getAttributes().getAssociatedFee())
                        && offer.getAttributes().getMinSelected() == 5 && offer.getAttributes().getMaxSelected() == 8)
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes())
                        && (Objects.isNull(offer.getAttributes().getMinSelected())
                        || Objects.isNull(offer.getAttributes().getMaxSelected())))
                .findAny();
        assertFalse(ctOffer.isPresent());

    }

    @Test
    public void testGetOffersLocalsNonContract() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SALES_BP_LOCALS_NON_CONTRACT, OfferRequest.class);
            offerRequest.setHasLocalChannels(true);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_BP_RR, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.isRoadrunner(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.hasLocalChannels(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isEligibleForServedMarket(Mockito.any())).thenCallRealMethod();
        when(featureHelper.isEnabled(Constants.FEATURE_ROADRUNNER_ENABLED)).thenReturn(true);
        when(featureHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)).thenReturn(false);

        CTOfferResponse offers = satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
        assertNotNull(offers);

        long count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes().getOfferProductType().equals(Constants.VIDEO_PLAN))
                .count();
        assertEquals(34, count);

        count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offersUtils.isRoadrunner(offer))
                .count();
        assertEquals(0, count);

    }

    @Test
    public void testCalculateBestPriceForRoadrunnerLocals() {

        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();

        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SALES_BP_LOCALS_RR, OfferRequest.class);
            offerRequest.setHasLocalChannels(true);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_BP_RR, CTOfferResponse.class);

        } catch (Exception e) {
            // do nothing
        }
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        Mockito.lenient().when(offersUtils.isRoadrunner(Mockito.any())).thenCallRealMethod();
        Mockito.lenient().when(redisCacheHelper.getValues("localsBoltonPrice", Constants.SATELLITE_PRODUCT_FAMILY))
                .thenReturn(Arrays.asList("12"));
        Mockito.doCallRealMethod().when(offersUtils).getTwoDigitRoundOffValue(any());

        satelliteCTOffersProcessor.calculateBestPrice(ctOfferResponse.getOffers(), offerRequestWrapper, true);

        Optional<CTOffer> ctOffer = ctOfferResponse.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BASE-ULTIMATE-RR_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes()))
                .findAny();
        assertTrue(ctOffer.isPresent());
        assertEquals(175.0, ctOffer.get().getAttributes().getOfferPricewithLocals().getDollarAmount(), 0.0);
        assertEquals(185.0, ctOffer.get().getAttributes().getPlanPricewithLocals().getDollarAmount(), 0.0);

        ctOffer = ctOfferResponse.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BASE-ULTIMATE-ALL-INCLUDED_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes()))
                .findAny();
        assertTrue(ctOffer.isPresent());
        assertNull(ctOffer.get().getAttributes().getOfferPricewithLocals());
        assertNull(ctOffer.get().getAttributes().getPlanPricewithLocals());
    }

    @Test
    public void testCalculateBestPriceForRoadrunnerNoLocals() {

        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();

        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SALES_BP_NO_LOCALS_RR, OfferRequest.class);
            offerRequest.setHasLocalChannels(false);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SALES_BP_RR, CTOfferResponse.class);

        } catch (Exception e) {
            // do nothing
        }
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        Mockito.lenient().when(offersUtils.isRoadrunner(Mockito.any())).thenCallRealMethod();
        Mockito.lenient().when(redisCacheHelper.getValues("localsBoltonPrice", Constants.SATELLITE_PRODUCT_FAMILY))
                .thenReturn(Arrays.asList("12"));
        Mockito.lenient().doCallRealMethod().when(offersUtils).getTwoDigitRoundOffValue(any());

        satelliteCTOffersProcessor.calculateBestPrice(ctOfferResponse.getOffers(), offerRequestWrapper, true);
        Optional<CTOffer> ctOffer = ctOfferResponse.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BASE-ULTIMATE-RR_sales_satellite".equals(offer.getCode())
                        && Objects.nonNull(offer.getAttributes()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        assertNull(ctOffer.get().getAttributes().getOfferPricewithLocals());
        assertNull(ctOffer.get().getAttributes().getPlanPricewithLocals());
    }

    @Test
    public void testGetHardwareInstantRebateOffersMdudth() {
        OfferRequest offerRequest = new OfferRequest();
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_DEVICE_MDUDTH_24MO, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
        } catch (Exception e) {
        }

        CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-agent.json", CTOfferResponse.class);

        CTOffer selectedOffer = new CTOffer();
        List<CTOffer> selectedOffers = new ArrayList<>();
        selectedOffers.add(selectedOffer);

        Mockito.doCallRealMethod().when(offersUtils).getProductObjFromOffer(any());
        when(featureHelper.isEnabled(Constants.FEATURE_MDU_DTH_ENABLED)).thenReturn(true);

        List<CTOffer> offers = satelliteCTOffersProcessor.getHardwareInstantRebateOffers(offerRequestWrapper, videoDeviceOffers.getOffers(), selectedOffers);

        Optional<CTOffer> ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-HD-DVR-V1_sales_satellite".equals(offer.getCode())
                        && Boolean.TRUE.equals(offer.getAttributes().getLeadDeviceOffer())).findAny();
        assertTrue(ctOffer.isPresent());

        long count = offers.stream().filter(Objects::nonNull)
                .filter(offer -> Boolean.TRUE.equals(offer.getAttributes().getLeadDeviceOffer()))
                .count();
        assertEquals(1, count);
    }

    @Test
    public void testGetHardwareInstantRebateOffersNonMdudth() {
        OfferRequest offerRequest = new OfferRequest();
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_DEVICE_AGENT, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
        } catch (Exception e) {
        }

        CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-agent.json", CTOfferResponse.class);

        CTOffer selectedOffer = new CTOffer();
        List<CTOffer> selectedOffers = new ArrayList<>();
        selectedOffers.add(selectedOffer);

        Mockito.doCallRealMethod().when(offersUtils).getProductObjFromOffer(any());
        when(featureHelper.isEnabled(Constants.FEATURE_MDU_DTH_ENABLED)).thenReturn(true);

        List<CTOffer> offers = satelliteCTOffersProcessor.getHardwareInstantRebateOffers(offerRequestWrapper, videoDeviceOffers.getOffers(), selectedOffers);

        Optional<CTOffer> ctOffer = offers.stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V1_sales_satellite".equals(offer.getCode())
                        && Boolean.TRUE.equals(offer.getAttributes().getLeadDeviceOffer())).findAny();
        assertTrue(ctOffer.isPresent());

        long count = offers.stream().filter(Objects::nonNull)
                .filter(offer -> Boolean.TRUE.equals(offer.getAttributes().getLeadDeviceOffer()))
                .count();
        assertEquals(1, count);
    }

    @Test
    public void testIneligibleSalesSubChannelInsurance() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_INSURANCE_MDUDTH, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_INSURANCE, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }

        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(featureHelper.isEnabled(Constants.FEATURE_MDU_DTH_ENABLED)).thenReturn(true);
        when(featureHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)).thenReturn(false);
        when(offersUtils.isNotValidBasedOnIneligibleSalesSubChannel(any(), any())).thenCallRealMethod();

        CTOfferResponse insuranceOffers = satelliteCTOffersProcessor.getInsuranceOffers(offerRequestWrapper, null);
        assertNotNull(insuranceOffers);
        assertEquals(0, insuranceOffers.getOffers().size());

    }

    @Test
    public void testGetSatelliteFeeOffersRoadrunner() {
        List<CTOffer> ctSelectedOffer = new ArrayList<CTOffer>();
        CTOfferResponse feeOffers = new DataReader().readFileToObj("acquisition/tv-access-fee-roadrunner.json", CTOfferResponse.class);
        CTOffer videoPlanSelectedOffer = new DataReader().readFileToObj("acquisition/video-plan-roadrunner.json", CTOffer.class);
        ctSelectedOffer.add(videoPlanSelectedOffer);

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new DataReader().readFileToObj("acquisition/video-device-request-rr.json", OfferRequest.class);
        offerRequestWrapper.setOfferRequest(offerRequest);

        when(featureHelper.isEnabled(Constants.FEATURE_RR_FEE_FILTER_ENABLED)).thenReturn(true);
        when(cpopClient.getOffers(any())).thenReturn(feeOffers);

        CTOfferResponse offers = satelliteCTOffersProcessor.getSatelliteFeeOffers(offerRequestWrapper, ctSelectedOffer);
        assertNotNull(offers);
        assertEquals(1, offers.getOffers().size());

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_FEE-TV-ACCESS-FEE-V29_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

    }

    @Test
    public void testGetSatelliteFeeOffersNonContract() {
        List<CTOffer> ctSelectedOffer = new ArrayList<CTOffer>();
        CTOfferResponse feeOffers = new DataReader().readFileToObj("acquisition/tv-access-fee-roadrunner.json", CTOfferResponse.class);
        CTOffer videoPlanSelectedOffer = new DataReader().readFileToObj("acquisition/video-plan-selected.json", CTOffer.class);
        ctSelectedOffer.add(videoPlanSelectedOffer);

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new DataReader().readFileToObj("acquisition/video-device-request-non-rr.json", OfferRequest.class);
        offerRequestWrapper.setOfferRequest(offerRequest);

        when(featureHelper.isEnabled(Constants.FEATURE_RR_FEE_FILTER_ENABLED)).thenReturn(true);
        when(cpopClient.getOffers(any())).thenReturn(feeOffers);

        CTOfferResponse offers = satelliteCTOffersProcessor.getSatelliteFeeOffers(offerRequestWrapper, ctSelectedOffer);
        assertNotNull(offers);
        assertEquals(2, offers.getOffers().size());

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_FEE-TV-ACCESS-FEE-V4_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

        ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_FEE-TV-ACCESS-FEE-V5_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());

    }

    @Test
    public void testSTMSServiceActivationFee0$GetOffers() {

        List<CTOffer> ctSelectedOffer = new ArrayList<CTOffer>();
        CTOfferResponse feeOffers = new DataReader().readFileToObj("acquisition/SAF_fee_response.json", CTOfferResponse.class);
        CTOffer videoPlanSelectedOffer = new DataReader().readFileToObj("acquisition/video-plan-roadrunner.json", CTOffer.class);
        ctSelectedOffer.add(videoPlanSelectedOffer);

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new DataReader().readFileToObj("acquisition/SAF_getOffers_0$_request.json", OfferRequest.class);
        offerRequestWrapper.setOfferRequest(offerRequest);

        when(featureHelper.isEnabled(Constants.FEATURE_RR_FEE_FILTER_ENABLED)).thenReturn(true);
        when(featureHelper.isEnabled(Constants.FEATURE_EPOCH_SERVICE_ACTIVATION_FEE_ENABLED)).thenReturn(true);
        when(cpopClient.getOffers(any())).thenReturn(feeOffers);
        when(offersUtils.isNotValidBasedOnIneligibleSalesSubChannel(any(), any())).thenCallRealMethod();

        CTOfferResponse offers = satelliteCTOffersProcessor.getSatelliteFeeOffers(offerRequestWrapper, ctSelectedOffer);
        assertNotNull(offers);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_FEE-SERVICE-ACTIVATION-SALES-V2_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());
    }

    @Test
    public void testSTMSServiceActivationFeeRackRateGetOffers() {

        List<CTOffer> ctSelectedOffer = new ArrayList<CTOffer>();
        CTOfferResponse feeOffers = new DataReader().readFileToObj("acquisition/SAF_fee_response.json", CTOfferResponse.class);
        CTOffer videoPlanSelectedOffer = new DataReader().readFileToObj("acquisition/video-plan-roadrunner.json", CTOffer.class);
        ctSelectedOffer.add(videoPlanSelectedOffer);

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new DataReader().readFileToObj("acquisition/SAF_getOffers_indirect_rackRate_request.json", OfferRequest.class);
        offerRequestWrapper.setOfferRequest(offerRequest);

        when(featureHelper.isEnabled(Constants.FEATURE_RR_FEE_FILTER_ENABLED)).thenReturn(true);
        when(featureHelper.isEnabled(Constants.FEATURE_EPOCH_SERVICE_ACTIVATION_FEE_ENABLED)).thenReturn(true);
        when(cpopClient.getOffers(any())).thenReturn(feeOffers);
        when(offersUtils.isNotValidBasedOnIneligibleSalesSubChannel(any(), any())).thenCallRealMethod();

        CTOfferResponse offers = satelliteCTOffersProcessor.getSatelliteFeeOffers(offerRequestWrapper, ctSelectedOffer);
        assertNotNull(offers);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_FEE-SERVICE-ACTIVATION-SALES-V1_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());
    }

    @Test
    public void testOnlineSTMSServiceActivationFeeRackRateGetOffers() {

        List<CTOffer> ctSelectedOffer = new ArrayList<CTOffer>();
        CTOfferResponse feeOffers = new DataReader().readFileToObj("acquisition/SAF_fee_response.json", CTOfferResponse.class);
        CTOffer videoPlanSelectedOffer = new DataReader().readFileToObj("acquisition/video-plan-roadrunner.json", CTOffer.class);
        ctSelectedOffer.add(videoPlanSelectedOffer);

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new DataReader().readFileToObj("acquisition/SAF_getOffers_online_rackRate_request.json", OfferRequest.class);
        offerRequestWrapper.setOfferRequest(offerRequest);

        when(featureHelper.isEnabled(Constants.FEATURE_RR_FEE_FILTER_ENABLED)).thenReturn(true);
        when(featureHelper.isEnabled(Constants.FEATURE_EPOCH_SERVICE_ACTIVATION_FEE_ENABLED)).thenReturn(true);
        when(cpopClient.getOffers(any())).thenReturn(feeOffers);
        when(offersUtils.isNotValidBasedOnIneligibleSalesSubChannel(any(), any())).thenCallRealMethod();

        CTOfferResponse offers = satelliteCTOffersProcessor.getSatelliteFeeOffers(offerRequestWrapper, ctSelectedOffer);
        assertNotNull(offers);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_FEE-SERVICE-ACTIVATION-SALES-V1_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());
    }

    @Test
    public void testGetOffersVideoAddonForSubCategoryFilterPrice() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(SUBCATEGORY_ADDON_REQUEST, OfferRequest.class);
            offerRequest.setHasLocalChannels(false);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(PRICE_SUBCATEGORY_ADDON_RESPONSE, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        doCallRealMethod().when(offersUtils).processOffers(any(), any());
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();

        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, ctOfferResponse.getOffers());
        assertNotNull(videoAddonOffers);
        Optional<CTOffer> ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-PEACOCK-V3_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes()))
                .findAny();
        assertEquals("EmbeddedSVOD", ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts()
                .get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getSubCategory());

        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-PEACOCK_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes()))
                .findAny();
        assertEquals("Streaming", ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts()
                .get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getSubCategory());
    }

    @Test
    public void testApplyOrderModChangesVideoAddonESPN() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_ADDON_ESPN, OfferRequest.class);
            offerRequest.setHasLocalChannels(true);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_ADDON_ESPN, CTOfferResponse.class);

            selectedBp = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_SELECTED_BP_PREMIER, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(Mockito.any(), Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isLocalChannelVideoAddon(any())).thenCallRealMethod();

        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, selectedBp.getOffers());
        assertNotNull(videoAddonOffers);

        assertEquals(0, videoAddonOffers.getIneligibleOffers().size());

    }

    @Test
    public void testApplyOrderModChangesVideoAddonESPNChangeBP() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_ADDON_ESPN_BP_CHANGE, OfferRequest.class);
            offerRequest.setHasLocalChannels(true);
            offerRequestWrapper.setOfferRequest(offerRequest);

            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_ADDON_ESPN, CTOfferResponse.class);

            selectedBp = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_SELECTED_BP_PREMIER, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        when(satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(Mockito.any(), Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        when(offersUtils.isLocalChannelVideoAddon(any())).thenCallRealMethod();

        CTOfferResponse videoAddonOffers = satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, selectedBp.getOffers());
        assertNotNull(videoAddonOffers);

        assertEquals(4, videoAddonOffers.getIneligibleOffers().size());

        Optional<IneligibleOffer> ineligibleOffer = videoAddonOffers.getIneligibleOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "P2143".equals(offer.getProductId())
                        && "Cinemax".equals(offer.getProductName()))
                .findAny();
        assertTrue(ineligibleOffer.isPresent());

        ineligibleOffer = videoAddonOffers.getIneligibleOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "P2142".equals(offer.getProductId())
                        && "Paramount+ with SHOWTIME".equals(offer.getProductName()))
                .findAny();
        assertTrue(ineligibleOffer.isPresent());

        ineligibleOffer = videoAddonOffers.getIneligibleOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "P2140".equals(offer.getProductId())
                        && "Max".equals(offer.getProductName()))
                .findAny();
        assertTrue(ineligibleOffer.isPresent());

        ineligibleOffer = videoAddonOffers.getIneligibleOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "P2141".equals(offer.getProductId())
                        && "STARZ".equals(offer.getProductName()))
                .findAny();
        assertTrue(ineligibleOffer.isPresent());

    }

}