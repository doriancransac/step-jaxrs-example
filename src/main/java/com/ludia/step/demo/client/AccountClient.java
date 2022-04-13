package com.ludia.step.demo.client;

import com.ludia.step.demo.model.Account;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AccountClient {

    private final Client client;
    private final String serviceUri;

    public AccountClient(String serviceUri) {
        this.client = ClientBuilder.newClient();
        this.serviceUri = serviceUri;
    }

    public Account create(String name, String password, String email) throws Exception {
        Account account = new Account(name, password, email);
        Response response = postEntity(account);
        Account returned = response.readEntity(Account.class);

        checkCreatedAccountIntegrity(account, returned);

        return returned;
    }

    public Account readByName(String name) throws Exception {
        //The name argument is passed as a PathParam, not a query param
        //Map<String, String> queryParams = new HashMap<>();
        //queryParams.put("name", name);
        Account returned = getEntity(null, name).readEntity(Account.class);

        checkReadAccountIntegrity(returned);
        return returned;
    }

    public int deleteByName(String name) throws Exception {
        //Only name matters here
        Account account = new Account(name, null, null);
        Response response = postEntity(account);
        String returned = response.readEntity(String.class);

        checkDeletedAccountIntegrity(returned);

        return extractDeletionCount(returned);
    }

    private int extractDeletionCount(String returned) {
        String count = returned.split(" deletion")[0];
        return Integer.parseInt(count.trim());
    }

    private void checkCreatedAccountIntegrity(Account input, Account output) throws Exception {
        if (output == null || !input.getName().equals(output.getName()) || output.getId() == null) {
            throw new Exception("Inconsistent account creation state: input=" + input + ", output =" + output);
        }
    }

    private void checkDeletedAccountIntegrity(String returned) throws Exception {
        if(returned == null || !returned.contains(" deletion(s)")){
            throw new Exception("Inconsistent deletion state, message was:" + returned);
        }
    }

    private void checkReadAccountIntegrity(Account returned) throws Exception {
        //Not necessarily an error per say
        /*if(returned == null){
            throw new Exception("Invalid returned account, account was null");
        }*/
    }

    private Response postEntity(Object entity) throws Exception {
        WebTarget createWebTarget = client.target(this.serviceUri);

        Invocation.Builder createInvocationBuilder =
                createWebTarget.request(MediaType.APPLICATION_JSON);
        //invocationBuilder.header("some-header", "true");
        Response response = createInvocationBuilder.post(Entity.entity(entity, MediaType.APPLICATION_JSON));

        handlePotentialError(response);
        return response;
    }

    private Response getEntity(Map<String, String> queryParams, String pathParam) throws Exception {
        String target = buildUriWithParams(this.serviceUri, queryParams, pathParam);

        WebTarget webTarget = client.target(target);

        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);

        Response response = invocationBuilder.get();

        handlePotentialError(response);
        return response;
    }

    private String buildUriWithParams(String serviceUri, Map<String, String> queryParams, String pathParam) {
        StringBuilder targetBuilder = new StringBuilder();
        targetBuilder.append(serviceUri);

        if(pathParam != null && !pathParam.trim().isEmpty()){
            targetBuilder.append("/").append(pathParam);
        }

        if (queryParams != null && queryParams.size() > 0){
            targetBuilder.append("?");
            queryParams.entrySet().forEach(entry -> {
                targetBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            });
            //Remove trailing & and return trimmed version of the string
            return targetBuilder.substring(0,targetBuilder.length()-1);
        }

        return targetBuilder.toString();
    }

    private void handlePotentialError(Response response) throws Exception {
        if (response.getStatus() != 200) {
            int status = response.getStatus();
            System.err.println("Response Code: " + status + ", entity=" + response.readEntity(String.class));
            throw new Exception("Erroneous service response, code:" + status) ;
        }
    }
}
