package com.ludia.step.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public class Account {
    @JsonProperty("_id")
    @JsonSerialize(using = ToStringSerializer.class)
    public ObjectId id;

    public String name;
    public String password;
    public String email;

    public Account() {
    }

    public Account(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObjectId getId() {
        return id;
    }



    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("account={")
                .append("name").append("=").append(name).append(";")
                .append("password").append("=").append(password).append(";")
                .append("id").append("=").append(id).append(";")
                .append("email").append("=").append(email).append(";")
        .append("}");
        return sb.toString();
    }
}
