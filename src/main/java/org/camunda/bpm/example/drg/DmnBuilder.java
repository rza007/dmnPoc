package org.camunda.bpm.example.drg;

import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.*;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class DmnBuilder {
    DmnModelInstance modelInstance;
    TreeMap<String, Types> inputs = new TreeMap<>();
    private final String DateRangeProperty = "DelegationDateRange";

    public DmnBuilder(){
        modelInstance = Dmn.createEmptyModel();
    }

    public String Build(ArrayList<DelegationRule> delegateRule){
        //Collect all input keys. This is required due to the way rules are compared via index.
        //We will need to maintain the order of rules across all rule sets
        for (DelegationRule delegation : delegateRule){
            if (delegation.getStartDate() != null){
                inputs.put(DateRangeProperty, Types.Date);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH:mm:ss");
                if (delegation.getEndDate() != null){
                    delegation.getRules().add(new Rule(DateRangeProperty, delegation.getStartDate().format(formatter), delegation.getEndDate().format(formatter), Operators.Between, Types.Date));
                }
                else
                {
                    delegation.getRules().add(new Rule(DateRangeProperty, delegation.getStartDate().format(formatter), null, Operators.Greater, Types.Date));
                }
            }

            for (Rule rule : delegation.getRules()){
                Types test = inputs.get(rule.getPropertyName());

                if (test != null && test != rule.getType()){
                    throw new IllegalArgumentException("Cannot have two different types for same property name");
                }

                inputs.put(rule.getPropertyName(), rule.getType());
            }
        }

        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setNamespace("http://camunda.org/schema/1.0/dmn");
        definitions.setId("ruletest");
        definitions.setName("Rule Test");
        modelInstance.setDefinitions(definitions);

        Decision decision = createElement(definitions, Decision.class);
        definitions.addChildElement(decision);
        decision.setId("delegate");
        decision.setName("Delegate");

        DecisionTable decisionTable = createElement(decision, DecisionTable.class);
        decisionTable.setHitPolicy(HitPolicy.RULE_ORDER);

        for (Map.Entry<String, Types> entry : inputs.entrySet()) {
            AddInputElement(decisionTable, entry.getValue(), entry.getKey());
        }

        AddOutputElement(decisionTable);

        for (DelegationRule delegation : delegateRule){
            BuildRules(delegation.getRules(), delegation.getOutput(), decisionTable);
        }

        // validate the model
        Dmn.validateModel(modelInstance);

        // convert to string
        return Dmn.convertToString(modelInstance);
    }

    protected void BuildRules(List<Rule> delegateRules, String output, DmnModelElementInstance decisionTable){
        org.camunda.bpm.model.dmn.instance.Rule ruleElement = createElement(decisionTable ,org.camunda.bpm.model.dmn.instance.Rule.class);

        for (Map.Entry<String, Types> entry : inputs.entrySet()) {
            Rule rule = delegateRules.stream().filter(x -> x.getPropertyName().equals(entry.getKey())).findFirst().orElse(null);

            if (rule == null) {
                // Rule doesnt exist for this property we will accept anything if provided
                AddInputClause(ruleElement, Operators.Equal, null, null, null);
            }
            else {
                AddInputClause(ruleElement, rule.getOperator(), rule.getValue(), rule.getValueN(), rule.getType());
            }
        }

        AddOutputClause(ruleElement, output);
    }


    protected void AddInputElement(DmnModelElementInstance parentElement, Types type, String propertyName){
        Input inputClause1 = createElement(parentElement, Input.class);
        inputClause1.setLabel(propertyName);

        InputExpression inputExpression1 = createElement(inputClause1, InputExpression.class);
        inputExpression1.setTypeRef(type.toString().toLowerCase());
        Text x = modelInstance.newInstance(Text.class);
        x.setTextContent(propertyName);
        inputExpression1.setText(x);
    }

    protected void AddOutputElement(DmnModelElementInstance parentElement){
        Output output = createElement(parentElement, Output.class);
        output.setLabel("Result");
        output.setName("result");
        output.setTypeRef("string");
    }

    protected void AddInputClause(DmnModelElementInstance parentElement, Operators operator, String value, String valueN, Types type){
        InputEntry inputEntry1 = createElement(parentElement, InputEntry.class);
        Text inputEntryText = modelInstance.newInstance(Text.class);

        String ruleValue = GetRuleValue(operator, value, valueN, type);

        if (ruleValue != null) {
            inputEntryText.setTextContent(ruleValue);
        }

        inputEntry1.setText(inputEntryText);
    }

    private String GetRuleValue(Operators operator, String value, String valueN, Types type) {
        if (value == null) return null;

        if (operator == Operators.Between && valueN == null){
            throw new IllegalArgumentException("valueN required when creating a between rule");
        }

        switch(type){
            case String:
            case Bool:
            case Int:
            case Long:
            case Double:
                break;
            case Date:
                value =  String.format("date and time(\"%s\")", value);
                if (valueN != null) valueN =  String.format("date and time(\"%s\")", valueN);
        }

        switch(operator){
            case Less:
                return "< " + value;
            case LessOrEqual:
                return "<= " + value;
            case Greater:
                return "> " + value;
            case GreaterOrEqual:
                return ">= " + value;
            case Between:
                return String.format("[%s..%s]", value, valueN);
            case Equal:
            default:
                return value;
        }
    }

    protected void AddOutputClause(DmnModelElementInstance parentElement, String value){
        OutputEntry outputEntry1 = createElement(parentElement ,OutputEntry.class);
        Text outputEntryText = modelInstance.newInstance(Text.class);
        outputEntryText.setTextContent("\"" + value + "\"");
        outputEntry1.setText(outputEntryText);
    }

    protected <T extends DmnModelElementInstance> T createElement(DmnModelElementInstance parentElement, Class<T> elementClass) {
        T element = modelInstance.newInstance(elementClass);

//        if (id != null && !id.isEmpty()){
//            element.setAttributeValue("id", id, true);
//        }

        parentElement.addChildElement(element);
        return element;
    }
}
