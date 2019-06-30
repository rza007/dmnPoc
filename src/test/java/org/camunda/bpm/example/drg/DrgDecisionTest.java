/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.example.drg;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.test.DmnEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class DrgDecisionTest {

    @Rule
    public DmnEngineRule dmnEngineRule = new DmnEngineRule();

    public DmnEngine dmnEngine;
    public DmnDecision decision;

    @Before
    public void parseDecision() {
        InputStream inputStream = DrgDecisionTest.class
                .getResourceAsStream("dinnerDecisions.dmn");
        dmnEngine = dmnEngineRule.getDmnEngine();

        DmnBuilder builder = new DmnBuilder();

        String xmlString = builder.Build(CreateRules());
        System.out.println(xmlString);

        InputStream targetStream = new ByteArrayInputStream(xmlString.getBytes());

        decision = dmnEngine.parseDecision("delegate", targetStream);
    }

    @Test
    public void TestCase1() {
        // All variables specified as inputs must be supplied here even if null
        VariableMap variables = Variables
                .putValue("Role", "Executive")
                .putValue("BusinessProcess", "PTO Request")
                .putValue("DelegationDateRange", "2019-04-31T00:00:00");

        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Need to establish rule ordering
        assertThat(result.collectEntries("result"))
                .hasSize(2)
                .contains("John M")
                .contains("Kerry");
    }

    @Test
    public void TestCase2() {
        // All variables specified as inputs must be supplied here even if null
        VariableMap variables = Variables
                .putValue("Role", "Executive")
                .putValue("BusinessProcess", "Merit Increase")
                .putValue("DelegationDateRange", "2019-04-31T00:00:00");

        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Need to establish rule ordering
        assertThat(result.collectEntries("result"))
                .hasSize(1)
                .contains("John M");
    }

    private ArrayList<DelegationRule> CreateRules() {
        ArrayList<DelegationRule> delegationRules = new ArrayList<>();

        AddRule1(delegationRules);
        AddRule2(delegationRules);
        AddRule3(delegationRules);

        return delegationRules;
    }

    public void AddRule1(ArrayList<DelegationRule> delegationRules) {
        ArrayList<org.camunda.bpm.example.drg.Rule> rules = new ArrayList<>();
        // Notice the extra quotation mark in the rule value. This is necessary to indicate this is a literal value.
        // We may have to decide on whether to have the client enter these with the quotes or add them ourselves.
        // Adding it in our selves introduces issues with a case where the rule is like this: "Test", "Test2", "Test3". Which means any of these strings pass the test.
        rules.add(new org.camunda.bpm.example.drg.Rule("Role", "\"Executive\"", null, Operators.Equal, Types.String));
        rules.add(new org.camunda.bpm.example.drg.Rule("BusinessProcess", "\"PTO Request\"", null, Operators.Equal, Types.String));

        DelegationRule delegate = new DelegationRule(rules, "someId", LocalDateTime.of(2019, 4, 17, 0, 0), LocalDateTime.of(2019, 5, 17, 23, 59), "John M");

        delegationRules.add(delegate);
    }

    public void AddRule2(ArrayList<DelegationRule> delegationRules) {
        ArrayList<org.camunda.bpm.example.drg.Rule> rules = new ArrayList<>();

        rules.add(new org.camunda.bpm.example.drg.Rule("Role", "\"Executive\"", null, Operators.Equal, Types.String));
        rules.add(new org.camunda.bpm.example.drg.Rule("BusinessProcess", "\"PTO Request\"", null, Operators.Equal, Types.String));

        DelegationRule delegate = new DelegationRule(rules, "someId", null, null, "Kerry");

        delegationRules.add(delegate);
    }

    public void AddRule3(ArrayList<DelegationRule> delegationRules) {
        ArrayList<org.camunda.bpm.example.drg.Rule> rules = new ArrayList<>();

        rules.add(new org.camunda.bpm.example.drg.Rule("Role", "\"Executive\"", null, Operators.Equal, Types.String));
        rules.add(new org.camunda.bpm.example.drg.Rule("BusinessProcess", "\"Merit Increase\"", null, Operators.Equal, Types.String));

        DelegationRule delegate = new DelegationRule(rules, "someId", LocalDateTime.of(2019, 4, 17, 0, 0), LocalDateTime.of(2019, 5, 17, 23, 59), "John M");

        delegationRules.add(delegate);
    }

}