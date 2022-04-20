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
 * A naive attempt at building an E2E test plan based off of the keywords
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(E2ETest.class)
public class StructuredAccountKeywordTest {
    //Env properties are global to all the test methods in here
    private static String serviceUriRoot = "http://localhost:30001";

    @BeforeClass
    public static void beforeClass() throws Exception {
        //Clear entire collection to get a clean slate prior to running tests
        System.out.println("--> Clearing account collection");
        JsonObject payload = runAccountClearKeyword();
        int deletionCount = payload.getInt("deletionCount");
        if(deletionCount > 0){
            System.err.println("WARNING: "+deletionCount+" existing account(s) have just been cleared.");
        }else{
            System.err.println("-->: Nothing to clear.");
        }
    }

    //Multi-service Test sequence (could/should be run as a "Plan")
    @Test
    public void Ordered000_NameBasedCycleKeywordTest() throws Exception{
        String inputName = "dorian";
        String inputPassword = "1234";
        String inputEmail = "dorian@aol.com";

        runAccountCreationKeywordTest(inputName, inputPassword, inputEmail);
        runAccountReadByNameKeywordTest(inputName, true);
        runAccountDeletionByNameKeywordTest(inputName, 1);
        runAccountReadByNameKeywordTest(inputName, false);
        runAccountDeletionByNameKeywordTest(inputName, 0);
    }

    @Test
    public void Ordered005_IdBasedCycleKeywordTest() throws Exception{
        String inputName = "dorian";
        String inputPassword = "1234";
        String inputEmail = "dorian@aol.com";

        String id = runAccountCreationKeywordTest(inputName, inputPassword, inputEmail);
        runAccountReadByNameKeywordTest(inputName, true);
        runAccountDeletionByIdKeywordTest(id, 1);
        runAccountReadByNameKeywordTest(inputName, false);
        runAccountDeletionByNameKeywordTest(inputName, 0);
    }

    @Test //Individual Creation keyword execution
    public void Ordered001_AccountCreationKeywordTest() throws Exception{
        //Concrete test case input
        String inputName = "dorian";
        String inputPassword = "1234";
        String inputEmail = "dorian@aol.com";

        runAccountCreationKeywordTest(inputName, inputPassword, inputEmail);
    }

    @Test //Individual Read keyword execution
    public void Ordered002_AccountReadingKeywordTest() throws Exception{
        //Concrete test case input
        String inputName = "dorian";
        //Variable test case validation input
        boolean isExpectedPresent = true;

        runAccountReadByNameKeywordTest(inputName, isExpectedPresent);
    }

    @Test //Individual Deletion keyword execution
    public void Ordered003AccountDeletionByNameKeywordTest() throws Exception{
        //Concrete test case input
        String inputName = "dorian";
        //Variable test case validation input
        int expectedDeletionCount = 1;

        runAccountDeletionByNameKeywordTest(inputName, expectedDeletionCount);
    }

    @Test //Individual Clear keyword execution
    public void Ordered004AccountClearKeywordTest() throws Exception{
        //This test case has no business input
        //Variable test case validation input
        int expectedDeletionCount = 0;

        runAccountClearKeywordTest(expectedDeletionCount);
    }

    private String runAccountCreationKeywordTest(String inputName, String inputPassword, String inputEmail) throws Exception {
        //Step execution arguments
        String keywordName = "CreateAccount";
        JsonObject keywordInput = Json.createObjectBuilder()
                .add("name", inputName)
                .add("password", inputPassword)
                .add("email", inputEmail)
                .build();

        JsonObject payload = runKeyword(keywordName, keywordInput.toString());

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        //External business validation
        Assert.assertEquals(inputName, payload.getString("retName"));
        Assert.assertEquals("<redacted>", payload.getString("retPassword"));
        Assert.assertEquals(inputEmail, payload.getString("retEmail"));

        return payload.getString("retId");
    }

    private void runAccountDeletionByNameKeywordTest(String inputName, int expectedDeletionCount) throws Exception {
        //Step execution arguments
        String keywordName = "DeleteAccountByName";
        JsonObject keywordInput = Json.createObjectBuilder()
                .add("name", inputName)
                .build();

        //Actual execution
        JsonObject payload = runKeyword(keywordName, keywordInput.toString());

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        Assert.assertEquals(expectedDeletionCount, payload.getInt("deletionCount"));
    }

    private void runAccountDeletionByIdKeywordTest(String id, int expectedDeletionCount) throws Exception {
        //Step execution arguments
        String keywordName = "DeleteAccountById";
        JsonObject keywordInput = Json.createObjectBuilder()
                .add("id", id)
                .build();

        //Actual execution
        JsonObject payload = runKeyword(keywordName, keywordInput.toString());

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        Assert.assertEquals(expectedDeletionCount, payload.getInt("deletionCount"));
    }

    private void runAccountReadByNameKeywordTest(String inputName, boolean isExpectedPresent) throws Exception {
        //Step execution arguments
        String keywordName = "ReadAccountByName";
        JsonObject keywordInput = Json.createObjectBuilder()
                .add("name", inputName)
                .build();

        //Actual execution
        JsonObject payload = runKeyword(keywordName, keywordInput.toString());

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        //Business validation
        Assert.assertEquals(isExpectedPresent, payload.getBoolean("isPresent"));
    }

    private void runAccountClearKeywordTest(int expectedDeletionCount) throws Exception {
        JsonObject payload = runAccountClearKeyword();
        Assert.assertEquals(expectedDeletionCount, payload.getInt("deletionCount"));
    }

    //This method does not check for a specific deletion count, can be used for technical calls
    private static JsonObject runAccountClearKeyword() throws Exception {
        //Step execution arguments
        String keywordName = "ClearAccounts";
        JsonObject keywordInput = Json.createObjectBuilder()
                .build();

        //Actual execution
        JsonObject payload = runKeyword(keywordName, keywordInput.toString());

        //Basic validation
        Assert.assertEquals(true, payload.getBoolean("success"));
        return payload;
    }

    private static JsonObject runKeyword(String keywordName, String keywordInput) throws Exception {

        Map<String, String> properties = new HashMap<>();
        // Endpoint config
        properties.put("serviceUri", serviceUriRoot);

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