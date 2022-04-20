package com.ludia.step.demo;

import com.ludia.step.demo.glue.E2ETest;
import com.ludia.step.demo.keyword.AccountCRUDKeywords;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import step.client.StepClient;
import step.client.executions.RemoteExecutionFuture;
import step.core.artefacts.reports.ReportNodeStatus;
import step.core.plans.Plan;
import step.core.plans.builder.PlanBuilder;
import step.core.reports.Error;
import step.functions.io.Output;
import step.functions.packages.FunctionPackage;
import step.functions.type.FunctionTypeException;
import step.functions.type.SetupFunctionException;
import step.grid.io.Attachment;
import step.handlers.javahandler.KeywordRunner;
import step.repositories.parser.StepsParser;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static step.planbuilder.BaseArtefacts.sequence;
import static step.planbuilder.FunctionArtefacts.keyword;

/***
 * A more standard, structured, portable & distributable of implementing a test plan (work in progress)
 * There are many different options for both implementing, publishing and executing a plan:
 * - Implementations: programmatic (pure java) vs natural language (text based) vs visual (IDE)
 * - Publication: artefact bundle published on a nexus vs direct programmatic upload vs manual upload over IDE
 * - Execution: fully local execution vs fully remote execution vs hybrid (local plan with use of remote agents)
 ***/
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(E2ETest.class)
public class PlanBasedAccountKeywordTest {
    //Env properties are global to all the test methods in here
    private static String serviceUriRoot = "http://localhost:30001";

    @BeforeClass
    public static void beforeClass() throws Exception {

    }

    //Approach 1: Programmatic plan with remote execution
    @Test
    public void programmaticPlanRemoteExecTest() throws SetupFunctionException, FunctionTypeException, IOException, TimeoutException, InterruptedException {
        try (StepClient client = new StepClient("http://localhost:8080", "admin", "init")) {

            String inputName = "dorian";
            String inputPassword = "1234";
            String inputEmail = "dorian@aol.com";

            JsonObject keywordInput = Json.createObjectBuilder()
                    .add("name", inputName)
                    .add("password", inputPassword)
                    .add("email", inputEmail)
                    .build();

            // Build a demo plan which calls the demo keyword
            Plan plan = PlanBuilder.create()
                    .startBlock(sequence())
                    .add(keyword("CreateAccount", keywordInput.toString()))
                    .add(keyword("ClearAccounts", "{}"))
                    .endBlock()
                    .build();

            // Upload the plan to the controller
            client.getPlans().save(plan);

            // Execute the plan on the controller
            String executionId = client.getExecutionManager().execute(plan.getId().toString());

            RemoteExecutionFuture future = client.getExecutionManager().getFuture(executionId);

            // Wait for the plan execution to terminate and visit the report tree
            future.waitForExecutionToTerminate().visitReportTree(node -> {
                Assert.assertEquals(ReportNodeStatus.PASSED, node.getNode().getStatus());
            });
        }
    }

    //@Test //Direct programmatic upload of the test jar
    public void keywordPackageCreation() throws SetupFunctionException, FunctionTypeException, IOException,
            TimeoutException, InterruptedException, StepsParser.ParsingException {
        try (StepClient client = new StepClient("http://localhost:8080", "admin", "init")) {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("version", "1234");
            attributes.put("name", "myPackageName2");

            // Upload new function package
            FunctionPackage myKwPackage = client.getFunctionPackageClient().newKeywordPackage(null,
                    new File("target/step-demo-1.0-SNAPSHOT.jar"), attributes);

            //client.getFunctionPackageClient().deleteKeywordPackage(myKwPackage.getId().toString());
        }
    }
}