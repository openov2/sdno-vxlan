/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInterface;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.model.tunnel.Tunnel;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.vxlan.mococlass.MockAllocIdResourceInvDao;
import org.openo.sdno.vxlan.mococlass.MockInventoryDao;
import org.openo.sdno.vxlan.mococlass.MockInventoryDaoUtil;
import org.openo.sdno.vxlan.service.impl.VxlanServiceImpl;

import mockit.Mock;
import mockit.MockUp;

@RunWith(value = JMock.class)
public class VxlanSvcRoaResourceTest {

    Mockery mockery = new JUnit4Mockery();

    @Test
    public void testQueryTunnel() {
        new MockInventoryDao();
        new MockInventoryDaoUtil<Tunnel>();
        VxlanSvcRoaResource roa = new VxlanSvcRoaResource();
        roa.setVxlanService(new VxlanServiceImpl());

        try {
            ResultRsp<List<Tunnel>> resultRsp = roa.queryTunnel(null, "uuid");
            assertEquals(resultRsp.getErrorCode(), ErrorCode.OVERLAYVPN_SUCCESS);
        } catch(ServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDel() {
        new MockInventoryDao();
        new MockInventoryDaoUtil<NeVxlanTunnel>();
        new MockInventoryDaoUtil<NeVxlanInterface>();
        new MockInventoryDaoUtil<NeVxlanInstance>();
        new MockInventoryDaoUtil<Connection>();
        new MockInventoryDaoUtil<EndpointGroup>();
        new MockInventoryDaoUtil<OverlayVpn>();

        new MockAllocIdResourceInvDao();

        new MockUp<RestfulProxy>() {

            @Mock
            public RestfulResponse delete(String deleteUrl, RestfulParametes restParametes) throws ServiceException {

                RestfulResponse restfulResp = new RestfulResponse();
                Map<String, String> header = new HashMap<String, String>();
                header.put("content", "application/json");
                restfulResp.setRespHeaderMap(header);
                restfulResp.setResponseJson(JsonUtil.toJson(new ResultRsp<String>(ErrorCode.OVERLAYVPN_SUCCESS)));
                restfulResp.setStatus(200);
                return restfulResp;
            }
        };

        VxlanSvcRoaResource roa = new VxlanSvcRoaResource();
        roa.setVxlanService(new VxlanServiceImpl());

        try {
            ResultRsp<String> resultRsp = roa.delete(null, "sdf");
            assertEquals(resultRsp.getErrorCode(), ErrorCode.OVERLAYVPN_SUCCESS);
        } catch(ServiceException e) {
            e.printStackTrace();
        }
    }
 }
