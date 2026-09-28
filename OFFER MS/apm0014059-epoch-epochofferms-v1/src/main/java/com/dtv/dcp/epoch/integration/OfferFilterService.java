package com.dtv.dcp.epoch.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.FieldDetails;
import com.dtv.dcp.epoch.model.ct.offer.GlobalEligibilityRule;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.RedisCacheHelper;;

@Component
public class OfferFilterService {

	private static final Logger log = LoggerFactory.getLogger(OfferFilterService.class);

	@Autowired
	RedisCacheHelper redisCacheHelper;

	@Autowired
	OfferFilterServiceUtil globalExpressionUtil;

	public CTOfferResponse applyFilters(OfferRequest request, CTOfferResponse response) {

		log.info("Inside Filter Service");

		List<CTOffer> filteredResponse= new ArrayList<CTOffer>();

		List<GlobalEligibilityRule> rules=redisCacheHelper.getGlobalEligibilityRules(Constants.OTT);

		filteredResponse=response.getOffers().stream()
				.filter(ctOffer -> rules.stream()
						.allMatch(rule -> {
							boolean isEligibleOffer = false;    									
							if(rule.getPredicate()!=null && !rule.getPredicate().isBlank()) {

								log.debug("Rule: "+rule.toString()+", Request Obj: "+request+", Response Obj: "+ctOffer);
								Map<String, Object> vars=new HashMap<String, Object>();
								rule.getEligibilityAtributes().stream().forEach(attr -> {
									vars.put(attr.getAttributeName().toLowerCase(), Boolean.valueOf(compareFields(attr.getRequestAttributePath(), attr.getResponseAttributePath(), attr.getStaticValue(), request, ctOffer)));
								});

								isEligibleOffer= (Boolean)MVEL.evalToBoolean(rule.getPredicate().toLowerCase(), vars);
								log.debug("Rule: "+rule.toString()+", Result: "+isEligibleOffer);
							}
							return isEligibleOffer;
						})
						)
				.collect(Collectors.toList());
		response.setOffers(filteredResponse.stream().distinct().collect(Collectors.toList()));
		response.setCount(response.getOffers().size());
		response.setTotal(response.getOffers().size());
		return response;

	}

	private boolean compareFields(String requestFieldName, String responseFieldName, String staticValue, Object requestObj, Object responseObj) {
		return compareRequestResponseThruMVEL(requestFieldName, responseFieldName, staticValue, requestObj, responseObj);
	}

