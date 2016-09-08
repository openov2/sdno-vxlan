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

package org.openo.sdno.vxlan.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;

public class MultiCtrlUtilTest {

    List<NeVxlanInstance> createVxlanServiceList;

    Map<String, ControllerMO> neIdToCtrlMap;

    @Before
    public void init() {
        createVxlanServiceList = new ArrayList<NeVxlanInstance>();
        neIdToCtrlMap = new HashMap<String, ControllerMO>();
    }

    @Test
    public void testGetCtrUuidToInstanceMapWithEmpty() {
        try {
            MultiCtrlUtil.getCtrUuidToInstanceMap(createVxlanServiceList, neIdToCtrlMap);
        } catch(Exception se) {
            assertTrue(se instanceof ServiceException);
        }

    }

    @Test
    public void testGetCtrUuidToInstanceMapWithWrongKey() {
        ControllerMO controller = new ControllerMO();
        NeVxlanInstance neVxlanInstance = new NeVxlanInstance();
        neVxlanInstance.setNeId("test");
        createVxlanServiceList.add(neVxlanInstance);
        neIdToCtrlMap.put("WrongKey", controller);
        try {
            MultiCtrlUtil.getCtrUuidToInstanceMap(createVxlanServiceList, neIdToCtrlMap);
        } catch(Exception se) {
            assertTrue(se instanceof ServiceException);
        }

    }

    @Test
    public void testGetCtrUuidToInstanceMap() {

        ControllerMO controller = new ControllerMO();
        controller.setObjectId("test");

        List<NeVxlanInstance> instanceList = new ArrayList<NeVxlanInstance>();
        NeVxlanInstance neVxlanInstance = new NeVxlanInstance();
        neVxlanInstance.setNeId("test");
        instanceList.add(neVxlanInstance);

        Map<String, ControllerMO> neIdToControllerMap = new HashMap<String, ControllerMO>();
        neIdToControllerMap.put(neVxlanInstance.getNeId(), controller);
        try {
            MultiCtrlUtil.getCtrUuidToInstanceMap(instanceList, neIdToControllerMap);
        } catch(ServiceException e) {
        }
    }

}
