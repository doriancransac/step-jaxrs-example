package com.ludia.step.demo;

import com.ludia.step.demo.keyword.AccountCRUDKeywords;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import step.core.reports.Error;
import step.functions.io.Output;
import step.grid.io.Attachment;
import step.handlers.javahandler.KeywordRunner;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountCRUDKeywordsLocalTest {
    //Env properties are global to all the test methods in here
    private static String serviceUriRoot = "http://localhost:30001";

    @Test //Full keyword sequence (could/should be run as a "Plan")
    public void FullAccountCycleKeywordTest() throws Exception{
        String inputName = "dorian";
        String inputPassword = "1234";
        String inputEmail = "dorian@aol.com";

        runAccountCreationKeywordTest(inputName, inputPassword, inputEmail);
        runAccountReadingKeywordTest(inputName, true);
        runAccountDeletionKeywordTest(inputName, 1);
        runAccountReadingKeywordTest(inputName, false);
        runAccountDeletionKeywordTest(inputName, 0);
    }

    //@Test //Individual keyword execution
    public void AccountCreationKeywordTest() throws Exception{
        //Concrete test case input
        String inputName = "dorian";
        String inputPassword = "1234";
        String inputEmail = "dorian@aol.com";

        runAccountCreationKeywordTest(inputName, inputPassword, inputEmail);
    }

    //@Test //Individual keyword execution
    public void AccountDeletionKeywordTest() throws Exception{
        //Concrete test case input
        String inputName = "dorian";
        int expectedDeletionCount = 1;

        runAccountDeletionKeywordTest(inputName, expectedDeletionCount);
    }

    //@Test //Individual keyword execution
    public void AccountReadingKeywordTest() throws Exception{
        //Concrete test case input
        String inputName = "dorian";
        boolean isExpectedPresent = true;

        runAccountReadingKeywordTest(inputName, isExpectedPresent);
    }

    private void runAccountCreationKeywordTest(String inputName, String inputPassword, String inputEmail) throws Exception {
        //Step execution arguments
        String keywordName = "CreateAccount";
        JsonObject keywordInput = Json.createObjectBuilder()
                .add("name", inputName)
                .add("password", inputPassword)
                .add("email", inputEmail)
                .build();

        String servicePath = "/account/create";
        Map<String, String> properties = new HashMap<>();
        // Endpoint config
        properties.put("serviceUri", serviceUriRoot + servicePath);

        JsonObject payload = runKeyword(keywordName, keywordInput.toString(), properties);

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        //External business validation
        Assert.assertEquals(inputName, payload.getString("retName"));
        Assert.assertEquals("<redacted>", payload.getString("retPassword"));
        Assert.assertEquals(inputEmail, payload.getString("retEmail"));
    }

    private void runAccountDeletionKeywordTest(String inputName, int expectedDeletionCount) throws Exception {
        //Step execution arguments
        String keywordName = "DeleteAccount";
        JsonObject keywordInput = Json.createObjectBuilder()
                .add("name", inputName)
                .build();

        String servicePath = "/account/deleteByName";
        Map<String, String> properties = new HashMap<>();
        // Endpoint config
        properties.put("serviceUri", serviceUriRoot + servicePath);

        //Actual execution
        JsonObject payload = runKeyword(keywordName, keywordInput.toString(), properties);

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        Assert.assertEquals(expectedDeletionCount, payload.getInt("deletionCount"));
    }


    private void runAccountReadingKeywordTest(String inputName, boolean isExpectedPresent) throws Exception {
        //Step execution arguments
        String keywordName = "ReadAccount";
        JsonObject keywordInput = Json.createObjectBuilder()
                .add("name", inputName)
                .build();

        String servicePath = "/account/readByName";
        Map<String, String> properties = new HashMap<>();
        // Endpoint config
        properties.put("serviceUri", serviceUriRoot + servicePath);

        //Actual execution
        JsonObject payload = runKeyword(keywordName, keywordInput.toString(), properties);

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        //Business validation
        Assert.assertEquals(isExpectedPresent, payload.getBoolean("isPresent"));
    }

    private JsonObject runKeyword(String keywordName, String keywordInput, Map<String, String> properties) throws Exception {
        System.out.println("CALL_KEYWORD: KEYWORD=" + keywordName + "; INPUT="+keywordInput + "; PROPERTIES=" + properties);
        //Actual execution
        KeywordRunner.ExecutionContext ctx = KeywordRunner.getExecutionContext(properties, AccountCRUDKeywords.class);
        Output<JsonObject> output = ctx.run(keywordName, keywordInput);
        printExceptionIfAny(output);
        //Debug print outs & output assertions
        JsonObject payload = output.getPayload();
        System.out.println("RESPONSE:" + payload);
        return payload;
    }

    private void printExceptionIfAny(Output<JsonObject> output) {
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