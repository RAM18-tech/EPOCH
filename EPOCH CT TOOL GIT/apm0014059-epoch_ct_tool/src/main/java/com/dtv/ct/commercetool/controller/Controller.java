package com.dtv.ct.commercetool.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dtv.ct.commercetool.model.ProductUpdateRequest;
import com.dtv.ct.commercetool.service.RestService;

@RestController
@RequestMapping("/restservices")
public class Controller {

    @Autowired
    RestService restService;

    @RequestMapping(value = "/ct/update", method = RequestMethod.POST)
    public ResponseEntity<String> ctUpdate(@RequestBody ProductUpdateRequest request) {
        return restService.updateCT(request);
    }

    @RequestMapping(value = "/ct/priceUpdate", method = RequestMethod.POST)
    public ResponseEntity<List<String>> priceUpdate(@RequestBody ProductUpdateRequest request) {
        return restService.priceUpdate(request);
    }

    @RequestMapping(value = "/ct/priceTierUpdate", method = RequestMethod.POST)
    public ResponseEntity<List<String>> priceTierUpdate(@RequestBody ProductUpdateRequest request) {
        return restService.priceTierUpdate(request);
    }

    @RequestMapping(value = "/ct/priceAdd", method = RequestMethod.POST)
    public ResponseEntity<List<String>> priceAdd(@RequestBody ProductUpdateRequest request) {
        return restService.priceAdd(request);
    }

    @RequestMapping(value = "/ct/storeIdUpdate", method = RequestMethod.POST)
    public ResponseEntity<String> storeIdUpdate(@RequestBody ProductUpdateRequest request) {
        return restService.storeIdUpdate(request);
    }

    @RequestMapping(value = "/ct/publishUnpublish", method = RequestMethod.POST)
    public ResponseEntity<String> publishOrUnPublish(@RequestBody ProductUpdateRequest request) {
        return restService.publishOrUnPublish(request);
    }

    @RequestMapping(value = "/ct/multiTextUpdate", method = RequestMethod.POST)
    public ResponseEntity<String> multiTextUpdate(@RequestBody ProductUpdateRequest request) {
        return restService.multiTextUpdate(request);
    }

    @RequestMapping(value = "/ct/findEmptyProdList", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> findEmptyProdList(@RequestBody ProductUpdateRequest request) {
        return restService.findEmptyProdList(request);
    }

    @RequestMapping(value = "/ct/findMissingReferences", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> findMissingReferences(@RequestBody ProductUpdateRequest request) {
        return restService.findMissingReferences(request);
    }

    @RequestMapping(value = "/ct/findExpiredEnDateOfProduct", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> findExpiredEnDateOfProduct(@RequestBody ProductUpdateRequest request) {
        return restService.findExpiredEnDateOfProduct(request);
    }

    @RequestMapping(value = "/ct/story", method = RequestMethod.POST)
    public ResponseEntity<List<StringBuilder>> story(@RequestBody ProductUpdateRequest request) {
        return restService.story(request);
    }

    @RequestMapping(value = "/ct/compare", method = RequestMethod.POST)
    public ResponseEntity<ByteArrayResource> compare(@RequestBody ProductUpdateRequest request) {
        ResponseEntity<Map<String, String>> compareResult = restService.compare(request);
        Map<String, String> resultMap = compareResult.getBody();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (String filePathStr : resultMap.values()) {
                Path filePath = java.nio.file.Paths.get(filePathStr);
                byte[] data = Files.readAllBytes(filePath);
                outputStream.write(data);
            }
            byte[] mergedData = outputStream.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(mergedData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged_result.txt")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(mergedData.length)
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/ct/v2/compare", method = RequestMethod.POST)
    public ResponseEntity<ByteArrayResource> comparevV2(@RequestBody ProductUpdateRequest request) {
        ResponseEntity<Map<String, String>> compareResult = restService.compareV2(request);
        Map<String, String> resultMap = compareResult.getBody();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (String filePathStr : resultMap.values()) {
                Path filePath = java.nio.file.Paths.get(filePathStr);
                byte[] data = Files.readAllBytes(filePath);
                outputStream.write(data);
            }
            byte[] mergedData = outputStream.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(mergedData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged_result.txt")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(mergedData.length)
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/ct/updateSalesChannel", method = RequestMethod.POST)
    public ResponseEntity<String> updateSalesChannel(@RequestBody ProductUpdateRequest request) {
        return restService.updateSalesChannel(request);
    }

    @RequestMapping(value = "/ct/dealerCodes", method = RequestMethod.POST)
    public ResponseEntity<String> dealerCodesUpdate(@RequestBody ProductUpdateRequest request) {
        return restService.dealerCodesUpdate(request);
    }

    @RequestMapping(value = "generateIXPFile", method = RequestMethod.POST)
    public ResponseEntity<String> generateIXPFile() {
        return restService.generateIXPFile();
    }

    @RequestMapping(value = "/ct/updateOfferAttribute", method = RequestMethod.POST)
    public ResponseEntity<String> updateOfferAttributes(@RequestBody ProductUpdateRequest request) {
        return restService.updateOfferAttributes(request);
    }

    @RequestMapping(value = "/ct/addParentOffers", method = RequestMethod.POST)
    public ResponseEntity<String> addParentOffers(@RequestBody ProductUpdateRequest request) {
        return restService.addParentOffers(request);
    }

    @RequestMapping(value = "/ct/rebranding", method = RequestMethod.POST)
    public ResponseEntity<String> rebrading(@RequestBody ProductUpdateRequest request) throws IOException {
        return restService.rebrandingUpdate(request);
    }

    @RequestMapping(value = "/ct/associatedProducts", method = RequestMethod.POST)
    public ResponseEntity<String> associatedProducts(@RequestBody ProductUpdateRequest request) throws IOException {
        return restService.associatedProducts(request);
    }

    @RequestMapping(value = "/ct/compareAttributesInsideOffer", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Map<String, List<String>>>> compareAttributesInsideOffer(@RequestBody ProductUpdateRequest request) throws IOException {
        return restService.compareAttributesInsideOffer(request);
    }
}
