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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.model.common.enums.topo.TopologyRole;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVtep;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.mappingpolicy.VxlanMappingPolicy;

public class VxlanBuilderTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testFullMeshBuildNeVxlanInstanceInput() throws ServiceException {
        FullMeshVxlanBuilder vxlanBuilder = new FullMeshVxlanBuilder(null, null, null, null, null, null);
        vxlanBuilder.buildNeVxlanInstance();
    }

    @Test
    public void testHubSpokeBuildNeVxlanInstanceInputNull2() throws ServiceException {
        HubSpokeVxlanBuilder vxlanBuilder = new HubSpokeVxlanBuilder(null, null, null, null, null, null);
        vxlanBuilder.buildNeVxlanInstance();
    }

    @Test
    public void testFullMeshBuildNeVxlanInstance() throws ServiceException {
        Connection connection = new Connection();
        VxlanMappingPolicy vxlanMappingPolicy = new VxlanMappingPolicy();
        connection.setVxlanMappingPolicy(vxlanMappingPolicy);

        Map<String, NeVtep> deviceIdToNeVtepMap = new HashMap<>();
        NeVtep neVtep1 = new NeVtep("WSIF01", "192.168.1.1");
        NeVtep neVtep2 = new NeVtep("WSIF02", "192.168.1.2");
        deviceIdToNeVtepMap.put("WSIF01", neVtep1);
        deviceIdToNeVtepMap.put("WSIF02", neVtep2);

        Map<String, WanSubInterface> deviceIdToWansubInfMap = new HashMap<>();
        WanSubInterface wsif = new WanSubInterface();
        deviceIdToWansubInfMap.put("WSIF01", wsif);

        List<EndpointGroup> originalEpgs = new ArrayList<>();
        List<EndpointGroup> operEpgs = new ArrayList<>();
        EndpointGroup epg1 = new EndpointGroup();
        epg1.setNeId("NE01");
        epg1.setDeviceId("WSIF01");
        Map<String, List<String>> portNativeIdToVlanMap1 = new HashMap<>();
        List<String> tmp1 = new ArrayList<>();
        portNativeIdToVlanMap1.put("01", tmp1);
        epg1.setPortNativeIdToVlanMap(portNativeIdToVlanMap1);
        operEpgs.add(epg1);

        EndpointGroup epg2 = new EndpointGroup();
        epg2.setNeId("NE02");
        epg2.setDeviceId("WSIF02");
        Map<String, List<String>> portNativeIdToVlanMap2 = new HashMap<>();
        List<String> tmp2 = new ArrayList<>();
        portNativeIdToVlanMap2.put("02", tmp2);
        epg2.setPortNativeIdToVlanMap(portNativeIdToVlanMap2);
        operEpgs.add(epg2);

        String vni = "VNI";

        FullMeshVxlanBuilder vxlanBuilder = new FullMeshVxlanBuilder(connection, deviceIdToNeVtepMap,
                deviceIdToWansubInfMap, originalEpgs, operEpgs, vni);
        vxlanBuilder.buildNeVxlanInstance();

        assertEquals(2, vxlanBuilder.getOperVxlanInstances().size());
        assertEquals("WSIF01", vxlanBuilder.getOperVxlanInstances().get(0).getNeId());
        assertEquals("VNI", vxlanBuilder.getOperVxlanInstances().get(0).getVni());
    }

    @Test
    public void testBuildNeVxlanInstance2() throws ServiceException {
        Connection connection = new Connection();
        VxlanMappingPolicy vxlanMappingPolicy = new VxlanMappingPolicy();
        connection.setVxlanMappingPolicy(vxlanMappingPolicy);

        Map<String, NeVtep> deviceIdToNeVtepMap = new HashMap<>();
        NeVtep neVtep1 = new NeVtep("WSIF01", "192.168.1.1");
        NeVtep neVtep2 = new NeVtep("WSIF02", "192.168.1.2");
        deviceIdToNeVtepMap.put("WSIF01", neVtep1);
        deviceIdToNeVtepMap.put("WSIF02", neVtep2);

        Map<String, WanSubInterface> deviceIdToWansubInfMap = new HashMap<>();
        WanSubInterface wsif = new WanSubInterface();
        wsif.setName("wsif");
        deviceIdToWansubInfMap.put("WSIF01", wsif);

        List<EndpointGroup> operEpgs = new ArrayList<>();
        List<EndpointGroup> originalEpgs = new ArrayList<>();
        EndpointGroup hubEpg = new EndpointGroup();
        hubEpg.setTopologyRole(TopologyRole.HUB.getName());
        hubEpg.setNeId("NE01");
        hubEpg.setDeviceId("WSIF01");
        Map<String, List<String>> portNativeIdToVlanMap1 = new HashMap<>();
        List<String> tmp1 = new ArrayList<>();
        portNativeIdToVlanMap1.put("01", tmp1);
        hubEpg.setPortNativeIdToVlanMap(portNativeIdToVlanMap1);

        EndpointGroup spokeEpg = new EndpointGroup();
        spokeEpg.setTopologyRole(TopologyRole.SPOKE.getName());
        spokeEpg.setNeId("NE02");
        spokeEpg.setDeviceId("WSIF02");
        Map<String, List<String>> portNativeIdToVlanMap2 = new HashMap<>();
        List<String> tmp2 = new ArrayList<>();
        portNativeIdToVlanMap1.put("02", tmp2);
        spokeEpg.setPortNativeIdToVlanMap(portNativeIdToVlanMap2);

        operEpgs.add(hubEpg);
        operEpgs.add(spokeEpg);

        String vni = "VNI";

        HubSpokeVxlanBuilder vxlanBuilder = new HubSpokeVxlanBuilder(connection, deviceIdToNeVtepMap,
                deviceIdToWansubInfMap, originalEpgs, operEpgs, vni);
        vxlanBuilder.buildNeVxlanInstance();

        assertEquals(2, vxlanBuilder.getOperVxlanInstances().size());
        assertEquals("WSIF01", vxlanBuilder.getOperVxlanInstances().get(0).getNeId());
        assertEquals("VNI", vxlanBuilder.getOperVxlanInstances().get(0).getVni());
    }
}
