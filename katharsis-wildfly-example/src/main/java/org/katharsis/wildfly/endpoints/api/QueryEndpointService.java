/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.katharsis.wildfly.endpoints.api;

import java.io.Serializable;
import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 *
 * @author grogdj
 */
@Local
@Path("/query")
public interface QueryEndpointService extends Serializable {

    @GET
    @Path("all")
    @Produces({MediaType.APPLICATION_JSON})
    Response getAll();
    
   

}