	private boolean compareRequestResponseThruMVEL(String requestFieldName, String responseFieldName, String mvelExpression, Object requestObj, Object responseObj) {

		FieldDetails responseField = new FieldDetails(responseObj, responseFieldName);
		FieldDetails requestField = new FieldDetails(requestObj, requestFieldName);
		
		if(responseFieldName!=null && !responseFieldName.isBlank() && responseField.getValue()==null)
		{
			return true;
		}
		else if(requestField.getValue()==null && (responseFieldName==null || responseFieldName.isBlank()))
		{
			return false;
		}
		else {
			Map<String, Object> context=new HashMap<String, Object>();
			context.put("request", requestObj);
			context.put("response", responseObj);
			context.put("globalExpr", globalExpressionUtil);

			Map<String, Object> vars=new HashMap<String, Object>();
			vars.put("match", Boolean.FALSE);
			log.debug("Expression: "+mvelExpression);
			String updatedmvelExpression=mvelExpression.replace("request", "request."+requestFieldName);
			updatedmvelExpression=updatedmvelExpression.replace("response", "response."+responseFieldName);

			log.debug("Updated Expression: "+updatedmvelExpression);
			return (Boolean) MVEL.eval(updatedmvelExpression, context, vars, Boolean.class);
		}
	}
/*
	public enum FieldCompare{
		REQUESTORRESPONSEISNULL,
		REQUESTVALUEISNULL,		
		REQUESTRESPONSEARELIST,
		REQUESTRESPONSEARENOTLIST,
		REQUESTISLIST,
		RESPONSEISLIST,
		REQUESTISNOTLIST
	}
	private boolean compareFields(String requestFieldName, String responseFieldName, String staticValue, Object requestObj, Object responseObj) {
		return (staticValue!=null && staticValue.trim().length()>0)
				?compareRequestWithStaticValue(requestFieldName, staticValue, requestObj)
						:compareRequestResponseFields(requestFieldName, responseFieldName, staticValue, requestObj, responseObj);
	}

	private boolean compareRequestWithStaticValue(String requestFieldName, String staticValue, Object requestObj) {

		FieldDetails requestField = new FieldDetails(requestObj, requestFieldName);

		Map<FieldCompare, FilterRule<Boolean>> filterRules=createRequestStaticValueFilterRules(requestField, staticValue);

		return Stream.of(FieldCompare.REQUESTVALUEISNULL, 
				FieldCompare.REQUESTISLIST, 
				FieldCompare.REQUESTISNOTLIST)
				.filter(compareType -> filterRules.get(compareType).conditon.get())
				.map(compareType -> filterRules.get(compareType).process.get())
				.findFirst()
				.orElse(false);
	}

	private boolean compareRequestResponseFields(String requestFieldName, String responseFieldName, String staticValue, Object requestObj, Object responseObj) {

		FieldDetails responseField = new FieldDetails(responseObj, responseFieldName);
		FieldDetails requestField = new FieldDetails(requestObj, requestFieldName);

		Map<FieldCompare, FilterRule<Boolean>> filterRules=createRequestResponseFilterRules(requestField, responseField);

		return Stream.of(FieldCompare.REQUESTORRESPONSEISNULL,
				FieldCompare.REQUESTRESPONSEARELIST,
				FieldCompare.REQUESTRESPONSEARENOTLIST,
				FieldCompare.REQUESTISLIST,
				FieldCompare.RESPONSEISLIST)
				.filter(compareType -> filterRules.get(compareType).conditon.get())
				.map(compareType -> filterRules.get(compareType).process.get())
				.findFirst()
				.orElse(false);
	}
	private Map<FieldCompare, FilterRule<Boolean>> createRequestStaticValueFilterRules(FieldDetails requestField, String staticValue){
		Map<FieldCompare, FilterRule<Boolean>> rules=new HashMap<OfferFilterService.FieldCompare, FilterRule<Boolean>>();
		rules.put(FieldCompare.REQUESTVALUEISNULL, createFilterRuleForStaticRequestValueNull(requestField));    	
		rules.put(FieldCompare.REQUESTISLIST, createFilterRuleForStaticValueRequestIsList(requestField, staticValue));
		rules.put(FieldCompare.REQUESTISNOTLIST, createFilterRuleForStaticValueRequestIsNotList(requestField, staticValue));
		return rules;
	}

	private Map<FieldCompare, FilterRule<Boolean>> createRequestResponseFilterRules(FieldDetails requestField, FieldDetails responseField){
		Map<FieldCompare, FilterRule<Boolean>> rules=new HashMap<OfferFilterService.FieldCompare, FilterRule<Boolean>>();
		rules.put(FieldCompare.REQUESTORRESPONSEISNULL, createFilterRuleForRequestOrResponseIsNull(requestField, responseField));
		rules.put(FieldCompare.REQUESTRESPONSEARELIST, createFilterRuleForRequestResponseAreList(requestField, responseField));
		rules.put(FieldCompare.REQUESTRESPONSEARENOTLIST, createFilterRuleForRequestResponseAreNotList(requestField, responseField));
		rules.put(FieldCompare.REQUESTISLIST, createFilterRuleForRequestIsList(requestField, responseField));
		rules.put(FieldCompare.RESPONSEISLIST, createFilterRuleForResponseIsList(requestField, responseField));
		return rules;
	}

	public FilterRule<Boolean> createFilterRule(Supplier<Boolean> condition, Supplier<Boolean> process){
		return new FilterRule<>(condition, process);
	}

	FilterRule<Boolean> createFilterRuleForRequestOrResponseIsNull(FieldDetails requestField, FieldDetails responseField){
		return createFilterRule(
				() -> requestField==null || requestField.getValue()==null || responseField==null|| responseField.getValue()==null,
				() -> true
				);
	}

	FilterRule<Boolean> createFilterRuleForStaticRequestValueNull(FieldDetails requestField){
		return createFilterRule(
				() -> requestField==null || requestField.getValue()==null,
				() -> false
				);
	}

	FilterRule<Boolean> createFilterRuleForRequestResponseAreList(FieldDetails requestField, FieldDetails responseField){
		return createFilterRule(
				() -> responseField.getValue()!=null && requestField.getValue()!=null && responseField.getType().equals(requestField.getType()) && responseField.getType().equals(List.class),
				() -> CollectionUtils.isEqualCollection((List<?>)requestField.getValue(), (List<?>)responseField.getValue())
				);
	}

	FilterRule<Boolean> createFilterRuleForRequestResponseAreNotList(FieldDetails requestField, FieldDetails responseField){
		return createFilterRule(
				() -> responseField.getValue()!=null && requestField.getValue()!=null && responseField.getType().equals(requestField.getType()) && !responseField.getType().equals(List.class),
				() -> responseField.getValue().equals(requestField.getValue())
				);
	}

	FilterRule<Boolean> createFilterRuleForResponseIsList(FieldDetails requestField, FieldDetails responseField){
		return createFilterRule(
				() -> responseField.getValue()!=null && requestField.getValue()!=null && responseField.getType().equals(List.class),
				() -> ((List<?>)responseField.getValue()).size()==0 || ((List<?>)responseField.getValue()).contains(requestField.getValue())
				);
	}

	FilterRule<Boolean> createFilterRuleForRequestIsList(FieldDetails requestField, FieldDetails responseField){
		return createFilterRule(
				() -> responseField.getValue()!=null && requestField.getValue()!=null && requestField.getType().equals(List.class),
				() -> ((List<?>)requestField.getValue()).size()==0 || ((List<?>)requestField.getValue()).contains(responseField.getValue())
				);
	}

	FilterRule<Boolean> createFilterRuleForStaticValueRequestIsList(FieldDetails requestField, String staticValue){
		return createFilterRule(
				() -> staticValue!=null && requestField.getValue()!=null && requestField.getType().equals(List.class),
				() -> Arrays.asList(staticValue.split(",")).containsAll((List<?>)requestField.getValue())
				);
	}

	FilterRule<Boolean> createFilterRuleForStaticValueRequestIsNotList(FieldDetails requestField, String staticValue){
		return createFilterRule(
				() -> staticValue!=null && requestField.getValue()!=null && !requestField.getType().equals(List.class),
				() -> Arrays.asList(staticValue.split(",")).contains(requestField.getValue())
				);
	}
*/
}
