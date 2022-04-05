package com.ludia.step.demo.keyword;

import com.ludia.step.demo.client.AccountClient;
import com.ludia.step.demo.model.Account;
import step.handlers.javahandler.AbstractKeyword;
import step.handlers.javahandler.Keyword;

public class AccountCRUDKeywords extends AbstractKeyword {

    @Keyword
    public void CreateAccount() throws Exception {
        //Business input wiring
        String name = input.getString("name");
        String password = input.getString("password");
        String email = input.getString("email");

        //Technical properties wiring-- derived from the environment, not business input
        String serviceUri = properties.get("serviceUri");

        //Meat and potatoes of the keyword
        Account account = new AccountClient(serviceUri).create(name, password, email);

        //Output management -- for post validation (equivalent of assertions, outside of keyword)
        boolean successStatus = false;
        if(account != null) {
            successStatus = true;
            output.add("retName", account.getName());
            output.add("retPassword", account.getPassword());
            output.add("retEmail", account.getEmail());
        }
        output.add("success", successStatus);
    }

    @Keyword
    public void DeleteAccount() throws Exception {
        //Business input wiring
        String name = input.getString("name");

        //Technical properties wiring-- derived from the environment, not business input
        String serviceUri = properties.get("serviceUri");

        //Meat and potatoes of the keyword
        new AccountClient(serviceUri).deleteByName(name);

        //Output management -- nothing to do here
        output.add("success", true);
    }
}