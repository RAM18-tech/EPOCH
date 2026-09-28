package com.dtv.ct.commercetool.service;

import com.dtv.ct.commercetool.model.ProductUpdateRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class RestServiceImpl implements RestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceImpl.class);


    @Autowired
    private PriceProcessor priceProcessor;

    @Autowired
    private EnumUpdateProcessor enumUpdateProcessor;

    @Autowired
    private CompareProcessor compareProcessor;

    @Autowired
    private CompareProcessorV2 compareProcessorV2;

    @Autowired
    private Rebranding rebranding;

    @Autowired
    private RebrandingRefactor rebrandingRefactor;

    @Autowired
    private PublishAndUnPublishProcessor publishAndUnPublishProcessor;
    @Autowired
    private ProductProcessor productProcessor;
    @Autowired
    private FeatureFlagProcessor featureFlagProcessor;

    @Override
    public ResponseEntity<String> updateCT(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.updateCT Method");
        try {
            productProcessor.updateAssociatedProduct(request);
            //productProcessor.getAllOttBenfits(request);
            // productProcessor.updateBenefit(null, request.getEnvironment(),request.getProductName());
            // productProcessor.updateBenefitEmptyString(null, request.getEnvironment(),request.getProductName());

            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed : {}" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<String>> priceUpdate(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.priceUpdate Method");
        try {
            List<String> list = priceProcessor.productUpdatePriceDates(request);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Arrays.asList(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<String>> priceTierUpdate(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.priceTierUpdate Method");
        try {
            List<String> list = priceProcessor.priceTierUpdate(request);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Arrays.asList(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<String>> priceAdd(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.priceAdd Method");
        try {
            List<String> list = priceProcessor.pricesAddNewPrices(request);
            if (CollectionUtils.isEmpty(list)) {
                return new ResponseEntity<>(Arrays.asList("Success"), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(list, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Arrays.asList( e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> storeIdUpdate(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.storeIdUpdate Method");
        try {
            enumUpdateProcessor.storeIDUpdate(request);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> publishOrUnPublish(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.publishOrUnPublish Method");
        try {
            publishAndUnPublishProcessor.unPublishAndSetEndDate(request);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Map<String, String>> findEmptyProdList(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.publishOrUnPublish Method");
        try {
            Map<String, String> resp = productProcessor.findEmptyProductRef(request);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            //return new ResponseEntity<>("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Override
    public ResponseEntity<Map<String, String>> findExpiredEnDateOfProduct(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.findExpiredEnDateOfProduct Method");
        try {
            Map<String, String> resp = productProcessor.findExpiredEnDateOfProduct(request);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            //return new ResponseEntity<>("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }


    @Override
    public ResponseEntity<List<StringBuilder>> story(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.findExpiredEnDateOfProduct Method");
        try {
            //List<String> response = productProcessor.getBundlePrd(request);
            // List<StringBuilder> resp = productProcessor.story2(response, request);
           //productProcessor.findAllConflictingOffers(request);
            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            //return new ResponseEntity<>("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Override
    public ResponseEntity<Map<String, String>> findMissingReferences(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.findMissingReferences Method");
        try {
            Map<String, String> resp = publishAndUnPublishProcessor.checkIfqualifyingProductEmptyInOffer(request);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            //return new ResponseEntity<>("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Override
    public ResponseEntity<String> multiTextUpdate(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.multiTextUpdate Method");
        try {
            enumUpdateProcessor.empty(request);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            //return new ResponseEntity<>("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Override
    public ResponseEntity<Map<String, String>> compare(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.compare Method");
        try {
            if (StringUtils.isAnyEmpty(request.getSource(), request.getTarget(), request.getUserId())){
                return new ResponseEntity("Source, Target and UserId are mandatory fields", HttpStatus.BAD_REQUEST);
            }
            Map<String, String> response = new HashMap<>();
            if (CollectionUtils.isNotEmpty(request.getOfferCodes())) {
                if (request.getOfferCodes().contains("epochGlobalConfigurations")){
                    response.putAll(compareProcessor.epochGlobalConfigCompare(request));
                    request.getOfferCodes().remove("epochGlobalConfigurations");
                }
                response.putAll(compareProcessor.compareProducts(request));
            }
            if (CollectionUtils.isNotEmpty(request.getBenefitCodes())) {
                response.putAll( compareProcessor.compareBenefits(request));
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Map<String, String>> compareV2(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.compareV2 Method");
        try {
            if (StringUtils.isAnyEmpty(request.getSource(), request.getTarget(), request.getUserId())) {
                return new ResponseEntity("Source, Target and UserId are mandatory fields", HttpStatus.BAD_REQUEST);
            }
            Map<String, String> response = new HashMap<>();
            if (CollectionUtils.isNotEmpty(request.getOfferCodes())) {
                if (request.getOfferCodes().contains("epochGlobalConfigurations")) {
                    response.putAll(compareProcessor.epochGlobalConfigCompare(request));
                    request.getOfferCodes().remove("epochGlobalConfigurations");
                }
                response.putAll(compareProcessorV2.compareProducts(request));
            }
            if (CollectionUtils.isNotEmpty(request.getBenefitCodes())) {
                response.putAll(compareProcessor.compareBenefits(request));
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> updateSalesChannel(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.updateCT Method");
        try {
           productProcessor.updateSalesChannelBothInsideOutside(request);
           // productProcessor.updateALlowConflictingFromConflicting(request);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed : {}" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> dealerCodesUpdate(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.dealerCodesUpdate Method");
        try {
            enumUpdateProcessor.dealerCodesUpdate(request);
            return new ResponseEntity<>(" dealerCodesUpdate Success", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("dealerCodesUpdate Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> generateIXPFile() {
        LOGGER.debug("Inside RestServiceImpl.generateIXPFile Method");
        try {
           String fileName = featureFlagProcessor.generateIXPFile();
            return new ResponseEntity<>(fileName, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to generate IXP File: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> updateOfferAttributes(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.updateOfferAttributes Method");
        try {
            //enumUpdateProcessor.updateOfferAttributes(request);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            //return new ResponseEntity<>("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Override
    public ResponseEntity<String> addParentOffers(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.addParentOffers in benefit level");
        try {
            productProcessor.addParentOffers(request);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            //return new ResponseEntity<>("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Override
    public ResponseEntity<String> rebrandingUpdate(ProductUpdateRequest request) {
        try {
            rebrandingRefactor.performRebrandingProcess(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public ResponseEntity<String> associatedProducts(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.associatedProducts Method");
        try {
            productProcessor.updateAssociatedProductInsideOutSide(request);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed : {}" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Map<String, Map<String, List<String>>> > compareAttributesInsideOffer(ProductUpdateRequest request) {
        LOGGER.debug("Inside RestServiceImpl.compareAttributesInsideOffer Method");
        try {
            Map<String, Map<String, List<String>>>  response = productProcessor.compareSearchAbleAttributes(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity("Failed " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
