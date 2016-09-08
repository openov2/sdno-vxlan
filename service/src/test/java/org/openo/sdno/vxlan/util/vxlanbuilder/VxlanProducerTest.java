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

package org.openo.sdno.vxlan.util.vxlanbuilder;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVtep;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.mappingpolicy.VxlanMappingPolicy;

public class VxlanProducerTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testBuild() throws ServiceException {
        Connection connection = new Connection();
        VxlanMappingPolicy vxlanMappingPolicy = new VxlanMappingPolicy();
        connection.setVxlanMappingPolicy(vxlanMappingPolicy);

        Map<String, NeVtep> deviceIdToNeVtepMap = new HashMap<>();
        NeVtep neVtep1 = new NeVtep("WSIF01", "192.168.1.1");
        NeVtep neVtep2 = new NeVtep("WSIF02", "192.168.1.2");
        deviceIdToNeVtepMap.put("WSIF01", neVtep1);
        deviceIdToNeVtepMap.put("WSIF02", neVtep2);

        Map<String, WanSubInterface> deviceIdToWansubInfMap = new HashMap<>();
        WanSubInterface wsif1 = new WanSubInterface();
        deviceIdToWansubInfMap.put("WSIF01", wsif1);
        WanSubInterface wsif2 = new WanSubInterface();
        deviceIdToWansubInfMap.put("WSIF02", wsif2);

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
        portNativeIdToVlanMap2.put("01", tmp2);
        epg2.setPortNativeIdToVlanMap(portNativeIdToVlanMap2);
        operEpgs.add(epg2);

        String vni = "VNI";

        FullMeshVxlanBuilder vxlanBuilder = new FullMeshVxlanBuilder(connection, deviceIdToNeVtepMap,
                deviceIdToWansubInfMap, originalEpgs, operEpgs, vni);

        List<CommonVxlanBuilder> vxlanBuilders = new ArrayList<CommonVxlanBuilder>();
        vxlanBuilders.add(vxlanBuilder);

        VxlanProducer vxlanProducer = new VxlanProducer(vxlanBuilders);
        vxlanProducer.build();

        assertEquals(2, vxlanProducer.getOperVxlanInstances().size());
        assertEquals(2, vxlanProducer.getAllVxlanInstances().size());
        assertEquals("VNI", vxlanProducer.getOperVxlanInstances().get(0).getVni());
        assertEquals("WSIF01", vxlanProducer.getOperVxlanInstances().get(0).getNeId());
        assertEquals("Normal", vxlanProducer.getOperVxlanInstances().get(0).getActionState());
    }

}
