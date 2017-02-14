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

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.model.v2.vxlan.ActionModel;
import org.openo.sdno.overlayvpn.model.v2.vxlan.NbiVxlanTunnel;
import org.openo.sdno.vxlan.mococlass.MockInventoryDaoMercury;
import org.openo.sdno.vxlan.mococlass.MockLogicalTernminationPointInvDao;
import org.openo.sdno.vxlan.mococlass.MockNetworkElementInvDao;
import org.openo.sdno.vxlan.mococlass.MockRestfulProxy;
import org.openo.sdno.vxlan.mococlass.MockSdnControllerDao;
import org.openo.sdno.vxlan.util.builder.VxlanTunnelDbHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import mockit.Mocked;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/applicationContext.xml",
                "classpath*:META-INF/spring/service.xml", "classpath*:spring/service.xml"})
public class VxlanSvcResourceTest {

    VxlanSvcResource demo = new VxlanSvcResource();

    VxlanTunnelDbHelper helper = new VxlanTunnelDbHelper();

    @Mocked
    HttpServletResponse resp;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new MockInventoryDaoMercury();
        new MockLogicalTernminationPointInvDao();
        new MockNetworkElementInvDao();
        new MockSdnControllerDao();
        new MockRestfulProxy();
        VxlanTunnelDbHelper helper = new VxlanTunnelDbHelper();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testQuery() throws ServiceException {
        NbiVxlanTunnel nibTunnel = demo.query("vxlanid");
        assertTrue(nibTunnel.getUuid().equals("vxlanid"));
    }

    @Test
    public void testBatchquery() throws ServiceException {
        List<NbiVxlanTunnel> nibTunnels = demo.batchquery(Arrays.asList("vxlanid"));
        assertTrue(nibTunnels.get(0).getUuid().equals("vxlanid"));
    }

    @Test
    public void testCreateSuccess() throws ServiceException {
        NbiVxlanTunnel nbiTunnel = new NbiVxlanTunnel();
        nbiTunnel.setUuid("vxlanid");
        nbiTunnel.setName("vpn1");
        nbiTunnel.setDestNeId("neid2");
        nbiTunnel.setSrcNeId("neid1");
        nbiTunnel.setDestNeRole("localcpe");
        nbiTunnel.setSrcNeRole("localcpe");
        nbiTunnel.setDestPortName("Ltp2");
        nbiTunnel.setSrcPortName("Ltp1");
        nbiTunnel.setVni("188");
        nbiTunnel.setPortVlanList("[{\"neId\":\"neid1\",\"vlan\":\"5-6\"},{\"neId\":\"neid2\",\"vlan\":\"7-8\"}]");
        List<NbiVxlanTunnel> nibTunnels = demo.create(Arrays.asList(nbiTunnel));
        assertTrue(nibTunnels.get(0).getUuid().equals("vxlanid"));
    }

    @Test
    public void testDelete() throws ServiceException {
        String uuid = demo.delete("vxlanid");
        assertTrue(uuid.equals("vxlanid"));
    }

    @Test
    public void testAction() throws ServiceException {
        ActionModel action = new ActionModel();
        action.setDeploy(Arrays.asList("vxlanid"));
        List<String> uuids = demo.action(action);
        assertTrue(uuids.get(0).equals("vxlanid"));
    }

    @Test
    public void testHealthCheck() throws ServiceException {
        demo.healthCheck(resp);
        assertTrue(true);
    }

}
