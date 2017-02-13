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

package org.openo.sdno.vxlan.drivermanager;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.testframework.http.model.HttpModelUtils;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.restclient.HttpRestClient;

public class DriverRegisterManager {

    private static final String DRIVER_REGISTER_FILE = "src/integration-test/resources/driver/registerdriver.json";

    private static final String DRIVER_UNREGISTER_FILE = "src/integration-test/resources/driver/unregisterdriver.json";

    private static final HttpRestClient restClient = new HttpRestClient();

    public static void registerDriver() throws ServiceException {

        HttpRequest httpRegisterRequest =
                HttpModelUtils.praseHttpRquestResponseFromFile(DRIVER_REGISTER_FILE).getRequest();
        RestfulParametes restfulParametes = new RestfulParametes();
        restfulParametes.setRawData(httpRegisterRequest.getData());
        restfulParametes.setHeaderMap(httpRegisterRequest.getHeaders());
        RestfulResponse registerReponse = restClient.post(httpRegisterRequest.getUri(), restfulParametes);
        if(!HttpCode.isSucess(registerReponse.getStatus())) {
            throw new ServiceException("Register Driver failed");
        }

    }

    public static void unRegisterDriver() throws ServiceException {

        HttpRequest httpUnRegisterRequest =
                HttpModelUtils.praseHttpRquestResponseFromFile(DRIVER_UNREGISTER_FILE).getRequest();
        RestfulParametes restfulParametes = new RestfulParametes();
        restfulParametes.setHeaderMap(httpUnRegisterRequest.getHeaders());
        RestfulResponse unRegisterReponse = restClient.delete(httpUnRegisterRequest.getUri(), new RestfulParametes());
        if(!HttpCode.isSucess(unRegisterReponse.getStatus())) {
            throw new ServiceException("UnRegister Driver failed");
        }

    }

}
