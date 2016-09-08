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

package org.openo.sdno.vxlan.mockdata;

import java.util.ArrayList;
import java.util.List;

import org.openo.sdno.overlayvpn.model.netmodel.ipsec.NeIpSecConnection;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;

public class VxlanServiceData {

    public List<NeVxlanTunnel> neVxlanTunnelList() {
        List<NeVxlanTunnel> neVxlanTunnelList = new ArrayList<NeVxlanTunnel>();
        NeVxlanTunnel neVxlanTunnel = new NeVxlanTunnel();
        neVxlanTunnelList.add(neVxlanTunnel);
        neVxlanTunnel.setConnectionServiceId("connectionServiceId1");
        return neVxlanTunnelList;
    }

    public List<NeIpSecConnection> neIpSecConnectionList() {
        List<NeIpSecConnection> neIpSecConnectionList = new ArrayList<NeIpSecConnection>();
        NeIpSecConnection neIpSecConnection = new NeIpSecConnection();
        neIpSecConnectionList.add(neIpSecConnection);
        neIpSecConnection.setConnectionServiceId("connectionServiceId");
        return neIpSecConnectionList;
    }

}
