/*
 * Copyright (c) 2016, Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openo.sdno.vxlan.service.impl.create;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInterface;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.vxlan.mococlass.MockInventoryDaoUtil;
import org.openo.sdno.vxlan.util.MultiCtrlUtil;

import mockit.Mock;
import mockit.MockUp;

public class CreateVxlanServiceTest {

    @Before
    public void setUp() throws Exception {
        new MockInventoryDaoUtil<NeVxlanTunnel>();
        new MockInventoryDaoUtil<NeVxlanInterface>();
        new MockInventoryDaoUtil<NeVxlanInstance>();
        new MockInventoryDaoUtil<Connection>();
        new MockInventoryDaoUtil<EndpointGroup>();
        new MockInventoryDaoUtil<OverlayVpn>();
    }

    @Test
    public void testDeployVxlanInstance() {
        new MockMultiCtrlUtil();
        new MockRestfulProxy();
        try {
            CreateVxlanService.deployVxlanInstance(null, null);
        } catch(ServiceException e) {
            e.printStackTrace();
        }
    }

    public static class MockRestfulProxy extends MockUp<RestfulProxy> {

        @Mock
        public static RestfulResponse post(String uri, RestfulParametes restParametes) throws ServiceException {
            RestfulResponse rsp = new RestfulResponse();
            rsp.setStatus(200);
            rsp.setResponseJson(JsonUtil.toJson(new ResultRsp<List<NeVxlanInstance>>(ErrorCode.OVERLAYVPN_SUCCESS,
                    Arrays.asList(new NeVxlanInstance()))));
            return rsp;
        }
    }

    public static class MockMultiCtrlUtil extends MockUp<MultiCtrlUtil> {

        @Mock
        public static Map<String, List<NeVxlanInstance>> getCtrUuidToInstanceMap(
                List<NeVxlanInstance> createVxlanServiceList, Map<String, ControllerMO> neIdToCtrlMap)
                throws ServiceException {
            Map<String, List<NeVxlanInstance>> mps = new HashMap<String, List<NeVxlanInstance>>();

            NeVxlanInstance instance = new NeVxlanInstance();
            // instance.set
            mps.put("ctruuid", Arrays.asList(instance));

            return mps;
        }
    }

}
