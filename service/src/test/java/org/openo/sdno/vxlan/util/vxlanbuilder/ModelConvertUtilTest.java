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

package org.openo.sdno.vxlan.util.vxlanbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.functions.T;
import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVtep;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.res.ResourcesUtil;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.vxlan.mococlass.MockInventoryDaoUtil;

import mockit.Mock;
import mockit.MockUp;

public class ModelConvertUtilTest {

    @Before
    public void setUp() throws Exception {
        new MockInventoryDaoUtil<NeVxlanInstance>();
    }

    @Test
    public void testConvertFromOverlayVpnService1() {
        OverlayVpn vpn = new OverlayVpn();
        List<Connection> vpnConnections = new ArrayList<>();
        Connection con = new Connection("con01");
        con.setTopology("point_to_point");
        vpnConnections.add(con);
        vpn.setVpnConnections(vpnConnections);

        new MockUp<ResourcesUtil>() {

            @Mock
            List<Long> requestGloabelValue(String poolname, String label, int reqNumber, Long min, Long max) {
                List<Long> vniList = new ArrayList<>();
                vniList.add(1L);
                return vniList;
            }
        };

        Map<String, NeVtep> deviceIdToNeVtepMap = new HashMap<>();
        Map<String, WanSubInterface> deviceIdToWansubInfMap = new HashMap<>();
        Map<String, String> vniToConnectionIdMap = new HashMap<>();
        try {
            ModelConvertUtil.convertFromOverlayVpnService(vpn, deviceIdToNeVtepMap, deviceIdToWansubInfMap,
                    vniToConnectionIdMap);
        } catch(ServiceException e) {
            fail("should not get ServiceException, " + e.getId());
        }
    }

    @Test
    public void testConvertFromOverlayVpnService2() throws ServiceException {
        OverlayVpn vpn = new OverlayVpn();
        List<Connection> vpnConnections = new ArrayList<>();
        Connection con = new Connection("con01");
        con.setTopology("full_mesh");
        vpnConnections.add(con);
        vpn.setVpnConnections(vpnConnections);

        new MockUp<ResourcesUtil>() {

            @Mock
            List<Long> requestGloabelValue(String poolname, String label, int reqNumber, Long min, Long max) {
                List<Long> vniList = new ArrayList<>();
                vniList.add(1L);
                return vniList;
            }
        };

        Map<String, NeVtep> deviceIdToNeVtepMap = new HashMap<>();
        Map<String, WanSubInterface> deviceIdToWansubInfMap = new HashMap<>();
        Map<String, String> vniToConnectionIdMap = new HashMap<>();
        ModelConvertUtil.convertFromOverlayVpnService(vpn, deviceIdToNeVtepMap, deviceIdToWansubInfMap,
                vniToConnectionIdMap);
    }

    @Test
    public void testGetVniForConn() throws ServiceException {
        List<Connection> connections = new ArrayList<>();
        Connection con = new Connection("con01");
        EndpointGroup epg = new EndpointGroup("epg01");
        epg.setType("vni");
        epg.setEndpoints("END");
        List<EndpointGroup> list = new ArrayList<>();
        list.add(epg);
        con.setEndpointGroups(list);
        connections.add(con);

        Map<String, String> vniToConnectionIdMap = new HashMap<>();
        ModelConvertUtil.getVniForConn(connections, vniToConnectionIdMap);
        assertEquals("END", vniToConnectionIdMap.get("con01"));
    }

    @Test
    public void testAllocVniForConn() throws ServiceException {
        new MockUp<InventoryDao<NeVxlanInstance>>() {

            @Mock
            public ResultRsp<List<T>> queryByFilter(Class clazz, String filter, String queryResultFields) {
                List<NeVxlanInstance> list = new ArrayList<>();
                NeVxlanInstance ne = new NeVxlanInstance();
                ne.setConnectionServiceId("001");
                ne.setVni("VNI001");
                list.add(ne);
                ResultRsp rsp = new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS, list);
                return rsp;
            }
        };

        new MockUp<ResourcesUtil>() {

            @Mock
            List<Long> requestGloabelValue(String poolname, String label, int reqNumber, Long min, Long max) {
                List<Long> vniList = new ArrayList<>();
                vniList.add(1L);
                return vniList;
            }
        };

        List<Connection> connections = new ArrayList<>();
        Connection con = new Connection("con01");
        EndpointGroup epg = new EndpointGroup("epg01");
        epg.setType("vni");
        epg.setEndpoints("END");
        List<EndpointGroup> list = new ArrayList<>();
        list.add(epg);
        con.setEndpointGroups(list);
        connections.add(con);

        Map<String, String> vniMap = ModelConvertUtil.allocVniForConn(connections);
        assertEquals(vniMap.size(), 2);
    }

    @Test
    public void testAllocVniForConn2() throws ServiceException {
        new MockUp<InventoryDao>() {

            @Mock
            public ResultRsp<List<T>> queryByFilter(Class clazz, String filter, String queryResultFields) {
                List<NeVxlanInstance> list = new ArrayList<>();
                NeVxlanInstance ne = new NeVxlanInstance();
                ne.setConnectionServiceId("001");
                ne.setVni("VNI001");
                list.add(ne);
                ResultRsp rsp = new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS, list);
                return rsp;
            }
        };

        new MockUp<ResourcesUtil>() {

            @Mock
            List<Long> requestGloabelValue(String poolname, String label, int reqNumber, Long min, Long max) {
                List<Long> vniList = new ArrayList<>();
                vniList.add(1L);
                return vniList;
            }
        };

        List<Connection> connections = new ArrayList<>();
        Connection con = new Connection("con01");
        EndpointGroup epg = new EndpointGroup("epg01");
        epg.setType("vni");
        epg.setEndpoints("END");
        List<EndpointGroup> list = new ArrayList<>();
        list.add(epg);
        con.setEndpointGroups(list);
        connections.add(con);
        Map<String, String> result = ModelConvertUtil.allocVniForConn(connections);
        assertEquals(2, result.size());
    }
}
