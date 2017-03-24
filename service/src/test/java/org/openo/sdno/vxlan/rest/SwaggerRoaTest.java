/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;

public class SwaggerRoaTest {

    private SwaggerRoa swaggerRoa = new SwaggerRoa();

    @Test
    public void apiDocTest() throws ServiceException {
        swaggerRoa.setSwaggerFilePath("src/test/resources/swagger.json");
        assertTrue(StringUtils.isNotBlank(swaggerRoa.apidoc()));
    }

    @Test(expected = ServiceException.class)
    public void apiDocQueryErrorTest() throws ServiceException {
        swaggerRoa.setSwaggerFilePath("src/test/resources/unexist.json");
        swaggerRoa.apidoc();
    }

}
