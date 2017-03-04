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

package org.openo.sdno.vxlan.mococlass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;

import mockit.Mock;
import mockit.MockUp;

public class MockNetworkElementInvDao extends MockUp<NetworkElementInvDao> {

    private Map<String, NetworkElementMO> data = new HashMap<String, NetworkElementMO>();

    public MockNetworkElementInvDao() {
        NetworkElementMO ne1 = new NetworkElementMO();
        ne1.setId("neid1");
        ne1.setNeRole("Thin CPE");
        ne1.setIpAddress("192.168.1.1");
        ne1.setControllerID(Arrays.asList("controllerid"));
        ne1.setName("Ne1");
        ne1.setNativeID("nativeid1");
        data.put("neid1", ne1);

        NetworkElementMO ne2 = new NetworkElementMO();
        ne2.setId("neid2");
        ne2.setNeRole("vCPE");
        ne2.setIpAddress("192.168.2.1");
        ne2.setControllerID(Arrays.asList("controllerid"));
        ne2.setName("Ne2");
        ne2.setNativeID("nativeid2");
        data.put("neid2", ne2);
    }

    @Mock
    public List<NetworkElementMO> getAllMO() {
        List<NetworkElementMO> result = new ArrayList<NetworkElementMO>();
        result.addAll(data.values());
        return result;
    }

    @Mock
    public NetworkElementMO query(String id) {
        return data.get(id);

    }

}
