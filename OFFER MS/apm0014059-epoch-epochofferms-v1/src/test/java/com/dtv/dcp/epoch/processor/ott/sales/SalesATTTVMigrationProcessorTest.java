package com.dtv.dcp.epoch.processor.ott.sales;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.customergraph.response.UVCustomerAccountResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.TestUtility;

/**
 * @author vc402t
 *
 */
public class SalesATTTVMigrationProcessorTest {

	private static final String UVERSEBAN_629019590 = "629019590";
	@InjectMocks
	SalesATTTVMigrationProcessor salesATTTVMigrationProcessor;
	
	@Mock
	OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;
	
	@Mock
	FeatureManagerHelper featureHelper;

	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testPopulateMigrationRequest() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerContext\":{\"existingProductFamily\":[\"IPTV\",\"satellite\"],\"IPTV\":{\"products\":[{\"productCode\":\"u300\",\"productType\":\"video-plan\"}],\"accountNumber\":\"9999\",\"sunsetDate\":\"01102022\",\"isActive\":true},\"satellite\":{\"accountNumber\":\"9999\",\"isActive\":true}},\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"migrationServiceType\":[\"satellite\"],\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"contractIndicator\":[\"contract\"],\"customerSegments\":[\"Residential\"],\"exclusions\":{\"offerType\":[\"reward\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"]}",
				CTOfferRequest.class);
		salesATTTVMigrationProcessor.populateMigrationRequest(offerRequestWrapper, ctOfferRequest);
		assertNotNull(ctOfferRequest);
	}

	@Test
	public void testPopulateMigrationRequestForOpusLegacyDTVS() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerContext\":{\"existingProductFamily\":[\"IPTV\",\"satellite\"],\"IPTV\":{\"products\":[{\"productCode\":\"u300\",\"productType\":\"video-plan\"}],\"accountNumber\":\"9999\",\"sunsetDate\":\"01102022\",\"isActive\":true},\"satellite\":{\"accountNumber\":\"9999\",\"isActive\":true}},\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"migrationServiceType\":[\"satellite\"],\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"contractIndicator\":[\"contract\"],\"customerSegments\":[\"Residential\"],\"exclusions\":{\"offerType\":[\"reward\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"]}",
				CTOfferRequest.class);
		salesATTTVMigrationProcessor.populateMigrationRequest(offerRequestWrapper, ctOfferRequest);
		assertNotNull(ctOfferRequest);
	}
	@Test
	public void testPopulateMigrationRequestForOnlineMigrationDTVS() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerContext\":{\"existingProductFamily\":[\"IPTV\",\"satellite\"],\"IPTV\":{\"products\":[{\"productCode\":\"u300\",\"productType\":\"video-plan\"}],\"accountNumber\":\"9999\",\"sunsetDate\":\"01102022\",\"isActive\":true},\"satellite\":{\"accountNumber\":\"9999\",\"isActive\":true}},\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"migrationServiceType\":[\"satellite\"],\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"online\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"contractIndicator\":[\"contract\"],\"customerSegments\":[\"Residential\"],\"exclusions\":{\"offerType\":[\"reward\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"]}",
				CTOfferRequest.class);
		when(featureHelper.isEnabled("svc-iptvonline-launch")).thenReturn(true);
		salesATTTVMigrationProcessor.populateMigrationRequest(offerRequestWrapper, ctOfferRequest);
		assertNotNull(ctOfferRequest);
	}
	@Test
	public void testFilterMigrationEligibleOffersWithChannelOPUS() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerContext\":{\"existingProductFamily\":[\"IPTV\",\"satellite\"],\"IPTV\":{\"products\":[{\"productCode\":\"u300\",\"productType\":\"video-plan\"}],\"accountNumber\":\"9999\",\"sunsetDate\":\"01102022\",\"isActive\":true},\"satellite\":{\"accountNumber\":\"9999\",\"isActive\":true}},\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"contractIndicator\":[\"contract\"],\"customerSegments\":[\"Residential\"],\"exclusions\":{\"offerType\":[\"reward\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"]}",
				CTOfferRequest.class);
		CTOfferResponse ctOfferResponse = JsonService
				.getObjectFromJson(TestUtility.loadJson("/IPTVMigrCTOfferResponse.json"), CTOfferResponse.class);
		ctOfferResponse = salesATTTVMigrationProcessor.filterMigrationEligibleOffers(offerRequestWrapper,
				ctOfferRequest, ctOfferResponse);
		assertNotNull(ctOfferResponse);
	}

	@Test
	public void testFilterMigrationEligibleOffersWithChannelOnline() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerContext\":{\"existingProductFamily\":[\"IPTV\",\"satellite\"],\"IPTV\":{\"products\":[{\"productCode\":\"u300\",\"productType\":\"video-plan\"}],\"accountNumber\":\"9999\",\"sunsetDate\":\"01102022\",\"isActive\":true},\"satellite\":{\"accountNumber\":\"9999\",\"isActive\":true}},\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"online\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"contractIndicator\":[\"contract\"],\"customerSegments\":[\"Residential\"],\"exclusions\":{\"offerType\":[\"reward\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"]}",
				CTOfferRequest.class);
		CTOfferResponse ctOfferResponse = JsonService
				.getObjectFromJson(TestUtility.loadJson("/IPTVMigrCTOfferResponse.json"), CTOfferResponse.class);
		ctOfferResponse = salesATTTVMigrationProcessor.filterMigrationEligibleOffers(offerRequestWrapper,
				ctOfferRequest, ctOfferResponse);
		assertNotNull(ctOfferResponse);
	}

	@Test
	public void testFilterMigrationEligibleOffersWithSunsetDate() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerContext\":{\"existingProductFamily\":[\"IPTV\",\"satellite\"],\"IPTV\":{\"products\":[{\"productCode\":\"u300\",\"productType\":\"video-plan\"}],\"accountNumber\":\"9999\",\"sunsetDate\":\"01102021\",\"isActive\":true},\"satellite\":{\"accountNumber\":\"9999\",\"isActive\":true}},\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"contractIndicator\":[\"contract\"],\"customerSegments\":[\"Residential\"],\"exclusions\":{\"offerType\":[\"reward\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"]}",
				CTOfferRequest.class);
		CTOfferResponse ctOfferResponse = JsonService
				.getObjectFromJson(TestUtility.loadJson("/IPTVMigrCTOfferResponse.json"), CTOfferResponse.class);
		ctOfferResponse = salesATTTVMigrationProcessor.filterMigrationEligibleOffers(offerRequestWrapper,
				ctOfferRequest, ctOfferResponse);
		assertNotNull(ctOfferResponse);
	}

	@Test
	public void testPopulateMigrationRequestWithUverseBAN() throws Exception {
		 new UVCustomerAccountResponse();
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		offerRequestWrapper.setLinkedUverseAccountNums(UVERSEBAN_629019590);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"customerContext\":[{\"productFamily\":\"IPTV\",\"products\":[{\"productCode\":\"32232\"},{\"productCode\":\"32232\"},{\"productCode\":\"300ALL\",\"productType\":\"Base Package\"},{\"productCode\":\"34188\"}]}],\"customerEligibility\":{\"zipCode\":[\"53151\"]},\"customerSegments\":[\"Residential\"],\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":300},\"salesChannel\":[\"opus\"],\"state\":\"staged\",\"offerIntent\":[\"migration\"]}",
				CTOfferRequest.class);
		UVCustomerAccountResponse uverseResponse = JsonService.getObjectFromJson("{\"customer\":{\"profile\":{\"firstName\":\"Super\",\"lastName\":\"Man\",\"emailAddress\":\"sdfa@att.com\",\"customerType\":\"Consumer\",\"phoneNumber\":\"2104947788\",\"customerSubType\":\"Consumer\",\"creditRisk\":\"Low\",\"productTypes\":[\"UMC\",\"ACCS\",\"FL\",\"HSIA\",\"PLST\",\"RG\",\"WSPBAN\",\"CWSCTN\",\"WSPMC\"],\"birthDate\":\"{xW<-IL-ll\"},\"accounts\":[{\"accountStatus\":\"O\",\"addresses\":[{\"zip\":\"91911\",\"country\":\"USA\",\"dmaCode\":\"825\",\"city\":\"CHLA VSTA\",\"addressType\":\"ServiceFSP\",\"hsiaLowFlag\":\"FALSE\",\"hsiaHighFlag\":\"FALSE\",\"unitNumber\":\"646\",\"zip4\":\"1655\",\"unitType\":\"APT\",\"hsiaMedFlag\":\"FALSE\",\"streetName\":\"MOSS\",\"iptvFlag\":\"FALSE\",\"addressLine1\":\"646 MOSS\",\"voipFlag\":\"FALSE\",\"state\":\"CA\",\"unitValue\":\"38\"}],\"startServiceDate\":1579182411000,\"accountType\":\"UVERSEDTV\",\"systemOfRecord\":\"LS-CRM\",\"iptvSunsetDate\":\"2050-12-31 00:00:00\",\"ban\":\"630012224\"}]}}",UVCustomerAccountResponse.class);
		when(ottSalesOffersProcessorHelper.getUverseCustomerAccounts(UVERSEBAN_629019590, "uverse", UVCustomerAccountResponse.class)).thenReturn(uverseResponse);
		salesATTTVMigrationProcessor.populateMigrationRequest(offerRequestWrapper, ctOfferRequest);
		assertNotNull(ctOfferRequest);
	}

	@Test
	public void testFilterMigrationEligibleOffersWithUverseBAN() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		offerRequestWrapper.setLinkedUverseAccountNums(UVERSEBAN_629019590);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"customerContext\":[{\"productFamily\":\"IPTV\",\"products\":[{\"productCode\":\"32232\"},{\"productCode\":\"32232\"},{\"productCode\":\"300ALL\",\"productType\":\"Base Package\"},{\"productCode\":\"34188\"}]}],\"customerEligibility\":{\"zipCode\":[\"53151\"]},\"customerSegments\":[\"Residential\"],\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":300},\"salesChannel\":[\"opus\"],\"state\":\"staged\",\"offerIntent\":[\"migration\"]}",
				CTOfferRequest.class);
		CTOfferResponse ctOfferResponse = JsonService
				.getObjectFromJson(TestUtility.loadJson("/IPTVMigrCTOfferResponse.json"), CTOfferResponse.class);
		ctOfferResponse = salesATTTVMigrationProcessor.filterMigrationEligibleOffers(offerRequestWrapper,
				ctOfferRequest, ctOfferResponse);
		assertNotNull(ctOfferResponse);
	}

	@Test
	public void testFilterMigrationEligibleOffersWithUverseBANandChannelOnline() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"online\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		offerRequestWrapper.setLinkedUverseAccountNums(UVERSEBAN_629019590);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"customerContext\":[{\"productFamily\":\"IPTV\",\"products\":[{\"productCode\":\"32232\"},{\"productCode\":\"32232\"},{\"productCode\":\"300ALL\",\"productType\":\"Base Package\"},{\"productCode\":\"34188\"}]}],\"customerEligibility\":{\"zipCode\":[\"53151\"]},\"customerSegments\":[\"Residential\"],\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":300},\"salesChannel\":[\"opus\"],\"state\":\"staged\",\"offerIntent\":[\"migration\"]}",
				CTOfferRequest.class);
		CTOfferResponse ctOfferResponse = JsonService
				.getObjectFromJson(TestUtility.loadJson("/IPTVMigrCTOfferResponse.json"), CTOfferResponse.class);
		ctOfferResponse = salesATTTVMigrationProcessor.filterMigrationEligibleOffers(offerRequestWrapper,
				ctOfferRequest, ctOfferResponse);
		assertNotNull(ctOfferResponse);
	}
	
	@Test
	public void testFilterLegacySatelliteMigrationEligibleOffersWithUverseBAN() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		offerRequestWrapper.setLinkedUverseAccountNums(UVERSEBAN_629019590);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"customerContext\":[{\"productFamily\":\"IPTV\",\"products\":[{\"productCode\":\"32232\"},{\"productCode\":\"32232\"},{\"productCode\":\"300ALL\",\"productType\":\"Base Package\"},{\"productCode\":\"34188\"}]}],\"customerEligibility\":{\"zipCode\":[\"53151\"]},\"customerSegments\":[\"Residential\"],\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":300},\"salesChannel\":[\"opus\"],\"state\":\"staged\",\"offerIntent\":[\"migration\"]}",
				CTOfferRequest.class);
		CTOfferResponse ctOfferResponse = JsonService
				.getObjectFromJson(TestUtility.loadJson("/IPTVMigrLegacySatelliteOfferResponse.json"), CTOfferResponse.class);
		ctOfferResponse = salesATTTVMigrationProcessor.filterMigrationEligibleOffers(offerRequestWrapper,
				ctOfferRequest, ctOfferResponse);
		assertNotNull(ctOfferResponse);
	}
	@Test
	public void testFilterLegacySatelliteMigrationEligibleOffersWithChannelOPUS() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerContext\":{\"existingProductFamily\":[\"IPTV\",\"satellite\"],\"IPTV\":{\"products\":[{\"productCode\":\"u300\",\"productType\":\"video-plan\"}],\"accountNumber\":\"9999\",\"sunsetDate\":\"01102022\",\"isActive\":true},\"satellite\":{\"accountNumber\":\"9999\",\"isActive\":true}},\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"contractIndicator\":[\"contract\"],\"customerSegments\":[\"Residential\"],\"exclusions\":{\"offerType\":[\"reward\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"]}",
				CTOfferRequest.class);
		CTOfferResponse ctOfferResponse = JsonService
				.getObjectFromJson(TestUtility.loadJson("/IPTVMigrLegacySatelliteOfferResponse.json"), CTOfferResponse.class);
		ctOfferResponse = salesATTTVMigrationProcessor.filterMigrationEligibleOffers(offerRequestWrapper,
				ctOfferRequest, ctOfferResponse);
		assertNotNull(ctOfferResponse);
	}
	@Test
	public void testPopulateMigrationWithCustomMigrationCohortRequest() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerContext\":{\"existingProductFamily\":[\"IPTV\",\"satellite\"],\"IPTV\":{\"products\":[{\"productCode\":\"u300\",\"productType\":\"video-plan\"}],\"accountNumber\":\"9999\",\"isActive\":true},\"satellite\":{\"accountNumber\":\"9999\",\"isActive\":true}},\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"migrationServiceType\":[\"satellite\"],\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"contractIndicator\":[\"contract\"],\"customerSegments\":[\"Residential\"],\"exclusions\":{\"offerType\":[\"reward\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"]}",
				CTOfferRequest.class);
		salesATTTVMigrationProcessor.populateMigrationRequest(offerRequestWrapper, ctOfferRequest);
		assertNotNull(ctOfferRequest);
	}
	@Test
	public void testPopulateCTRequestMigrationServiceTypeWithUverseBAN() throws Exception {
		OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(
				"{\"offerRequest\":{\"contractIndicator\":[\"contract\"],\"customerEligibility\":{\"zipCode\":[\"32958\"]},\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"migrationServiceType\":[\"satellite\"],\"pagination\":{\"page\":1,\"limit\":100},\"salesChannel\":[\"opus\"],\"offerProductTypes\":[\"video-plan\"]},\"iptvMigration\":false,\"iptvMigrationIntent\":false,\"mobility\":false}",
				OfferRequestWrapper.class);
		offerRequestWrapper.setLinkedUverseAccountNums(UVERSEBAN_629019590);
		CTOfferRequest ctOfferRequest = JsonService.getObjectFromJson(
				"{\"customerContext\":[{\"productFamily\":\"IPTV\",\"products\":[{\"productCode\":\"32232\"},{\"productCode\":\"32232\"},{\"productCode\":\"300ALL\",\"productType\":\"Base Package\"},{\"productCode\":\"34188\"}]},{\"productFamily\":\"satellite\",\"products\":[]}],\"customerEligibility\":{\"zipCode\":[\"53151\"]},\"customerSegments\":[\"Residential\"],\"offerActionType\":[\"Acquisition\"],\"offerProductFamily\":[\"OTT\"],\"offerProductType\":[\"video-plan\"],\"pagination\":{\"page\":1,\"limit\":300},\"salesChannel\":[\"opus\"],\"state\":\"staged\",\"offerIntent\":[\"migration\"]}",
				CTOfferRequest.class);
		when(featureHelper.isEnabled("svc-iptvonline-launch")).thenReturn(true);
		salesATTTVMigrationProcessor.populateMigrationRequest(offerRequestWrapper, ctOfferRequest);
		assertNotNull(ctOfferRequest);
	}
}