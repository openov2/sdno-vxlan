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

package org.openo.sdno.vxlan.mococlass;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanInstance;
import org.openo.sdno.overlayvpn.result.ResultRsp;

import mockit.Mock;
import mockit.MockUp;

public class MockRestfulProxy extends MockUp<RestfulProxy> {

    @Mock
    RestfulResponse post(String uri, RestfulParametes restParametes) throws ServiceException {
        RestfulResponse response = new RestfulResponse();
        List<SbiNeVxlanInstance> SbiNeVxlanInstanceList = new ArrayList<SbiNeVxlanInstance>();
        SbiNeVxlanInstanceList.add(new SbiNeVxlanInstance());
        ResultRsp<List<SbiNeVxlanInstance>> sbiRsp =
                new ResultRsp<List<SbiNeVxlanInstance>>(ErrorCode.OVERLAYVPN_SUCCESS, SbiNeVxlanInstanceList);
        response.setStatus(HttpStatus.SC_OK);
        response.setResponseJson(JsonUtil.toJson(sbiRsp));

        return response;
    }

    @Mock
    RestfulResponse delete(String uri, RestfulParametes restParametes) throws ServiceException {
        RestfulResponse response = new RestfulResponse();
        List<SbiNeVxlanInstance> SbiNeVxlanInstanceList = new ArrayList<SbiNeVxlanInstance>();
        SbiNeVxlanInstanceList.add(new SbiNeVxlanInstance());
        ResultRsp<List<SbiNeVxlanInstance>> sbiRsp =
                new ResultRsp<List<SbiNeVxlanInstance>>(ErrorCode.OVERLAYVPN_SUCCESS, SbiNeVxlanInstanceList);
        response.setStatus(HttpStatus.SC_OK);
        response.setResponseJson(JsonUtil.toJson(sbiRsp));

        return response;
    }

}
