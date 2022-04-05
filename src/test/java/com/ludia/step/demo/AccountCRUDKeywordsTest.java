package com.ludia.step.demo;

import com.ludia.step.demo.keyword.AccountCRUDKeywords;
import org.junit.Assert;
import org.junit.Test;
import step.core.reports.Error;
import step.functions.io.Output;
import step.handlers.javahandler.KeywordRunner;

import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class AccountCRUDKeywordsTest {

    @Test
    public void FullAccountCycleKeywordTest() throws Exception{
        AccountCreationKeywordTest();
        AccountDeletionKeywordTest();
    }

    @Test
    public void AccountCreationKeywordTest() throws Exception{
        //Concrete test case input
        String inputName = "dorian";
        String inputPassword = "1234";
        String inputEmail = "dorian@aol.com";

        //Technical input
        Map<String, String> properties = new HashMap<>();
        properties.put("serviceUri", "http://localhost:30001/account/create");

        //Step execution arguments
        String keywordName = "CreateAccount";
        String keywordInput =
                "{" +
                        "\"name\""     + ":" + "\""+inputName+"\""         + "," +
                        "\"password\"" + ":" + "\""+inputPassword+"\""     + "," +
                        "\"email\""    + ":" + "\""+inputEmail+"\""        +
                "}";

        //Actual execution
        KeywordRunner.ExecutionContext ctx = KeywordRunner.getExecutionContext(properties, AccountCRUDKeywords.class);
        Output<JsonObject> output = ctx.run(keywordName, keywordInput);
        printExceptionIfAny(output);
        //Debug print outs & output assertions
        JsonObject payload = output.getPayload();
        System.out.println(payload);
        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        //External business validation
        Assert.assertEquals(inputName, payload.getString("retName"));
        Assert.assertEquals(inputPassword, payload.getString("retPassword"));
        Assert.assertEquals(inputEmail, payload.getString("retEmail"));
    }

    private void printExceptionIfAny(Output<JsonObject> output) {
        Error error = output.getError();
        if(error != null){
            System.out.println(error.getMsg());
        }
    }

    @Test
    public void AccountDeletionKeywordTest() throws Exception{
        //Concrete test case input
        String inputName = "dorian";

        //Technical input
        Map<String, String> properties = new HashMap<>();
        properties.put("serviceUri", "http://localhost:30001/account/deleteByName");

        //Step execution arguments
        String keywordName = "DeleteAccount";
        String keywordInput =
                "{" +
                        "\"name\""     + ":" + "\""+inputName+"\""         +
                        "}";

        //Actual execution
        KeywordRunner.ExecutionContext ctx = KeywordRunner.getExecutionContext(properties, AccountCRUDKeywords.class);
        Output<JsonObject> output = ctx.run(keywordName, keywordInput);
        printExceptionIfAny(output);
        //Debug print outs & output assertions
        JsonObject payload = output.getPayload();
        System.out.println(payload);

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
    }
}