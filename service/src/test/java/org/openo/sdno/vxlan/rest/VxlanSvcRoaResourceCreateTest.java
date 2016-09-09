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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.brs.invdao.LogicalTernminationPointInvDao;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.invdao.SiteInvDao;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.brs.model.LogicalTernminationPointMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.brs.model.SiteMO;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVtep;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInterface;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.ctrlconnection.ControllerUtil;
import org.openo.sdno.vxlan.mockdata.OverlayVpnData;
import org.openo.sdno.vxlan.mococlass.MockAllocIdResourceInvDao;
import org.openo.sdno.vxlan.mococlass.MockInventoryDao;
import org.openo.sdno.vxlan.mococlass.MockInventoryDaoUtil;
import org.openo.sdno.vxlan.service.impl.VxlanServiceImpl;
import org.openo.sdno.vxlan.service.impl.create.CreateVxlanService;
import org.openo.sdno.vxlan.util.NeInterfaceUtil;
import org.openo.sdno.vxlan.util.WanSubInterfaceUtil;
import org.openo.sdno.vxlan.util.vxlanbuilder.VxlanProducer;

import mockit.Mock;
import mockit.MockUp;

@RunWith(value = JMock.class)
public class VxlanSvcRoaResourceCreateTest {

    Mockery mockery = new JUnit4Mockery();

    @Before
    public void setUp() throws Exception {
        new MockInventoryDao();
        new MockInventoryDaoUtil<NeVxlanTunnel>();
        new MockInventoryDaoUtil<NeVxlanInterface>();
        new MockInventoryDaoUtil<NeVxlanInstance>();
        new MockInventoryDaoUtil<Connection>();
        new MockInventoryDaoUtil<EndpointGroup>();
        new MockInventoryDaoUtil<OverlayVpn>();

        new MockNeDao();
        new MockLtpDao();
        new MockSiteDao();
        new MockControllerUtil();
        new MockNeVtep();

        new MockWanSubInterfaceUtil();

        new MockAllocIdResourceInvDao();

        new MockCreateVxlanService();
    }

    @Test
    public void test() {

        OverlayVpn overlayVpn = JsonUtil.fromJson(OverlayVpnData.getCreateFullMeshVpnDataString(), OverlayVpn.class);

        VxlanSvcRoaResource roa = new VxlanSvcRoaResource();
        roa.setVxlanService(new VxlanServiceImpl());

        try {
            ResultRsp<OverlayVpn> resultRsp = roa.create(null, overlayVpn);
            assertEquals(resultRsp.getErrorCode(), ErrorCode.OVERLAYVPN_SUCCESS);
        } catch(ServiceException e) {
            e.printStackTrace();
        }
    }

    public final class MockNeDao extends MockUp<NetworkElementInvDao> {

        @Mock
        public NetworkElementMO query(String neId) throws ServiceException {
            NetworkElementMO ne = new NetworkElementMO();
            ne.setId("1111");
            ne.setNativeID("fasdf");

            ne.setSiteID(Arrays.asList("fdsafasdf"));

            return ne;
        }
    }

    public final class MockLtpDao extends MockUp<LogicalTernminationPointInvDao> {

        @Mock
        public LogicalTernminationPointMO query(String ltpId) throws ServiceException {
            LogicalTernminationPointMO tp = new LogicalTernminationPointMO();
            tp.setId("sfa");
            tp.setNativeID("fasdf");
            tp.setMeID("1111");

            return tp;
        }
    }

    public final class MockSiteDao extends MockUp<SiteInvDao> {

        @Mock
        public SiteMO query(String ltpId) throws ServiceException {
            SiteMO site = new SiteMO();

            return site;
        }
    }

    public static final class MockControllerUtil extends MockUp<ControllerUtil> {

        @Mock
        public static ResultRsp testCtrlConnection(List neUuids) {
            Map<String, ControllerMO> mps = new HashMap<String, ControllerMO>();

            ControllerMO ctrl = new ControllerMO();

            ctrl.setObjectId("sdafadsf");

            mps.put("1111", ctrl);

            return new ResultRsp<Map<String, ControllerMO>>(ErrorCode.OVERLAYVPN_SUCCESS, mps);
        }
    }

    public static final class MockNeVtep extends MockUp<NeInterfaceUtil> {

        @Mock
        private static ResultRsp<NeVtep> getNeVtep(String cltuuid, String deviceId) {

            return new ResultRsp<NeVtep>(ErrorCode.OVERLAYVPN_SUCCESS, new NeVtep("1111", "10.36.32.20"));
        }
    }

    public static final class MockWanSubInterfaceUtil extends MockUp<WanSubInterfaceUtil> {

        @Mock
        public static ResultRsp<Map<String, WanSubInterface>> queryNeWanInfForVxlan(
                Map<String, NetworkElementMO> deviceIdToNeMap, Map<String, ControllerMO> deviceIdToCtrlMap)
                throws ServiceException {
            Map<String, WanSubInterface> wanSubInfMap = new HashMap<String, WanSubInterface>();
            wanSubInfMap.put("fasdf", new WanSubInterface());
            return new ResultRsp<Map<String, WanSubInterface>>(ErrorCode.OVERLAYVPN_SUCCESS, wanSubInfMap);
        }
    }

    public static final class MockCreateVxlanService extends MockUp<CreateVxlanService> {

        @Mock
        static ResultRsp<OverlayVpn> createVxlanComponents(String tenantId, Map<String, ControllerMO> deviceIdToCtrlMap,
                VxlanProducer vxlanProducer) {
            return new ResultRsp<OverlayVpn>(ErrorCode.OVERLAYVPN_SUCCESS);
        }
    }

}
