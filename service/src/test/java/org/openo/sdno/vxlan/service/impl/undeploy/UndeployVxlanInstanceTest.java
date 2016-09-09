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

package org.openo.sdno.vxlan.service.impl.undeploy;

import static org.junit.Assert.assertEquals;

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
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.common.enums.AdminStatus;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInterface;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.vxlan.mococlass.MockInventoryDaoUtil;

import mockit.Mock;
import mockit.MockUp;

public class UndeployVxlanInstanceTest {

    @Before
    public void setUp() {
        new MockInventoryDao();
        new MockInventoryDaoUtil<NeVxlanTunnel>();
        new MockInventoryDaoUtil<NeVxlanInterface>();
        new MockInventoryDaoUtil<NeVxlanInstance>();
    }

    @Test
    public void testUndeployInstance() {

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

        try {
            ResultRsp<String> resultRsp = UndeployVxlanInstance.undeployInstance("123", "111", "ne1");
            assertEquals(resultRsp.getErrorCode(), ErrorCode.OVERLAYVPN_SUCCESS);
        } catch(ServiceException e) {
            e.printStackTrace();
        }
    }

    private final class MockInventoryDao<T> extends MockUp<InventoryDao<T>> {

        @Mock
        ResultRsp queryByFilter(Class clazz, String filter, String queryResultFields) throws ServiceException {

            if(NeVxlanTunnel.class.equals(clazz)) {
                NeVxlanTunnel neVxlanTunnel = new NeVxlanTunnel();
                return new ResultRsp<List<NeVxlanTunnel>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(neVxlanTunnel));
            } else if(NeVxlanInterface.class.equals(clazz)) {
                NeVxlanInterface neVxlanInterface = new NeVxlanInterface();
                return new ResultRsp<List<NeVxlanInterface>>(ErrorCode.OVERLAYVPN_SUCCESS,
                        Arrays.asList(neVxlanInterface));
            } else if(NeVxlanInstance.class.equals(clazz)) {
                NeVxlanInstance vxlanInstance = new NeVxlanInstance();
                vxlanInstance.setUuid("route1");
                vxlanInstance.setNeId("ne1");
                vxlanInstance.setControllerId("ttt");
                vxlanInstance.setConnectionServiceId("111");
                vxlanInstance.setAdminStatus(AdminStatus.ACTIVE.getName());
                vxlanInstance.setExternalId("333");
                return new ResultRsp<List<NeVxlanInstance>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(vxlanInstance));
            }

            return null;
        }

        @Mock
        ResultRsp<String> batchDelete(Class clazz, List<String> uuids) throws ServiceException {
            return new ResultRsp<String>();
        }

        @Mock
        public ResultRsp update(Class clazz, List oriUpdateList, String updateFieldListStr) {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        @Mock
        public ResultRsp<T> insert(T data) throws ServiceException {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        @Mock
        public ResultRsp<List<T>> batchInsert(List<T> dataList) {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }
    }
}
