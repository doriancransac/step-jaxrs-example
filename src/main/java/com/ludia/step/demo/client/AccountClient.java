package com.ludia.step.demo.client;

import com.ludia.step.demo.model.Account;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

        return account;
    }

    private void checkCreatedAccountIntegrity(Account input, Account output) throws Exception {
        if (output == null || !input.getName().equals(output.getName())) {
            throw new Exception("Inconsistent account creation state: provided name=" + input.getName() + ", returned=" + output.getName());
        }
    }

    public void deleteByName(String name) throws Exception {
        //Only name matters here
        Account account = new Account(name, null, null);
        Response response = postEntity(account);
        String returned = response.readEntity(String.class);

        checkDeletedAccountIntegrity(returned);
    }

    private void checkDeletedAccountIntegrity(String returned) throws Exception {
        if(returned == null || !returned.contains("1 deletion")){
            throw new Exception("Inconsistent deletion state, message was:" + returned);
        }
    }

    private Response postEntity(Object entity) throws Exception {
        WebTarget createWebTarget = client.target(this.serviceUri);

        Invocation.Builder createInvocationBuilder =
                createWebTarget.request(MediaType.APPLICATION_JSON);
        //invocationBuilder.header("some-header", "true");
        Response response = createInvocationBuilder.post(Entity.entity(entity, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 200) {
            throw new Exception(response.getEntity().toString());
        }
        return response;
    }
}
