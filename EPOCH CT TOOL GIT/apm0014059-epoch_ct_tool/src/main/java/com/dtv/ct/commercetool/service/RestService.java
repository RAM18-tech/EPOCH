package com.dtv.ct.commercetool.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.dtv.ct.commercetool.model.ProductUpdateRequest;

public interface RestService {

    public ResponseEntity<String> updateCT(ProductUpdateRequest request);

    public ResponseEntity<List<String>> priceUpdate(ProductUpdateRequest request);

    public ResponseEntity<List<String>> priceTierUpdate(ProductUpdateRequest request);

    public ResponseEntity<List<String>> priceAdd(ProductUpdateRequest request);

    public ResponseEntity<String> storeIdUpdate(ProductUpdateRequest request);

    public ResponseEntity<String> publishOrUnPublish(ProductUpdateRequest request);

    public ResponseEntity<Map<String, String>> findEmptyProdList(ProductUpdateRequest request);

    public ResponseEntity<Map<String, String>> findExpiredEnDateOfProduct(ProductUpdateRequest request);

    public ResponseEntity<Map<String, String>> findMissingReferences(ProductUpdateRequest request);

    public ResponseEntity<String> multiTextUpdate(ProductUpdateRequest request);

    public ResponseEntity<List<StringBuilder>> story(ProductUpdateRequest request);

    public ResponseEntity<Map<String, String>> compare(ProductUpdateRequest request);

    public ResponseEntity<Map<String, String>> compareV2(ProductUpdateRequest request);

    public ResponseEntity<String> updateSalesChannel(ProductUpdateRequest request);

    public ResponseEntity<String> dealerCodesUpdate(ProductUpdateRequest request);

    public ResponseEntity<String> generateIXPFile();

    public ResponseEntity<String> updateOfferAttributes(ProductUpdateRequest request);

    public ResponseEntity<String> addParentOffers(ProductUpdateRequest request);

    public ResponseEntity<String> rebrandingUpdate(ProductUpdateRequest request) throws IOException;

    public ResponseEntity<String> associatedProducts(ProductUpdateRequest request);

    public ResponseEntity<Map<String, Map<String, List<String>>> > compareAttributesInsideOffer(ProductUpdateRequest request);
}
