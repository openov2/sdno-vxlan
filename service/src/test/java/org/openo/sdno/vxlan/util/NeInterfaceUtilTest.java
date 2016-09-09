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

package org.openo.sdno.vxlan.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVtep;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.ctrlconnection.ControllerUtil;

import mockit.Mock;
import mockit.MockUp;

public class NeInterfaceUtilTest {

    @Test
    public void queryVtepForVxlanTestEmpty() {
        Map<String, NetworkElementMO> deviceIdToNeMap = new HashMap<String, NetworkElementMO>();
        Map<String, ControllerMO> deviceIdToCtrlMap = new HashMap<String, ControllerMO>();

        ResultRsp<Map<String, NeVtep>> respData = null;
        try {
            respData = NeInterfaceUtil.queryVtepForVxlan(deviceIdToNeMap, deviceIdToCtrlMap);
        } catch(Exception ex) {
            fail("Exception not expected");
        }
        String resp = "overlayvpn.operation.success";
        assertEquals(resp, respData.getErrorCode());

    }

    @Test
    public void queryVtepForVxlanTestNormal() {

        new MockUp<RestfulProxy>() {

            @Mock
            public RestfulResponse get(String queryUrl, RestfulParametes restParametes) throws ServiceException {

                RestfulResponse restfulResp = new RestfulResponse();
                Map<String, String> header = new HashMap<String, String>();
                header.put("content", "application/json");
                restfulResp.setRespHeaderMap(header);
                restfulResp.setResponseJson("{\"name\":\"test\"}");
                restfulResp.setStatus(200);
                return restfulResp;
            }
        };
        new MockUp<ControllerUtil>() {

            @Mock
            public ResultRsp<Map<String, ControllerMO>> testCtrlConnection(List<String> neUuids)
                    throws ServiceException {

                ControllerMO ctrlMo = new ControllerMO();

                ctrlMo.setObjectId("45466574");
                ctrlMo.setDescription("this is for test");
                ctrlMo.setHostName("htipl");
                ctrlMo.setName("test");
                ctrlMo.setProductName("test");
                ctrlMo.setSlaveHostName("test");
                ctrlMo.setVendor("huawei");
                ctrlMo.setVersion("1.0");
                ResultRsp<Map<String, ControllerMO>> retResult = new ResultRsp<Map<String, ControllerMO>>();
                Map<String, ControllerMO> dataMap = new HashMap<String, ControllerMO>();
                dataMap.put("45466574", ctrlMo);
                retResult.setData(dataMap);
                return retResult;
            }
        };
        Map<String, ControllerMO> deviceIdToCtrlMap = new HashMap<String, ControllerMO>();
        Map<String, NetworkElementMO> deviceIdToNeMap = new HashMap<String, NetworkElementMO>();

        NetworkElementMO networkElementMo = new NetworkElementMO();

        ControllerMO controllerlMo = new ControllerMO();
        controllerlMo.setObjectId("45466574");
        controllerlMo.setDescription("this is for test");
        controllerlMo.setHostName("htipl");
        controllerlMo.setName("test");
        controllerlMo.setProductName("test");
        controllerlMo.setSlaveHostName("test");
        controllerlMo.setVendor("huawei");
        controllerlMo.setVersion("1.0");
        deviceIdToCtrlMap.put("test", controllerlMo);

        networkElementMo.setId("45466574");
        networkElementMo.setAdminState("tester");
        List<String> idList = new ArrayList<String>();
        idList.add("45466574");
        networkElementMo.setControllerID(idList);
        networkElementMo.setDescription("this is for test");
        networkElementMo.setIpAddress("10.10.10.10");
        networkElementMo.setIsVirtual("no");
        networkElementMo.setLocation("bangalore");
        networkElementMo.setLogicID("45466574");
        networkElementMo.setName("test");
        networkElementMo.setNativeID("45466574");
        networkElementMo.setOwner("test");

        deviceIdToNeMap.put("test", networkElementMo);

        ResultRsp<Map<String, NeVtep>> respData = null;
        try {
            respData = NeInterfaceUtil.queryVtepForVxlan(deviceIdToNeMap, deviceIdToCtrlMap);
        } catch(Exception ex) {
            fail("exception :" + ex.toString());
        }
        String errorcode = "overlayvpn.operation.failed";
        assertEquals(errorcode, respData.getErrorCode());
    }

