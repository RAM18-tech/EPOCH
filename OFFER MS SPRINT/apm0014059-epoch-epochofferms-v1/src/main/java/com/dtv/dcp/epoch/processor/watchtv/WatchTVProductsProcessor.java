package com.dtv.dcp.epoch.processor.watchtv;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;


@Component
public class WatchTVProductsProcessor {

    /** The log. */
    private static Logger log = LoggerFactory.getLogger(WatchTVProductsProcessor.class);

    @Autowired
    CpopClientHelper cpopClientHelper;

    @Value("${apiclient.rest.cpopofferms.ctstate}")
    private String ctstate;

    /**
     *
     * @param productRequestWrapper
     * @return
     */
    public CTProductResponse getProducts(ProductRequestWrapper productRequestWrapper){
       CTProductRequest ctProductRequest = new CTProductRequest();
       try {
            BeanUtils.copyProperties(ctProductRequest, productRequestWrapper.getCtProductRequest());
        } catch (Exception e) {
            log.error(String.format("Error in BeanUtils.copyProperties: %s",e.getMessage()));
        }

        if(productRequestWrapper.getProductRequest()!=null){
            ctProductRequest.setProductCodes(productRequestWrapper.getProductRequest().getProductCodes());
        }
        log.info("CTRequest: {}", ctProductRequest);
               return cpopClientHelper.getProducts(ctProductRequest);
    }
}
