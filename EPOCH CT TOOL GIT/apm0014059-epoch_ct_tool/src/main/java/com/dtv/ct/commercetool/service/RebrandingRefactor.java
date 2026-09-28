package com.dtv.ct.commercetool.service;

import com.commercetools.api.models.cart_discount.CartDiscount;
import com.commercetools.api.models.common.LocalizedString;
import com.commercetools.api.models.product.Attribute;
import com.commercetools.api.models.product.AttributeImpl;
import com.commercetools.api.models.product.Product;
import com.commercetools.api.models.product.ProductReferenceImpl;
import com.commercetools.api.models.product_type.*;
import com.commercetools.api.models.type.FieldContainer;
import com.dtv.ct.commercetool.config.ProjectRootConfiguration;
import com.dtv.ct.commercetool.model.ProductUpdateRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RebrandingRefactor {
    @Autowired
    ProjectRootConfiguration projectRoot;

    Workbook workbook = null;
    // List<String> listOfCharacterToSearch = Arrays.asList("HBOMAX", "HBO MAX", "HBO-MAX", "hbomax", "HBO Max", "MAX", "HBO", "hbo", "max");

    public void performRebrandingProcess(ProductUpdateRequest request) throws IOException {
        performRebranding(request.getEnvironment(), request);
        performOfferRebranding(request.getEnvironment(), request);
       // cartDisCountRebranding(request.getEnvironment(), request);
        try (FileOutputStream fileOut = new FileOutputStream("Rebranding_" +request.getEnvironment() + ".xlsx")) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    public static String keyIsAnyOf(List<String> offerKeys) {
        if (offerKeys != null && !offerKeys.isEmpty()) {
            StringJoiner joiner = new StringJoiner("\",\"", "\"", "\"");
            offerKeys.forEach(joiner::add);
            return "key in (" + joiner.toString() + ")";
        }
        return null;
    }

    public static String bundleIsAnyOf(List<String> offerKeys) {
        if (offerKeys != null && !offerKeys.isEmpty()) {
            StringJoiner joiner = new StringJoiner("\",\"", "\"", "\"");
            offerKeys.forEach(joiner::add);
            return "id in (" + joiner.toString() + ")";
        }
        return null;
    }

    private void performRebranding(String environment, ProductUpdateRequest request) throws IOException {
        // Rebranding logic goes here
        List<ProductType> productTypes = projectRoot.getEnvironment(environment).productTypes().get().withLimit(500).executeBlocking().getBody().getResults();

        List<String> listOf3DAttributesKeys = Arrays.asList("display-names-by-key-new", "descriptions-by-key-new", "disclosure-messages-by-key");
        List<String> listOfNon3DAttributesKeys = Arrays.asList("display-names-by-key", "descriptions-by-key", "disclosure-message-by-key");
        workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");
        Row commonRow0 = sheet.createRow(0);
        commonRow0.createCell(0).setCellValue("PRODUCT KEY");

        Row commonRow1 = sheet.createRow(1);
        commonRow1.createCell(0).setCellValue("Name");

        Row commonRow2 = sheet.createRow(2);
        commonRow2.createCell(0).setCellValue("ProductDescription");

        Row commonRow3 = sheet.createRow(3);
        commonRow3.createCell(0).setCellValue("Description (Variant)");

        Row commonRow4 = sheet.createRow(4);
        commonRow4.createCell(0).setCellValue("Product Display Name (Variant)");

        Row commonRow5 = sheet.createRow(5);
        commonRow5.createCell(0).setCellValue("Congrats Message (Variant)");

        Row commonRow6 = sheet.createRow(6);
        commonRow6.createCell(0).setCellValue("OPUS Disclosure Message (Variant)");

        Row commonRow7 = sheet.createRow(7);
        commonRow7.createCell(0).setCellValue("MyATT Disclosure Message (Variant)");

        Row commonRow8 = sheet.createRow(8);
        commonRow8.createCell(0).setCellValue("Dependent Promo Name(Variant)");

        Row commonRow9 = sheet.createRow(9);
        commonRow9.createCell(0).setCellValue("Dependent Promo Display Name(Variant)");

        Row commonRow10 = sheet.createRow(10);
        commonRow10.createCell(0).setCellValue("Dependent Promo Description(Variant)");

        int rowIndex = 11;
        HashMap<String, Map<String, String>> mainAttributeMap = new HashMap<>();
        for (String key : listOf3DAttributesKeys) {
            ProductType productType = productTypes.stream()
                    .filter(pt -> pt.getKey().equals(key))
                    .findFirst()
                    .orElse(null);
            HashMap<String, String> attributeMap = new HashMap<>();
            if (productType != null) {
                // Write product type key
                Row keyRow = sheet.createRow(rowIndex++);
                keyRow.createCell(0).setCellValue(productType.getKey());

                // Write each attribute name below the key
                List<AttributeDefinition> attributes = productType.getAttributes();
                for (AttributeDefinition attr : attributes) {
                    Row attrRow = sheet.createRow(rowIndex++);
                    attrRow.createCell(0).setCellValue(productType.getName() + "->" + attr.getName());
                    attributeMap.put(attr.getName(), null);
                }
                mainAttributeMap.put(productType.getId(), attributeMap);
            }
        }

        for (String key : listOfNon3DAttributesKeys) {
            ProductType productType = productTypes.stream()
                    .filter(pt -> pt.getKey().equals(key))
                    .findFirst()
                    .orElse(null);
            HashMap<String, String> attributeMap = new HashMap<>();
            if (productType != null) {
                // Write product type key
                Row keyRow = sheet.createRow(rowIndex++);
                keyRow.createCell(0).setCellValue(productType.getKey());

                // Write each attribute name below the key
                List<AttributeDefinition> attributes = productType.getAttributes();
                for (AttributeDefinition attr : attributes) {
                    if (attr.getName().equalsIgnoreCase("key")) {
                        AttributeEnumType attributeEnumType = (AttributeEnumType) attr.getType();
                        List<AttributePlainEnumValue> enumValues = attributeEnumType.getValues();
                        for (AttributePlainEnumValue enumValue : enumValues) {
                            Row enumRow = sheet.createRow(rowIndex++);
                            enumRow.createCell(0).setCellValue("Non-3D " + productType.getName() + "->" + enumValue.getKey());
                            attributeMap.put(enumValue.getKey(), null);
                        }
                    }
                }
                mainAttributeMap.put(productType.getId(), attributeMap);
            }
        }
        Map<String, String> globalMsgAttributeMap = new HashMap<>();
        ProductType globalMsgType = productTypes.stream()
                .filter(pt -> pt.getKey().equals("global-messages-by-key"))
                .findFirst()
                .orElse(null);
        List<String> globalMsgAttributeKeys = new ArrayList<>();
        List<String> salesChannelMessageKeys = new ArrayList<>();
        List<String> flowType = new ArrayList<>();
        List<String> msgKey = new ArrayList<>();
        if (globalMsgType != null) {

            AttributeDefinition messageType = globalMsgType.getAttribute("messageType");
            AttributeEnumType attributeEnumType = (AttributeEnumType) messageType.getType();
            List<AttributePlainEnumValue> enumValues = attributeEnumType.getValues();
            for (AttributePlainEnumValue enumValue : enumValues) {
                globalMsgAttributeKeys.add(enumValue.getKey());
            }

            AttributeDefinition allMessages = globalMsgType.getAttribute("allMessages");
            AttributeSetTypeImpl attributeSetType = (AttributeSetTypeImpl) allMessages.getType();
            AttributeNestedTypeImpl nestedType = (AttributeNestedTypeImpl) attributeSetType.getElementType();

            ProductType allMessagesType = productTypes.stream()
                    .filter(pt -> pt.getId().equals(nestedType.getTypeReference().getId()))
                    .findFirst()
                    .orElse(null);

            AttributeDefinition salesChannelForMessages = allMessagesType.getAttribute("salesChannelForMessages");
            AttributeEnumType salesChannelAttributeEnumType = (AttributeEnumType) salesChannelForMessages.getType();
            List<AttributePlainEnumValue> salesChannelEnumValues = salesChannelAttributeEnumType.getValues();
            for (AttributePlainEnumValue enumValue : salesChannelEnumValues) {
                salesChannelMessageKeys.add(enumValue.getKey());
            }

            AttributeDefinition flowTypeForMessages = allMessagesType.getAttribute("flowType");
            AttributeEnumType flowTypeAttributeEnumType = (AttributeEnumType) flowTypeForMessages.getType();
            List<AttributePlainEnumValue> flowTypeEnumValues = flowTypeAttributeEnumType.getValues();
            for (AttributePlainEnumValue enumValue : flowTypeEnumValues) {
                flowType.add(enumValue.getKey());
            }

            AttributeDefinition messageKeyForMessages = allMessagesType.getAttribute("key");
            AttributeEnumType messageKeyAttributeEnumType = (AttributeEnumType) messageKeyForMessages.getType();
            List<AttributePlainEnumValue> messageKeyEnumValues = messageKeyAttributeEnumType.getValues();
            for (AttributePlainEnumValue enumValue : messageKeyEnumValues) {
                msgKey.add(enumValue.getKey());
            }
            for (String globalMsgAttributeKey : globalMsgAttributeKeys) {
                for (String salesChannel : salesChannelMessageKeys) {
                    for (String flowTypeKey : flowType) {
                        for (String messageKey : msgKey) {
                            String combinationKey = salesChannel + flowTypeKey + messageKey;
                            Row enumRow = sheet.createRow(rowIndex++);
                            enumRow.createCell(0).setCellValue("Global Messages->" + globalMsgAttributeKey + combinationKey);
                            globalMsgAttributeMap.put(globalMsgAttributeKey + combinationKey, null);
                        }
                    }
                }
            }
        }


        int productCol = 1;

        List<Product> products = projectRoot.getEnvironment(environment).products().get().withExpand(Arrays.asList("variants[*].attributes[*].value[*]")).withWhere(keyIsAnyOf(request.getOfferCodes())).withLimit(500).executeBlocking().getBody().getResults();
        //list of OTT products
        // List<Product> products = projectRoot.getEnvironment(environment).products().get().withWhere("masterData(current(masterVariant(attributes(name= \"productFamily\"  and value(key in ( \"OTT\" ))))))").withLimit(500).executeBlocking().getBody().getResults();
        for (Product product : products) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ZonedDateTime.class,
                            (com.google.gson.JsonSerializer<ZonedDateTime>) (src, typeOfSrc, context) ->
                                    new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)))
                    .registerTypeAdapter(ZonedDateTime.class,
                            (com.google.gson.JsonDeserializer<ZonedDateTime>) (json, type, context) ->
                                    ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_ZONED_DATE_TIME))
                    .create();
            String productJson = gson.toJson(product);
            // List<String> listOfCharacterToSearch = Arrays.asList("HBOMAX", "HBO MAX", "HBO-MAX", "hbomax", "HBO Max");
            //    if (listOfCharacterToSearch.stream().filter(charStr -> productJson.contains(charStr)).findAny().isPresent()) {
            for (String productTypeKey : mainAttributeMap.keySet()) {
                ProductType productType = productTypes.stream().filter(pt -> pt.getId().equals(product.getProductType().getId())).findFirst().orElse(null);
                String attributeName = getAttributeName(productType, productTypeKey);

                Attribute attribute = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                        .filter(attr -> attr.getName().equalsIgnoreCase(attributeName))
                        .findFirst()
                        .orElse(null);
                HashMap<String, String> attributeMap = new HashMap<>();
                if (attribute == null || attribute.getValue() == null) {
                    mainAttributeMap.put(productTypeKey, attributeMap);
                    continue;
                }
                List<AttributeImpl> nestedAttributes = (List<AttributeImpl>) attribute.getValue();
                if (CollectionUtils.isNotEmpty(nestedAttributes) && nestedAttributes.get(0) instanceof AttributeImpl) {
                    for (AttributeImpl nestedAttr : nestedAttributes) {
                        attributeMap.put(nestedAttr.getName(), nestedAttr.getValue().toString());
                    }
                }

                if (CollectionUtils.isNotEmpty(nestedAttributes) && nestedAttributes.get(0) instanceof List<?>) {
                    for (ArrayList<?> arrayList : (List<ArrayList<?>>) attribute.getValue()) {
                        List<AttributeImpl> innerAttributes = (List<AttributeImpl>) arrayList;
                        String key = "";
                        String value = "";
                        for (AttributeImpl innerAttr : innerAttributes) {
                            if (innerAttr.getName().equalsIgnoreCase("key")) {
                                AttributePlainEnumValue enumValue = (AttributePlainEnumValue) innerAttr.getValue();
                                key = enumValue.getKey();
                            }
                            if (innerAttr.getName().equalsIgnoreCase("value")) {
                                value = (String) innerAttr.getValue();
                            }
                        }
                        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
                            attributeMap.put(key, value);
                        }
                    }
                }
                mainAttributeMap.put(productTypeKey, attributeMap);
            }

            if (!globalMsgAttributeMap.isEmpty()) {
                Attribute globalMessagesByKey = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                        .filter(attr -> attr.getName().equalsIgnoreCase("globalMessagesByKey"))
                        .findFirst()
                        .orElse(null);

                if (globalMessagesByKey != null && globalMessagesByKey.getValue() != null) {
                    List<ArrayList<AttributeImpl>> globalMsgAttributes = (List<ArrayList<AttributeImpl>>) globalMessagesByKey.getValue();
                    for (List<AttributeImpl> innerList : globalMsgAttributes) {
                        String messageTypeKey = null;
                        for (AttributeImpl attr : innerList) {
                            // process attr
                            if (attr.getName().equalsIgnoreCase("messageType")) {
                                AttributePlainEnumValue enumValue = (AttributePlainEnumValue) attr.getValue();
                                messageTypeKey = enumValue.getKey();

                            }

                            if (attr.getName().equalsIgnoreCase("allMessages")) {
                                // This is the list of all messages, we will process it in the next step
                                List<ArrayList<AttributeImpl>> allMessages = (List<ArrayList<AttributeImpl>>) attr.getValue();
                                for (List<AttributeImpl> allMessagesInnerList : allMessages) {
                                    String salesChannel = null;
                                    String flowTypeKey = null;
                                    String messageKey = null;
                                    String value = null;
                                    for (AttributeImpl globalMsgAttribute : allMessagesInnerList) {
                                        if (globalMsgAttribute.getName().equalsIgnoreCase("salesChannelForMessages")) {
                                            AttributePlainEnumValue enumValue = (AttributePlainEnumValue) globalMsgAttribute.getValue();
                                            salesChannel = enumValue.getKey();
                                        }
                                        if (globalMsgAttribute.getName().equalsIgnoreCase("flowType")) {
                                            AttributePlainEnumValue enumValue = (AttributePlainEnumValue) globalMsgAttribute.getValue();
                                            flowTypeKey = enumValue.getKey();
                                        }
                                        if (globalMsgAttribute.getName().equalsIgnoreCase("key")) {
                                            AttributePlainEnumValue enumValue = (AttributePlainEnumValue) globalMsgAttribute.getValue();
                                            messageKey = enumValue.getKey();
                                        }
                                        if (globalMsgAttribute.getName().equalsIgnoreCase("value")) {
                                            value = (String) globalMsgAttribute.getValue();
                                        }

                                    }
                                    String combinationKey = salesChannel + flowTypeKey + messageKey;
                                    if (globalMsgAttributeMap.containsKey(messageTypeKey+combinationKey)) {
                                        globalMsgAttributeMap.put(messageTypeKey+combinationKey, value);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!globalMsgAttributeMap.isEmpty()){
                globalMsgAttributeMap.entrySet().removeIf(entry -> entry.getValue() == null);
                mainAttributeMap.put("global-messages-by-key", globalMsgAttributeMap);
            }

            if (sheet.getRow(0).getCell(productCol) == null) {
                sheet.getRow(0).createCell(productCol).setCellValue(product.getKey());
                if (containsText(product.getMasterData().getCurrent().getName().get("en"))) {
                    sheet.getRow(1).createCell(productCol).setCellValue(product.getMasterData().getCurrent().getName().get("en"));
                }
                if (product.getMasterData().getCurrent().getDescription() != null && containsText(product.getMasterData().getCurrent().getDescription().get("en"))) {
                    sheet.getRow(2).createCell(productCol).setCellValue(product.getMasterData().getCurrent().getDescription().get("en"));
                }
                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attribute -> attribute.getName().equalsIgnoreCase("description"))) {
                    Attribute descAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                            .filter(attr -> attr.getName().equalsIgnoreCase("description"))
                            .findFirst()
                            .orElse(null);
                    LocalizedString localizedString = (LocalizedString) descAttr.getValue();
                    if (descAttr != null && descAttr.getValue() != null && containsText(descAttr.getValue().toString())) {
                        sheet.getRow(3).createCell(productCol).setCellValue(localizedString.get("en"));
                    }
                }
                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attribute -> attribute.getName().equalsIgnoreCase("displayName"))) {
                    Attribute descAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                            .filter(attr -> attr.getName().equalsIgnoreCase("displayName"))
                            .findFirst()
                            .orElse(null);
                    LocalizedString localizedString = (LocalizedString) descAttr.getValue();
                    if (descAttr != null && descAttr.getValue() != null && containsText(descAttr.getValue().toString())) {
                        sheet.getRow(4).createCell(productCol).setCellValue(localizedString.get("en"));
                    }
                }
                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attribute -> attribute.getName().equalsIgnoreCase(("congratsMessage")))) {
                    Attribute congratsAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                            .filter(attr -> attr.getName().equalsIgnoreCase("congratsMessage"))
                            .findFirst()
                            .orElse(null);
                    if (congratsAttr != null && congratsAttr.getValue() != null && containsText(congratsAttr.getValue().toString())) {
                        sheet.getRow(5).createCell(productCol).setCellValue(congratsAttr.getValue().toString());
                    }
                }
                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attribute -> attribute.getName().equalsIgnoreCase(("opusDisclosureMessage")))) {
                    Attribute congratsAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                            .filter(attr -> attr.getName().equalsIgnoreCase("opusDisclosureMessage"))
                            .findFirst()
                            .orElse(null);
                    if (congratsAttr != null && congratsAttr.getValue() != null && containsText(congratsAttr.getValue().toString())) {
                        sheet.getRow(6).createCell(productCol).setCellValue(congratsAttr.getValue().toString());
                    }
                }

                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attribute -> attribute.getName().equalsIgnoreCase(("myAttDisclosureMessage")))) {
                    Attribute congratsAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                            .filter(attr -> attr.getName().equalsIgnoreCase("myAttDisclosureMessage"))
                            .findFirst()
                            .orElse(null);
                    if (congratsAttr != null && congratsAttr.getValue() != null && containsText(congratsAttr.getValue().toString())) {
                        sheet.getRow(7).createCell(productCol).setCellValue(congratsAttr.getValue().toString());
                    }
                }

                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attribute -> attribute.getName().equalsIgnoreCase(("dependentPromoName")))) {
                    Attribute congratsAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                            .filter(attr -> attr.getName().equalsIgnoreCase("dependentPromoName"))
                            .findFirst()
                            .orElse(null);
                    if (congratsAttr != null && congratsAttr.getValue() != null && containsText(congratsAttr.getValue().toString())) {
                        sheet.getRow(8).createCell(productCol).setCellValue(congratsAttr.getValue().toString());
                    }
                }

                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attribute -> attribute.getName().equalsIgnoreCase(("dependentPromoDisplayName")))) {
                    Attribute congratsAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                            .filter(attr -> attr.getName().equalsIgnoreCase("dependentPromoDisplayName"))
                            .findFirst()
                            .orElse(null);
                    if (congratsAttr != null && congratsAttr.getValue() != null && containsText(congratsAttr.getValue().toString())) {
                        sheet.getRow(9).createCell(productCol).setCellValue(congratsAttr.getValue().toString());
                    }
                }

                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attribute -> attribute.getName().equalsIgnoreCase(("dependentPromoDisplayName")))) {
                    Attribute congratsAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                            .filter(attr -> attr.getName().equalsIgnoreCase("dependentPromoDisplayName"))
                            .findFirst()
                            .orElse(null);
                    if (congratsAttr != null && congratsAttr.getValue() != null && containsText(congratsAttr.getValue().toString())) {
                        sheet.getRow(10).createCell(productCol).setCellValue(congratsAttr.getValue().toString());
                    }
                }

                if (product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream().anyMatch(attribute -> attribute.getName().equalsIgnoreCase(("dependentPromoDescription")))) {
                    Attribute congratsAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                            .filter(attr -> attr.getName().equalsIgnoreCase("dependentPromoDescription"))
                            .findFirst()
                            .orElse(null);
                    if (congratsAttr != null && congratsAttr.getValue() != null && containsText(congratsAttr.getValue().toString())) {
                        sheet.getRow(11).createCell(productCol).setCellValue(congratsAttr.getValue().toString());
                    }
                }
            }

            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String keyInA = row.getCell(0).getStringCellValue();

                // Iterate over all inner maps in mainAttributeMap
                for (Map<String, String> innerMap : mainAttributeMap.values()) {
                    String innerMapKey = innerMap.keySet().stream()
                            .filter(k -> keyInA.endsWith("->" + k))
                            .findFirst()
                            .orElse(null);
                    if (innerMapKey != null && innerMap.get(innerMapKey) != null) {
                        String value = innerMap.get(innerMapKey);
                        // if (listOfCharacterToSearch.stream().filter(charStr -> value.contains(charStr)).findAny().isPresent()) {
                        row.createCell(productCol).setCellValue(value != null ? value : "");
                        //}
                    }
                }
            }
            productCol++;
            //}
        }

        for (int i = sheet.getLastRowNum(); i >= 3; i--) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            boolean isEmpty = true;
            for (int j = 1; j < row.getLastCellNum(); j++) {
                if (row.getCell(j) != null && row.getCell(j).getCellType() != org.apache.poi.ss.usermodel.CellType.BLANK
                        && !row.getCell(j).toString().trim().isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                for (int j = 1; j < row.getLastCellNum(); j++) {
                    row.removeCell(row.getCell(j));
                }
                sheet.removeRow(row);
                if (i + 1 <= sheet.getLastRowNum()) {
                    sheet.shiftRows(i + 1, sheet.getLastRowNum(), -1);
                }
            }
        }


        try (FileOutputStream fileOut = new FileOutputStream("Rebranding_" +request.getEnvironment() + ".xlsx")) {
            workbook.write(fileOut);
        }


        ///workbook.close();

    }

    public String getAttributeName(ProductType productType, String productTypeKey) {
        for (AttributeDefinition attrDef : productType.getAttributes()) {
            if (attrDef.getType() instanceof AttributeNestedTypeImpl) {
                AttributeNestedTypeImpl attributeNestedType = (AttributeNestedTypeImpl) attrDef.getType();
                if (attributeNestedType.getTypeReference().getId().equalsIgnoreCase(productTypeKey)) {
                    return attrDef.getName();
                }
            } else if (attrDef.getType() instanceof AttributeSetTypeImpl) {
                AttributeSetTypeImpl attributeSetType = (AttributeSetTypeImpl) attrDef.getType();
                if (attributeSetType.getElementType() instanceof AttributeNestedTypeImpl) {
                    AttributeNestedTypeImpl nestedType = (AttributeNestedTypeImpl) attributeSetType.getElementType();
                    if (nestedType.getTypeReference().getId().equalsIgnoreCase(productTypeKey)) {
                        return attrDef.getName();
                    }
                }
            }
        }
        return null;
    }

    private void performOfferRebranding(String environment, ProductUpdateRequest request) throws IOException {
        // Rebranding logic goes here
        List<ProductType> productTypes = projectRoot.getEnvironment(environment).productTypes().get().withLimit(500).executeBlocking().getBody().getResults();

        List<String> listOf3DAttributesKeys = Arrays.asList("display-names-by-key-new", "descriptions-by-key-new", "disclosure-messages-by-key");
        List<String> listOfNon3DAttributesKeys = Arrays.asList("display-names-by-key", "descriptions-by-key", "disclosure-message-by-key");
        //Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("OFFERS");
        Row commonRow1 = sheet.createRow(0);
        commonRow1.createCell(0).setCellValue("PRODUCT KEY");

        Row commonRow2 = sheet.createRow(1);
        commonRow2.createCell(0).setCellValue("Name");

        Row commonRow3 = sheet.createRow(2);
        commonRow3.createCell(0).setCellValue("ProductDescription");

        Row commonRow4 = sheet.createRow(3);
        commonRow4.createCell(0).setCellValue("opusDisclosureMessage");

        Row commonRow5 = sheet.createRow(4);
        commonRow5.createCell(0).setCellValue("myAttDisclosureMessage");
        int rowIndex = 5;
        HashMap<String, Map<String, String>> mainAttributeMap = new HashMap<>();
        for (String key : listOf3DAttributesKeys) {
            ProductType productType = productTypes.stream()
                    .filter(pt -> pt.getKey().equals(key))
                    .findFirst()
                    .orElse(null);
            HashMap<String, String> attributeMap = new HashMap<>();
            if (productType != null) {
                // Write product type key
                Row keyRow = sheet.createRow(rowIndex++);
                keyRow.createCell(0).setCellValue(productType.getKey());

                // Write each attribute name below the key
                List<AttributeDefinition> attributes = productType.getAttributes();
                for (AttributeDefinition attr : attributes) {
                    Row attrRow = sheet.createRow(rowIndex++);
                    attrRow.createCell(0).setCellValue(productType.getName() + "->" + attr.getName());
                    attributeMap.put(attr.getName(), null);
                }
                mainAttributeMap.put(productType.getId(), attributeMap);
            }
        }

        for (String key : listOfNon3DAttributesKeys) {
            ProductType productType = productTypes.stream()
                    .filter(pt -> pt.getKey().equals(key))
                    .findFirst()
                    .orElse(null);
            HashMap<String, String> attributeMap = new HashMap<>();
            if (productType != null) {
                // Write product type key
                Row keyRow = sheet.createRow(rowIndex++);
                keyRow.createCell(0).setCellValue(productType.getKey());

                // Write each attribute name below the key
                List<AttributeDefinition> attributes = productType.getAttributes();
                for (AttributeDefinition attr : attributes) {
                    if (attr.getName().equalsIgnoreCase("key")) {
                        AttributeEnumType attributeEnumType = (AttributeEnumType) attr.getType();
                        List<AttributePlainEnumValue> enumValues = attributeEnumType.getValues();
                        for (AttributePlainEnumValue enumValue : enumValues) {
                            Row enumRow = sheet.createRow(rowIndex++);
                            enumRow.createCell(0).setCellValue(productType.getName() + "->" + enumValue.getKey());
                            attributeMap.put(enumValue.getKey(), null);
                        }
                    }
                }
                mainAttributeMap.put(productType.getId(), attributeMap);
            }
        }

        Map<String, String> globalMsgAttributeMap = new HashMap<>();
        ProductType globalMsgType = productTypes.stream()
                .filter(pt -> pt.getKey().equals("global-messages-by-key"))
                .findFirst()
                .orElse(null);
        List<String> globalMsgAttributeKeys = new ArrayList<>();
        List<String> salesChannelMessageKeys = new ArrayList<>();
        List<String> flowType = new ArrayList<>();
        List<String> msgKey = new ArrayList<>();
        if (globalMsgType != null) {

            AttributeDefinition messageType = globalMsgType.getAttribute("messageType");
            AttributeEnumType attributeEnumType = (AttributeEnumType) messageType.getType();
            List<AttributePlainEnumValue> enumValues = attributeEnumType.getValues();
            for (AttributePlainEnumValue enumValue : enumValues) {
                globalMsgAttributeKeys.add(enumValue.getKey());
            }

            AttributeDefinition allMessages = globalMsgType.getAttribute("allMessages");
            AttributeSetTypeImpl attributeSetType = (AttributeSetTypeImpl) allMessages.getType();
            AttributeNestedTypeImpl nestedType = (AttributeNestedTypeImpl) attributeSetType.getElementType();

            ProductType allMessagesType = productTypes.stream()
                    .filter(pt -> pt.getId().equals(nestedType.getTypeReference().getId()))
                    .findFirst()
                    .orElse(null);

            AttributeDefinition salesChannelForMessages = allMessagesType.getAttribute("salesChannelForMessages");
            AttributeEnumType salesChannelAttributeEnumType = (AttributeEnumType) salesChannelForMessages.getType();
            List<AttributePlainEnumValue> salesChannelEnumValues = salesChannelAttributeEnumType.getValues();
            for (AttributePlainEnumValue enumValue : salesChannelEnumValues) {
                salesChannelMessageKeys.add(enumValue.getKey());
            }

            AttributeDefinition flowTypeForMessages = allMessagesType.getAttribute("flowType");
            AttributeEnumType flowTypeAttributeEnumType = (AttributeEnumType) flowTypeForMessages.getType();
            List<AttributePlainEnumValue> flowTypeEnumValues = flowTypeAttributeEnumType.getValues();
            for (AttributePlainEnumValue enumValue : flowTypeEnumValues) {
                flowType.add(enumValue.getKey());
            }

            AttributeDefinition messageKeyForMessages = allMessagesType.getAttribute("key");
            AttributeEnumType messageKeyAttributeEnumType = (AttributeEnumType) messageKeyForMessages.getType();
            List<AttributePlainEnumValue> messageKeyEnumValues = messageKeyAttributeEnumType.getValues();
            for (AttributePlainEnumValue enumValue : messageKeyEnumValues) {
                msgKey.add(enumValue.getKey());
            }
            for (String globalMsgAttributeKey : globalMsgAttributeKeys) {
                for (String salesChannel : salesChannelMessageKeys) {
                    for (String flowTypeKey : flowType) {
                        for (String messageKey : msgKey) {
                            String combinationKey = salesChannel + flowTypeKey + messageKey;
                            Row enumRow = sheet.createRow(rowIndex++);
                            enumRow.createCell(0).setCellValue("Global Messages->" + globalMsgAttributeKey + combinationKey);
                            globalMsgAttributeMap.put(globalMsgAttributeKey + combinationKey, null);
                        }
                    }
                }
            }
        }


        int productCol = 1;
        List<Product> products = projectRoot.getEnvironment(environment).products().get().withWhere(keyIsAnyOf(request.getOfferCodes())).withLimit(500).executeBlocking().getBody().getResults();
        List<String> prodIds = products.stream().map(Product::getId).toList();
        //list of OTT products
        //Long totalProducts = projectRoot.getEnvironment(environment).products().get().withWhere("masterData(current(masterVariant(attributes(name= \"offerProductFamily\"  and value(key in ( \"OTT\" ))))))").executeBlocking().getBody().getTotal();
        Long totalProducts = projectRoot.getEnvironment(environment).products().get().withWhere("masterData(current(masterVariant(attributes(name= \"bundleProductIds\"  and value(" + bundleIsAnyOf(prodIds) + ")))))").executeBlocking().getBody().getTotal();
        List<Product> offerProducts = new ArrayList<>();
        int offset = 0;
        while (offset < totalProducts) {
            List<Product> productsBatch = projectRoot.getEnvironment(environment).products().get()
                    // .withWhere("masterData(current(masterVariant(attributes(name= \"offerProductFamily\"  and value(key in ( \"OTT\" ))))))")
                    .withWhere("masterData(current(masterVariant(attributes(name= \"bundleProductIds\"  and value(" + bundleIsAnyOf(prodIds) + ")))))")
                    .withLimit(500)
                    .withOffset(offset)
                    .executeBlocking()
                    .getBody()
                    .getResults();
            offerProducts.addAll(productsBatch);
            offset += 500;
        }
        //List<Product> products = projectRoot.getEnvironment(environment).products().get().withWhere("masterData(current(masterVariant(attributes(name= \"offerProductFamily\"  and value(key in ( \"OTT\" ))))))").withLimit(500).executeBlocking().getBody().getResults();
        for (Product product : offerProducts) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ZonedDateTime.class,
                            (com.google.gson.JsonSerializer<ZonedDateTime>) (src, typeOfSrc, context) ->
                                    new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)))
                    .registerTypeAdapter(ZonedDateTime.class,
                            (com.google.gson.JsonDeserializer<ZonedDateTime>) (json, type, context) ->
                                    ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_ZONED_DATE_TIME))
                    .create();
            String productJson = gson.toJson(product);

            // if (listOfCharacterToSearch.stream().filter(charStr -> productJson.contains(charStr)).findAny().isPresent()) {
            for (String productTypeKey : mainAttributeMap.keySet()) {
                ProductType productType = productTypes.stream().filter(pt -> pt.getId().equals(product.getProductType().getId())).findFirst().orElse(null);
                String attributeName = getAttributeName(productType, productTypeKey);

                Attribute attribute = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                        .filter(attr -> attr.getName().equalsIgnoreCase(attributeName))
                        .findFirst()
                        .orElse(null);

                HashMap<String, String> attributeMap = new HashMap<>();
                if (attribute == null || attribute.getValue() == null) {
                    mainAttributeMap.put(productTypeKey, attributeMap);
                    continue;
                }
                List<AttributeImpl> nestedAttributes = (List<AttributeImpl>) attribute.getValue();
                if (CollectionUtils.isNotEmpty(nestedAttributes) && nestedAttributes.get(0) instanceof AttributeImpl) {
                    for (AttributeImpl nestedAttr : nestedAttributes) {
                        attributeMap.put(nestedAttr.getName(), nestedAttr.getValue().toString());
                    }
                }
                if (CollectionUtils.isNotEmpty(nestedAttributes) && nestedAttributes.get(0) instanceof List<?>) {
                    for (ArrayList<?> arrayList : (List<ArrayList<?>>) attribute.getValue()) {
                        List<AttributeImpl> innerAttributes = (List<AttributeImpl>) arrayList;
                        String key = "";
                        String value = "";
                        for (AttributeImpl innerAttr : innerAttributes) {
                            if (innerAttr.getName().equalsIgnoreCase("key")) {
                                AttributePlainEnumValue enumValue = (AttributePlainEnumValue) innerAttr.getValue();
                                key = enumValue.getKey();
                            }
                            if (innerAttr.getName().equalsIgnoreCase("value")) {
                                value = (String) innerAttr.getValue();
                            }
                        }
                        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
                            attributeMap.put(key, value);
                        }
                    }
                }

                mainAttributeMap.put(productTypeKey, attributeMap);
            }


            if (!globalMsgAttributeMap.isEmpty()) {
                Attribute globalMessagesByKey = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                        .filter(attr -> attr.getName().equalsIgnoreCase("globalMessagesByKey"))
                        .findFirst()
                        .orElse(null);

                if (globalMessagesByKey != null && globalMessagesByKey.getValue() != null) {
                    List<ArrayList<AttributeImpl>> globalMsgAttributes = (List<ArrayList<AttributeImpl>>) globalMessagesByKey.getValue();
                    for (List<AttributeImpl> innerList : globalMsgAttributes) {
                        String messageTypeKey = null;
                        for (AttributeImpl attr : innerList) {
                            // process attr
                            if (attr.getName().equalsIgnoreCase("messageType")) {
                                AttributePlainEnumValue enumValue = (AttributePlainEnumValue) attr.getValue();
                                messageTypeKey = enumValue.getKey();

                            }

                            if (attr.getName().equalsIgnoreCase("allMessages")) {
                                // This is the list of all messages, we will process it in the next step
                                List<ArrayList<AttributeImpl>> allMessages = (List<ArrayList<AttributeImpl>>) attr.getValue();
                                for (List<AttributeImpl> allMessagesInnerList : allMessages) {
                                    String salesChannel = null;
                                    String flowTypeKey = null;
                                    String messageKey = null;
                                    String value = null;
                                    for (AttributeImpl globalMsgAttribute : allMessagesInnerList) {
                                        if (globalMsgAttribute.getName().equalsIgnoreCase("salesChannelForMessages")) {
                                            AttributePlainEnumValue enumValue = (AttributePlainEnumValue) globalMsgAttribute.getValue();
                                            salesChannel = enumValue.getKey();
                                        }
                                        if (globalMsgAttribute.getName().equalsIgnoreCase("flowType")) {
                                            AttributePlainEnumValue enumValue = (AttributePlainEnumValue) globalMsgAttribute.getValue();
                                            flowTypeKey = enumValue.getKey();
                                        }
                                        if (globalMsgAttribute.getName().equalsIgnoreCase("key")) {
                                            AttributePlainEnumValue enumValue = (AttributePlainEnumValue) globalMsgAttribute.getValue();
                                            messageKey = enumValue.getKey();
                                        }
                                        if (globalMsgAttribute.getName().equalsIgnoreCase("value")) {
                                            value = (String) globalMsgAttribute.getValue();
                                        }

                                    }
                                    String combinationKey = salesChannel + flowTypeKey + messageKey;
                                    if (globalMsgAttributeMap.containsKey(messageTypeKey+combinationKey)) {
                                        globalMsgAttributeMap.put(messageTypeKey+combinationKey, value);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!globalMsgAttributeMap.isEmpty()){
                globalMsgAttributeMap.entrySet().removeIf(entry -> entry.getValue() == null);
                mainAttributeMap.put("global-messages-by-key", globalMsgAttributeMap);
            }

            if (sheet.getRow(0).getCell(productCol) == null) {
                sheet.getRow(0).createCell(productCol).setCellValue(product.getKey());
                if (containsText(product.getMasterData().getCurrent().getName().get("en"))) {
                    sheet.getRow(1).createCell(productCol).setCellValue(product.getMasterData().getCurrent().getName().get("en"));
                }
                if (product.getMasterData().getCurrent().getDescription() != null && containsText(product.getMasterData().getCurrent().getDescription().get("en"))) {
                    sheet.getRow(2).createCell(productCol).setCellValue(product.getMasterData().getCurrent().getDescription().get("en"));
                }

                Attribute opusDisclosureAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                        .filter(attr -> attr.getName().equalsIgnoreCase("opusDisclosureMessage"))
                        .findFirst()
                        .orElse(null);
                if (opusDisclosureAttr != null && opusDisclosureAttr.getValue() != null && containsText(opusDisclosureAttr.getValue().toString())) {
                    sheet.getRow(3).createCell(productCol).setCellValue(opusDisclosureAttr.getValue().toString());
                }

                Attribute myAttDisclosureAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                        .filter(attr -> attr.getName().equalsIgnoreCase("myAttDisclosureMessage"))
                        .findFirst()
                        .orElse(null);

                if (myAttDisclosureAttr != null && myAttDisclosureAttr.getValue() != null && containsText(myAttDisclosureAttr.getValue().toString())) {
                    sheet.getRow(4).createCell(productCol).setCellValue(myAttDisclosureAttr.getValue().toString());
                }
            }

            for (int i = 5; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String keyInA = row.getCell(0).getStringCellValue();

                // Iterate over all inner maps in mainAttributeMap
                for (Map<String, String> innerMap : mainAttributeMap.values()) {
                    String innerMapKey = innerMap.keySet().stream()
                            .filter(k -> keyInA.endsWith("->" + k))
                            .findFirst()
                            .orElse(null);
                    if (innerMapKey != null && innerMap.get(innerMapKey) != null) {
                        String value = innerMap.get(innerMapKey);
                        //  if (listOfCharacterToSearch.stream().filter(charStr -> value.contains(charStr)).findAny().isPresent()) {
                        row.createCell(productCol).setCellValue(value != null ? value : "");
                        //}
                    }
                }
            }

            Attribute choiceGroupAttr = product.getMasterData().getCurrent().getMasterVariant().getAttributes().stream()
                    .filter(attr -> attr.getName().equalsIgnoreCase("offerChoiceGroup"))
                    .findFirst()
                    .orElse(null);
            HashMap<String, String> choiceMap = null;
            if (choiceGroupAttr != null && choiceGroupAttr.getValue() != null) {
                choiceMap = new HashMap<>();
                int choiceIndex = 0;
                for (ArrayList<?> arrayList : (List<ArrayList<?>>) choiceGroupAttr.getValue()) {
                    List<AttributeImpl> innerAttributes = (List<AttributeImpl>) arrayList;
                    for (AttributeImpl innerAttr : innerAttributes) {
                        if (innerAttr.getName().equalsIgnoreCase("disclosureMessagesByKey")) {
                            List<AttributeImpl> disclosureAttributes = (List<AttributeImpl>) innerAttr.getValue();
                            for (AttributeImpl disclosureAttr : disclosureAttributes) {
                                String name = "OfferChoiceGroup[" + choiceIndex + "] -> disclosureMessagesByKey" + "->" + disclosureAttr.getName();
                                String value = (String) disclosureAttr.getValue();
                                choiceMap.put(name, value);
                            }
                        }
                    }
                    choiceIndex++;
                }

                //first add the keys in A column if they don't exist
                for (String choiceKey : choiceMap.keySet()) {
                    boolean keyExists = false;
                    for (int i = 5; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;
                        String keyInA = row.getCell(0).getStringCellValue();
                        if (keyInA.equalsIgnoreCase(choiceKey)) {
                            keyExists = true;
                            break;
                        }
                    }
                    if (!keyExists) {
                        Row newRow = sheet.createRow(sheet.getLastRowNum() + 1);
                        newRow.createCell(0).setCellValue(choiceKey);
                    }
                }


                for (int i = 5; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    String keyInA = row.getCell(0).getStringCellValue();

                    // Iterate over all inner maps in choiceMap
                    for (String choiceKey : choiceMap.keySet()) {
                        if (keyInA.equalsIgnoreCase(choiceKey)) {
                            String value = choiceMap.get(choiceKey);
                            //   if (listOfCharacterToSearch.stream().filter(charStr -> value.contains(charStr)).findAny().isPresent()) {
                            row.createCell(productCol).setCellValue(value != null ? value : "");
                            // }
                        }
                    }
                }

            }
            productCol++;

            // }
        }
        for (int i = sheet.getLastRowNum(); i >= 3; i--) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            boolean isEmpty = true;
            for (int j = 1; j < row.getLastCellNum(); j++) {
                if (row.getCell(j) != null && row.getCell(j).getCellType() != org.apache.poi.ss.usermodel.CellType.BLANK
                        && !row.getCell(j).toString().trim().isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                for (int j = 1; j < row.getLastCellNum(); j++) {
                    row.removeCell(row.getCell(j));
                }
                sheet.removeRow(row);
                if (i + 1 <= sheet.getLastRowNum()) {
                    sheet.shiftRows(i + 1, sheet.getLastRowNum(), -1);
                }
            }
        }

        System.out.println("Offer Rebranding completed successfully.");

    }

    public boolean containsText(String value) {
        return true;
        //return listOfCharacterToSearch.stream().anyMatch(value::contains);
    }

    private void cartDisCountRebranding(String environment, ProductUpdateRequest request) {
//        if (workbook == null) {
//            workbook = new XSSFWorkbook();
//        } else {
//            Sheet existingSheet = workbook.getSheet("BENEFITS");
//            if (workbook.getNumberOfSheets() > 0 && existingSheet != null) {
//                int index = workbook.getSheetIndex(existingSheet);
//                workbook.removeSheetAt(index);
//            }
//        }
        Sheet sheet = workbook.createSheet("BENEFITS");
        Row commonRow1 = sheet.createRow(0);
        commonRow1.createCell(0).setCellValue("KEY");

        Row commonRow2 = sheet.createRow(1);
        commonRow2.createCell(0).setCellValue("Name");

        Row commonRow3 = sheet.createRow(2);
        commonRow3.createCell(0).setCellValue("Description");
        List<Product> products = projectRoot.getEnvironment(environment).products().get().withWhere(keyIsAnyOf(request.getOfferCodes())).withLimit(500).executeBlocking().getBody().getResults();
        List<String> prodIds = products.stream().map(Product::getId).toList();

        int rowIndex = 3;
        Long totalCartDiscounts = projectRoot.getEnvironment(environment).cartDiscounts().get().withWhere("isActive = true and custom(fields(beneficiaryProductFamily in (\"OTT\")))").executeBlocking().getBody().getTotal();
        List<CartDiscount> cartDiscounts = new ArrayList<>();
        int offset = 0;
        while (offset < totalCartDiscounts) {
            List<CartDiscount> cartDiscountsBatch = projectRoot.getEnvironment(environment).cartDiscounts().get()
                    .withLimit(500)
                    .withOffset(offset)
                    // .withWhere(keyIsAnyOf(request.getBenefitCodes()))
                    .withWhere("isActive = true and custom(fields(beneficiaryProductFamily in (\"OTT\")))")
                    .executeBlocking()
                    .getBody()
                    .getResults();
            cartDiscounts.addAll(cartDiscountsBatch);
            offset += 500;
        }
        int cartDiscountCol = 1;
        for (CartDiscount cartDiscount : cartDiscounts) {
            Map<String, Object> customFieldsMap = cartDiscount.getCustom().getFields().values();
            List<ProductReferenceImpl> applicablePrd = (List<ProductReferenceImpl>) customFieldsMap.get("applicableProducts");
            List<String> applicablePrdIds = applicablePrd != null ? applicablePrd.stream().filter(Objects::nonNull).map(ProductReferenceImpl::getId).toList() : new ArrayList<>();

            if (!CollectionUtils.containsAny(applicablePrdIds, prodIds)) {
                continue;
            }
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ZonedDateTime.class,
                            (com.google.gson.JsonSerializer<ZonedDateTime>) (src, typeOfSrc, context) ->
                                    new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)))
                    .registerTypeAdapter(ZonedDateTime.class,
                            (com.google.gson.JsonDeserializer<ZonedDateTime>) (json, type, context) ->
                                    ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_ZONED_DATE_TIME))
                    .create();
            String cartDiscountJson = gson.toJson(cartDiscount);

            //   if (listOfCharacterToSearch.stream().filter(charStr -> cartDiscountJson.contains(charStr)).findAny().isPresent()) {
            sheet.getRow(0).createCell(cartDiscountCol).setCellValue(cartDiscount.getKey());
            if (containsText(cartDiscount.getName().get("en"))) {
                sheet.getRow(1).createCell(cartDiscountCol).setCellValue(cartDiscount.getName().get("en"));
            }
            if (cartDiscount.getDescription() != null && containsText(cartDiscount.getDescription().get("en"))) {
                sheet.getRow(2).createCell(cartDiscountCol).setCellValue(cartDiscount.getDescription().get("en"));
            }
            HashMap<String, String> attributeMap = new HashMap<>();
            // if (listOfCharacterToSearch.stream().filter(charStr -> cartDiscountJson.contains(charStr)).findAny().isPresent()) {

            FieldContainer fieldContainer = cartDiscount.getCustom().getFields();
            Map<String, Object> fieldMap = fieldContainer.values();

            for (String key : fieldMap.keySet()) {
                Object value = fieldMap.get(key);
                if (value instanceof String) {
                    attributeMap.put(key, (String) value);
                }
            }
            //}

            //first add the keys in A column if they don't exist
            // Collect all existing keys in column A
            Set<String> existingKeys = new HashSet<>();
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String keyInA = row.getCell(0).getStringCellValue();
                existingKeys.add(keyInA);
            }

            // Add only missing attributeKeys
            for (String attributeKey : attributeMap.keySet()) {
                if (!existingKeys.contains(attributeKey)) {
                    Row newRow = sheet.createRow(sheet.getLastRowNum() + 1);
                    newRow.createCell(0).setCellValue(attributeKey);
                }
            }


            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String keyInA = row.getCell(0).getStringCellValue();

                // Iterate over all inner maps in attributeMap
                for (String attributeKey : attributeMap.keySet()) {
                    if (keyInA.equalsIgnoreCase(attributeKey)) {
                        String value = attributeMap.get(attributeKey);
                        //if (listOfCharacterToSearch.stream().filter(charStr -> value.contains(charStr)).findAny().isPresent()) {
                        row.createCell(cartDiscountCol).setCellValue(value != null ? value : "");
                        //}
                    }
                }
            }
            cartDiscountCol++;
        }
        //  }
    }
}
