package com.ludia.step.demo.client;

import com.ludia.step.demo.model.Account;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class AccountClient {

    private final Client client;
    private final String serviceUri;

    private static final String accountPath = "/account";

    private static final String createPath = "/create";
    private static final String deleteByNamePath = "/deleteByName";
    private static final String deleteByIdPath = "/deleteById";
    private static final String readByNamePath = "/readByName";
    private static final String clearPath = "/clear";

    public AccountClient(String serviceUri) {
        this.client = ClientBuilder.newClient();
        this.serviceUri = serviceUri;
    }

    public Account create(String name, String password, String email) throws Exception {
        Account account = new Account(name, password, email);
        Response response = postEntity(account, accountPath + createPath);
        Account returned = response.readEntity(Account.class);

        checkCreatedAccountIntegrity(account, returned);

        return returned;
    }

    public Account readByName(String name) throws Exception {
        //The name argument is passed as a PathParam, not a query param
        //Map<String, String> queryParams = new HashMap<>();
        //queryParams.put("name", name);
        Account returned = getEntity(null, name, accountPath + readByNamePath).readEntity(Account.class);

        checkReadAccountIntegrity(returned);
        return returned;
    }

    public int deleteByName(String name) throws Exception {
        //Only name matters here
        Account account = new Account(name, null, null);
        Response response = postEntity(account, accountPath + deleteByNamePath);
        String returned = response.readEntity(String.class);

        checkDeletedAccountIntegrity(returned);

        return extractDeletionCount(returned);
    }

    public int deleteById(String id) throws Exception {
        //TODO: make delete calls more consistent: deleteByName posts the name
        // but this service requires the id to be sent via QueryParam
        Response response = postEntity(null, accountPath + deleteByIdPath + "/" + id);
        String returned = response.readEntity(String.class);

        checkDeletedAccountIntegrity(returned);

        return extractDeletionCount(returned);
    }


    public int clear() throws Exception {
        Response response =  getEntity(null, null, accountPath + clearPath);
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

    private Response postEntity(Object entity, String target, String servicePath) throws Exception {
        WebTarget webTarget = client.target(target);
        return postEntity(entity, webTarget);
    }

    private Response postEntity(Object entity, String servicePath) throws Exception {
        WebTarget webTarget = client.target(this.serviceUri + servicePath);
        return postEntity(entity, webTarget);
    }

    private Response postEntity(Object entity, WebTarget createWebTarget) throws Exception {
        Invocation.Builder createInvocationBuilder =
                createWebTarget.request(MediaType.APPLICATION_JSON);
        //invocationBuilder.header("some-header", "true");
        Response response = createInvocationBuilder.post(Entity.entity(entity, MediaType.APPLICATION_JSON));

        handlePotentialError(response);
        return response;
    }

    private Response getEntity(Map<String, String> queryParams, String pathParam, String servicePath) throws Exception {
        String target = buildUriWithParams(this.serviceUri + servicePath, queryParams, pathParam);

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
            String entity = null;
            try {
                entity = response.readEntity(String.class);
            }catch (Exception e){
                entity = e.getClass().getSimpleName() + " : " +e.getMessage();
            }
            System.err.println("Response Code: " + status + ", entity=" + entity);
            throw new Exception("Erroneous service response, code:" + status + ", entity=" + entity) ;
        }
    }
}