    @Test
    public void queryVtepForVxlanTestInvalidResp() {

        new MockUp<RestfulProxy>() {

            @Mock
            public RestfulResponse get(String queryUrl, RestfulParametes restParametes) throws ServiceException {

                RestfulResponse restfulResp = new RestfulResponse();
                Map<String, String> header = new HashMap<String, String>();
                restfulResp.setRespHeaderMap(header);
                restfulResp.setResponseJson("{\"name\":\"test\"}");
                return restfulResp;
            }
        };
        new MockUp<ControllerUtil>() {

            @Mock
            public ResultRsp<Map<String, ControllerMO>> testCtrlConnection(List<String> neUuids)
                    throws ServiceException {

                ControllerMO ctrlMo = new ControllerMO();

                ctrlMo.setObjectId("45466574");
                ctrlMo.setDescription("this is for test");
                ctrlMo.setHostName("htipl");
                ctrlMo.setName("test");
                ctrlMo.setProductName("test");
                ctrlMo.setSlaveHostName("test");
                ctrlMo.setVendor("huawei");
                ctrlMo.setVersion("1.0");
                ResultRsp<Map<String, ControllerMO>> retResult = new ResultRsp<Map<String, ControllerMO>>();
                Map<String, ControllerMO> dataMap = new HashMap<String, ControllerMO>();
                dataMap.put("45466574", ctrlMo);
                retResult.setData(dataMap);
                return retResult;
            }
        };
        Map<String, ControllerMO> deviceIdToCtrlMap = new HashMap<String, ControllerMO>();
        Map<String, NetworkElementMO> deviceIdToNeMap = new HashMap<String, NetworkElementMO>();

        NetworkElementMO networkElementMo = new NetworkElementMO();

        ControllerMO controllerlMo = new ControllerMO();
        controllerlMo.setObjectId("45466574");
        controllerlMo.setDescription("this is for test");
        controllerlMo.setHostName("htipl");
        controllerlMo.setName("test");
        controllerlMo.setProductName("test");
        controllerlMo.setSlaveHostName("test");
        controllerlMo.setVendor("huawei");
        controllerlMo.setVersion("1.0");
        deviceIdToCtrlMap.put("test", controllerlMo);

        networkElementMo.setId("45466574");
        networkElementMo.setAdminState("tester");
        List<String> idList = new ArrayList<String>();
        idList.add("45466574");
        networkElementMo.setControllerID(idList);
        networkElementMo.setDescription("this is for test");
        networkElementMo.setIpAddress("10.10.10.10");
        networkElementMo.setIsVirtual("no");
        networkElementMo.setLocation("bangalore");
        networkElementMo.setLogicID("45466574");
        networkElementMo.setName("test");
        networkElementMo.setNativeID("45466574");
        networkElementMo.setOwner("test");

        deviceIdToNeMap.put("test", networkElementMo);

        ResultRsp<Map<String, NeVtep>> respData = null;
        try {
            respData = NeInterfaceUtil.queryVtepForVxlan(deviceIdToNeMap, deviceIdToCtrlMap);
        } catch(Exception ex) {
            fail("exception :" + ex.toString());
        }
        String errorcode = "overlayvpn.operation.failed";
        assertEquals(errorcode, respData.getErrorCode());
    }

    @Test
    public void queryVtepForVxlanTestForNullResp() {

        new MockUp<RestfulProxy>() {

            @Mock
            public RestfulResponse get(String queryUrl, RestfulParametes restParametes) throws ServiceException {
                return null;
            }
        };
        new MockUp<ControllerUtil>() {

            @Mock
            public ResultRsp<Map<String, ControllerMO>> testCtrlConnection(List<String> neUuids)
                    throws ServiceException {

                ControllerMO ctrlMo = new ControllerMO();

                ctrlMo.setObjectId("45466574");
                ctrlMo.setDescription("this is for test");
                ctrlMo.setHostName("htipl");
                ctrlMo.setName("test");
                ctrlMo.setProductName("test");
                ctrlMo.setSlaveHostName("test");
                ctrlMo.setVendor("huawei");
                ctrlMo.setVersion("1.0");
                ResultRsp<Map<String, ControllerMO>> retResult = new ResultRsp<Map<String, ControllerMO>>();
                Map<String, ControllerMO> dataMap = new HashMap<String, ControllerMO>();
                dataMap.put("45466574", ctrlMo);
                retResult.setData(dataMap);
                return retResult;
            }
        };
        Map<String, ControllerMO> deviceIdToCtrlMap = new HashMap<String, ControllerMO>();
        Map<String, NetworkElementMO> deviceIdToNeMap = new HashMap<String, NetworkElementMO>();

        NetworkElementMO networkElementMo = new NetworkElementMO();

        ControllerMO controllerlMo = new ControllerMO();
        controllerlMo.setObjectId("45466574");
        controllerlMo.setDescription("this is for test");
        controllerlMo.setHostName("htipl");
        controllerlMo.setName("test");
        controllerlMo.setProductName("test");
        controllerlMo.setSlaveHostName("test");
        controllerlMo.setVendor("huawei");
        controllerlMo.setVersion("1.0");
        deviceIdToCtrlMap.put("test", controllerlMo);

        networkElementMo.setId("45466574");
        networkElementMo.setAdminState("tester");
        List<String> idList = new ArrayList<String>();
        idList.add("45466574");
        networkElementMo.setControllerID(idList);
        networkElementMo.setDescription("this is for test");
        networkElementMo.setIpAddress("10.10.10.10");
        networkElementMo.setIsVirtual("no");
        networkElementMo.setLocation("bangalore");
        networkElementMo.setLogicID("45466574");
        networkElementMo.setName("test");
        networkElementMo.setNativeID("45466574");
        networkElementMo.setOwner("test");

        deviceIdToNeMap.put("test", networkElementMo);
        try {
            NeInterfaceUtil.queryVtepForVxlan(deviceIdToNeMap, deviceIdToCtrlMap);
            fail("exception expected but not thrown");
        } catch(Exception ex) {
            assertTrue(ex instanceof ServiceException);
        } // overlayvpn.operation.failed
    }

