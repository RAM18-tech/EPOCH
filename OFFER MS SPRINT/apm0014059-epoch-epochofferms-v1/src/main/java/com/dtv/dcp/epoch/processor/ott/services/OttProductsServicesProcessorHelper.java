package com.dtv.dcp.epoch.processor.ott.services;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;

@Component
public class OttProductsServicesProcessorHelper {

    private static final Logger log = LoggerFactory.getLogger(OttProductsServicesProcessorHelper.class);

    @Autowired
    CpopClientHelper cpopClientHelper;


    public CTProductResponse getFilteredProducts(ProductRequestWrapper productRequestWrapper , String productType) {
        List<CTOffer> offersList = null;
        CTProductResponse productResponse = new CTProductResponse();
        CTProductResponse ctProductResponse = null;
        List<String> customerSegments = null;

        try {
            customerSegments = productRequestWrapper.isMobility() ? Stream.of("Mobility").collect(Collectors.toList()) :
            (Optional.ofNullable(productRequestWrapper.getProductRequest().getCustomerSegments()).isPresent() ? 
            productRequestWrapper.getProductRequest().getCustomerSegments() : Stream.of("Residential").collect(Collectors.toList()));
            if (isCustomerSegmentPresent(customerSegments, Constants.MOBILITY)) {

                // if request for base
                if (productType.equalsIgnoreCase(Constants.BASE)) {
                    ctProductResponse = getVideoAddonProducts(productRequestWrapper);
                }
                // if request for standalone
//                if (productType.equalsIgnoreCase(Constants.STANDALONE)) {
//                   // ctProductResponse = getVideoAddonStandalonOffers(offerRequestWrapper, true);
//                }
                productResponse = ctProductResponse;

            } else {
                // NON mobility case for base offers so sending the non mobility offers
                if (isCustomerSegmentPresent(customerSegments, Constants.RESIDENTIAL)) {
                    productResponse = getNonMobilityProducts(productRequestWrapper, productType);
                }
            }

        } catch (ServiceException se) {
            log.error("Got ServiceException getFilteredProducts.... ", se);
            log.error(Constants.GOT_EXCEPTION, se);
            throw new ServiceException(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR)
                    .addDetail(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR_DETAILS001);
            //throw se;
        }
//        catch (Exception e) {
//            log.error(Constants.GOT_EXCEPTION, e);
//            throw new ServiceException(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR)
//                    .addDetail(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR_DETAILS001);
//        }

        return productResponse;
    }

    private boolean isCustomerSegmentPresent(List<String> custSegments, String matchCustSegment) {
        return custSegments != null ? custSegments.stream().anyMatch(custSegment -> custSegment.equals(matchCustSegment)): false;
    }

    public CTProductResponse getVideoAddonProducts(ProductRequestWrapper productRequestWrapper) {
        CTProductResponse ctProductResponse = null;

        CTProductRequest ctProductRequest = new CTProductRequest();
        if (Objects.nonNull(productRequestWrapper.getCtProductRequest())) {
            BeanUtils.copyProperties(productRequestWrapper.getProductRequest(), ctProductRequest);
        }
        ctProductRequest.setProductTypes(Arrays.asList(Constants.VIDEO_PLAN));

        // No need to set Customer Segments as it is not defined for addon offers.
        // Also removing customerContext
        ctProductRequest.setCustomerContext(null);
        ctProductRequest.setSalesChannel(null);
        ctProductRequest.setContractApplicable(null);

        ctProductResponse = getProductsFromCT(ctProductRequest);

        return ctProductResponse;

    }
    public CTProductResponse getProductsFromCT(CTProductRequest ctProductRequest) {
        CTProductResponse productResponse = null;


        //ctProductRequest.setOfferProductFamily(Stream.of("OTT").collect(Collectors.toList()));
        productResponse = cpopClientHelper.getProducts(ctProductRequest);
        if (productResponse == null) {
            log.info("In Method getProductsFromCT :: Getting the null response from the CTMS");
			if (Objects.nonNull(ctProductRequest.getProductFamily())
					&& ctProductRequest.getProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				 throw new ServiceException(ErrorMessages.OTT_CT_ERROR)
                 .addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);					
			} else {
				 throw new ServiceException(ErrorMessages.CT_ERROR)
                 .addDetail(ErrorMessages.CT_ERROR_DETAILS);
			}
        }
        log.info("In Method getOffersFromCT.OttCTOffersProcessor() :: Getting response from the CTMS");
        return productResponse;
    }

    public CTProductResponse getNonMobilityProducts(ProductRequestWrapper productRequestWrapper,String productType ) {

        if(productType.equalsIgnoreCase(Constants.BASE)) {
            excludeCustomerContextWireless(productRequestWrapper);
            return  getVideoAddonProducts(productRequestWrapper);
        }
        if(productType.equalsIgnoreCase(Constants.STANDALONE)) {
            return null;//ottCTOffersProcessor.getVideoAddonStandalonOffers(offerRequestWrapper, false);
        }
        return null;
    }

    public void excludeCustomerContextWireless(ProductRequestWrapper productRequestWrapper) {
        if (Objects.nonNull(productRequestWrapper.getCtProductRequest())) {
            List<CTCustomerContext> customerContext = productRequestWrapper.getCtProductRequest().getCustomerContext();
            if (Objects.nonNull(customerContext)) {
                List<CTCustomerContext> filteredCustomerContext = customerContext.stream().filter(Objects::nonNull)
                        .filter(existProduct -> (!"wireless".equalsIgnoreCase(existProduct.getProductFamily())))
                        .collect(Collectors.toList());

                if(CollectionUtils.isNotEmpty(filteredCustomerContext)) {
                    productRequestWrapper.getCtProductRequest().setCustomerContext(filteredCustomerContext);
                }else {
                    productRequestWrapper.getCtProductRequest().setCustomerContext(null);
                }

            }
        }
    }

}
