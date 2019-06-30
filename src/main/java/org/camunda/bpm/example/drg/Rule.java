package org.camunda.bpm.example.drg;

public class Rule {
    private String propertyName;
    private String value;
    private String valueN;
    private Operators operator;
    private Types type;

    public Rule(String propertyName, String value, String valueN, Operators operator, Types type) {
        this.propertyName = propertyName;
        this.value = value;
        this.valueN = valueN;
        this.operator = operator;
        this.type = type;
    }

    public Operators getOperator() {
        return operator;
    }

    public void setOperator(Operators operator) {
        this.operator = operator;
    }

    public Types getType() {
        return type;
    }

    public void setType(Types type) {
        this.type = type;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueN() {
        return valueN;
    }

    public void setValueN(String valueN) {
        this.valueN = valueN;
    }
}
