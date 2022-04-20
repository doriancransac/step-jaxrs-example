package com.ludia.step.demo;

import com.ludia.step.demo.glue.E2ETest;
import com.ludia.step.demo.keyword.AccountCRUDKeywords;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import step.core.reports.Error;
import step.functions.io.Output;
import step.grid.io.Attachment;
import step.handlers.javahandler.KeywordRunner;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Basic tests based on the keyword api (reflection + JSON input/output)
 * We're intentionally using boiler code for illustration purposes here
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(E2ETest.class)
public class BasicAccountKeywordTest {
    //Env properties are global to all the test methods in here
    private static String serviceUriRoot = "http://localhost:30001";

    @Test //Individual Creation keyword execution
    public void Ordered002_BasicAccountCreationKeywordTest() throws Exception{
        String keywordName = "CreateAccount";
        JsonObject keywordInput = Json.createObjectBuilder()
                .add("name", "dorian")
                .add("password", "1234")
                .add("email", "dorian@aol.com")
                .build();

        Map<String, String> properties = new HashMap<>();
        properties.put("serviceUri", serviceUriRoot);

        //Actual execution
        KeywordRunner.ExecutionContext ctx = KeywordRunner.getExecutionContext(properties, AccountCRUDKeywords.class);
        Output<JsonObject> output = ctx.run(keywordName, keywordInput);
        JsonObject payload = output.getPayload();
        printExceptionIfAny(output);

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
    }

    @Test //Individual Read keyword execution
    public void Ordered002_BasicAccountReadingKeywordTest() throws Exception{
        String keywordName = "ReadAccountByName";
        JsonObject keywordInput = Json.createObjectBuilder()
                .add("name", "dorian")
                .build();

        Map<String, String> properties = new HashMap<>();
        properties.put("serviceUri", serviceUriRoot);

        //Actual execution
        KeywordRunner.ExecutionContext ctx = KeywordRunner.getExecutionContext(properties, AccountCRUDKeywords.class);
        Output<JsonObject> output = ctx.run(keywordName, keywordInput);
        JsonObject payload = output.getPayload();
        printExceptionIfAny(output);

        //Basic technical validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        //Business validation
        Assert.assertEquals("1234", payload.getString("retPassword"));
    }

    @Test //Individual Deletion keyword execution
    public void Ordered003_BasicAccountDeletionByNameKeywordTest() throws Exception{
        //Concrete test case input
        String inputName = "dorian";
        //Step execution arguments
        String keywordName = "DeleteAccountByName";
        JsonObject keywordInput = Json.createObjectBuilder()
                .add("name", inputName)
                .build();

        Map<String, String> properties = new HashMap<>();
        properties.put("serviceUri", serviceUriRoot);

        //Actual execution
        KeywordRunner.ExecutionContext ctx = KeywordRunner.getExecutionContext(properties, AccountCRUDKeywords.class);
        Output<JsonObject> output = ctx.run(keywordName, keywordInput);
        JsonObject payload = output.getPayload();
        printExceptionIfAny(output);

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        Assert.assertEquals(1, payload.getInt("deletionCount"));
    }

    @Test //Individual Clear keyword execution
    public void Ordered004_BasicAccountClearKeywordTest() throws Exception{
        String keywordName = "ClearAccounts";
        JsonObject keywordInput = Json.createObjectBuilder()
                .build();

        Map<String, String> properties = new HashMap<>();
        properties.put("serviceUri", serviceUriRoot);

        //Actual execution
        KeywordRunner.ExecutionContext ctx = KeywordRunner.getExecutionContext(properties, AccountCRUDKeywords.class);
        Output<JsonObject> output = ctx.run(keywordName, keywordInput);
        JsonObject payload = output.getPayload();
        printExceptionIfAny(output);

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        Assert.assertEquals(0, payload.getInt("deletionCount"));
    }

    private static void printExceptionIfAny(Output<JsonObject> output) {
        List<Attachment> attachments = output.getAttachments();
        if(attachments != null) {
            attachments.forEach(attachment -> {
                System.out.println("--> Attachment:" + attachment.getDescription());
            });
        }

        Error error = output.getError();
        if(error != null){
            System.out.println("--> Error:" + error.getMsg());
        }
    }

}