    @Test
    public void queryVtepForVxlanTestForExceptionResp() {

        new MockUp<RestfulProxy>() {

            @Mock
            public RestfulResponse get(String queryUrl, RestfulParametes restParametes) throws ServiceException {
                RestfulResponse restfulResp = new RestfulResponse();
                Map<String, String> header = new HashMap<String, String>();
                header.put("content", "application/json");
                restfulResp.setRespHeaderMap(header);
                restfulResp.setResponseJson("{\"exceptionId\":\"1002\"}");
                restfulResp.setStatus(200);
                return restfulResp;
            }
        };
        new MockUp<ControllerUtil>() {

            @Mock
            public ResultRsp<Map<String, ControllerMO>> testCtrlConnection(List<String> neUuids)
                    throws ServiceException {

                ControllerMO ctrlMo = new ControllerMO();

                ctrlMo.setObjectId("45466574");
                ctrlMo.setDescription("this is for test");
                ctrlMo.setHostName("htipl");
                ctrlMo.setName("test");
                ctrlMo.setProductName("test");
                ctrlMo.setSlaveHostName("test");
                ctrlMo.setVendor("huawei");
                ctrlMo.setVersion("1.0");
                ResultRsp<Map<String, ControllerMO>> retResult = new ResultRsp<Map<String, ControllerMO>>();
                Map<String, ControllerMO> dataMap = new HashMap<String, ControllerMO>();
                dataMap.put("45466574", ctrlMo);
                retResult.setData(dataMap);
                return retResult;
            }
        };
        Map<String, ControllerMO> deviceIdToCtrlMap = new HashMap<String, ControllerMO>();
        Map<String, NetworkElementMO> deviceIdToNeMap = new HashMap<String, NetworkElementMO>();

        NetworkElementMO networkElementMo = new NetworkElementMO();

        ControllerMO controllerlMo = new ControllerMO();
        controllerlMo.setObjectId("45466574");
        controllerlMo.setDescription("this is for test");
        controllerlMo.setHostName("htipl");
        controllerlMo.setName("test");
        controllerlMo.setProductName("test");
        controllerlMo.setSlaveHostName("test");
        controllerlMo.setVendor("huawei");
        controllerlMo.setVersion("1.0");
        deviceIdToCtrlMap.put("test", controllerlMo);

        networkElementMo.setId("45466574");
        networkElementMo.setAdminState("tester");
        List<String> idList = new ArrayList<String>();
        idList.add("45466574");
        networkElementMo.setControllerID(idList);
        networkElementMo.setDescription("this is for test");
        networkElementMo.setIpAddress("10.10.10.10");
        networkElementMo.setIsVirtual("no");
        networkElementMo.setLocation("bangalore");
        networkElementMo.setLogicID("45466574");
        networkElementMo.setName("test");
        networkElementMo.setNativeID("45466574");
        networkElementMo.setOwner("test");

        deviceIdToNeMap.put("test", networkElementMo);

        ResultRsp<Map<String, NeVtep>> respData = null;
        try {
            respData = NeInterfaceUtil.queryVtepForVxlan(deviceIdToNeMap, deviceIdToCtrlMap);
        } catch(Exception ex) {
            fail("exception :" + ex.toString());
        }
        String resp = "overlayvpn.operation.failed";
        assertEquals(resp, respData.getErrorCode());
    }

}
