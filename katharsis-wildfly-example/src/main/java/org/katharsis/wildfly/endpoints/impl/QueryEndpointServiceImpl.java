/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.katharsis.wildfly.endpoints.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;

import org.katharsis.wildfly.endpoints.api.QueryEndpointService;
import org.katharsis.wildfly.model.User;

/**
 *
 * @author grogdj
 */
@Stateless
public class QueryEndpointServiceImpl implements QueryEndpointService {


    private final static Logger log = Logger.getLogger(QueryEndpointServiceImpl.class.getName());

    private List<User> users = new ArrayList<User>();

    public QueryEndpointServiceImpl() {
        List<String> interests = new ArrayList<String>();
        interests.add("coding");
        interests.add("art");
        users.add(new User(1L, "grogdj@gmail.com", "grogdj", "grogj", "dj", interests));
        users.add(new User(2L, "bot@gmail.com", "bot", "bot", "harry", interests));
        users.add(new User(3L, "evilbot@gmail.com", "evilbot", "bot", "john", interests));
    }

    @Override
    public Response getAll(){

        
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (User u : users) {
            JsonObjectBuilder jsonUserObjectBuilder = createFullJsonUser(u);
            jsonArrayBuilder.add(jsonUserObjectBuilder);
        }
        return Response.ok(jsonArrayBuilder.build().toString()).build();
    }

    private JsonObjectBuilder createFullJsonUser(User u) {
        JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder();
        jsonObjBuilder.add("userId", (u.getId() == null) ? "" : u.getId().toString());
        
        jsonObjBuilder.add("firstname", (u.getFirstname() == null) ? "" : u.getFirstname());
        jsonObjBuilder.add("lastname", (u.getLastname() == null) ? "" : u.getLastname());
        jsonObjBuilder.add("nickname", (u.getNickname() == null) ? "" : u.getNickname());
        JsonArrayBuilder interestsJsonArrayBuilder = Json.createArrayBuilder();
        for (String i : u.getInterests()) {
            interestsJsonArrayBuilder.add(i);
        }
        jsonObjBuilder.add("interests", interestsJsonArrayBuilder);
        return jsonObjBuilder;
    }

    
    
   

}
