/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.katharsis.wildfly.model;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author grogdj
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String email;

    private String nickname;

    private String firstname;

    private String lastname;

    private List<String> interests;

    public User() {
    }

    public User(Long id, String email, String nickname, String firstname, String lastname, List<String> interests) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.firstname = firstname;
        this.lastname = lastname;
        this.interests = interests;
    }
    
    
    public User(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    
    

}
