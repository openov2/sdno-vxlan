/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openo.sdno.vxlan.rest;

import java.io.FileInputStream;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swagger API Doc.<br/>
 *
 * @author
 * @version SDNO 0.5 Oct 24, 2016
 */
@Path("/sdnovxlan/v1")
@Produces({MediaType.APPLICATION_JSON})
public class SwaggerRoa {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerRoa.class);

    private String swaggerFilePath;

    public void setSwaggerFilePath(String swaggerFilePath) {
        this.swaggerFilePath = swaggerFilePath;
    }

    /**
     * Query api document.<br>
     * 
     * @return api document
     * @throws ServiceException when read document failed
     * @since SDNO 0.5
     */
    @GET
    @Path("/swagger.json")
    @Produces({MediaType.APPLICATION_JSON})
    public String apidoc() throws ServiceException {
        try (FileInputStream finStream = new FileInputStream(swaggerFilePath)) {
            return IOUtils.toString(finStream);
        } catch(IOException e) {
            LOGGER.error("Read swagger json occurs exception");
            throw new ServiceException("Read swagger json occurs exception");
        }
    }
}
