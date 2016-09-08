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

package org.openo.sdno.vxlan.util.MultiCtrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.vxlan.util.MultiCtrlUtil;

public class GetCtrUuidToInstanceMapTest {

    private List<NeVxlanInstance> createVxlanServiceList = new ArrayList<NeVxlanInstance>();

    private Map<String, ControllerMO> neIdToCtrlMap = new ConcurrentHashMap<String, ControllerMO>();

    @Test
    public void testGetCtrUuidToInstanceMap1() {
        try {
            MultiCtrlUtil.getCtrUuidToInstanceMap(createVxlanServiceList, neIdToCtrlMap);
        } catch(ServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetCtrUuidToInstanceMap2() {

        NeVxlanInstance neVxlanInstance = new NeVxlanInstance();
        neVxlanInstance.setVni("1254");
        neVxlanInstance.setNeId("120.12.0.1");

        List<String> vxlanInterfaces = new ArrayList<String>();
        vxlanInterfaces.add("vxlanInterface1");
        vxlanInterfaces.add("vxlanInterface2");
        neVxlanInstance.setVxlanInterfaces(vxlanInterfaces);

        List<String> vxlanTunnels = new ArrayList<String>();
        vxlanTunnels.add("vxlanTunnel1");
        vxlanTunnels.add("vxlanTunnel2");
        neVxlanInstance.setVxlanTunnels(vxlanTunnels);

        createVxlanServiceList.add(neVxlanInstance);

        try {
            MultiCtrlUtil.getCtrUuidToInstanceMap(createVxlanServiceList, neIdToCtrlMap);
        } catch(ServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetCtrUuidToInstanceMap3() {

        NeVxlanInstance neVxlanInstance = new NeVxlanInstance();
        neVxlanInstance.setVni("1254");
        neVxlanInstance.setNeId("120.12.0.1");

        List<String> vxlanInterfaces = new ArrayList<String>();
        vxlanInterfaces.add("vxlanInterface1");
        vxlanInterfaces.add("vxlanInterface2");
        neVxlanInstance.setVxlanInterfaces(vxlanInterfaces);

        List<String> vxlanTunnels = new ArrayList<String>();
        vxlanTunnels.add("vxlanTunnel1");
        vxlanTunnels.add("vxlanTunnel2");
        neVxlanInstance.setVxlanTunnels(vxlanTunnels);

        createVxlanServiceList.add(neVxlanInstance);

        ControllerMO controllerMO = new ControllerMO();
        controllerMO.setObjectId("120.12.0.1");
        neIdToCtrlMap.put("120.12.0.1", controllerMO);
        try {
            MultiCtrlUtil.getCtrUuidToInstanceMap(createVxlanServiceList, neIdToCtrlMap);
        } catch(ServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetCtrUuidToInstanceMap4() {

        NeVxlanInstance neVxlanInstance = new NeVxlanInstance();
        neVxlanInstance.setVni("1254");
        neVxlanInstance.setNeId("120.12.0.1");

        List<String> vxlanInterfaces = new ArrayList<String>();
        vxlanInterfaces.add("vxlanInterface1");
        vxlanInterfaces.add("vxlanInterface2");
        neVxlanInstance.setVxlanInterfaces(vxlanInterfaces);

        List<String> vxlanTunnels = new ArrayList<String>();
        vxlanTunnels.add("vxlanTunnel1");
        vxlanTunnels.add("vxlanTunnel2");
        neVxlanInstance.setVxlanTunnels(vxlanTunnels);

        createVxlanServiceList.add(neVxlanInstance);

        ControllerMO controllerMO = new ControllerMO();
        controllerMO.setObjectId("120.12.0.1");
        neIdToCtrlMap.put("120.12.0", controllerMO);
        try {
            MultiCtrlUtil.getCtrUuidToInstanceMap(createVxlanServiceList, neIdToCtrlMap);
        } catch(ServiceException e) {
            e.printStackTrace();
        }
    }
}
