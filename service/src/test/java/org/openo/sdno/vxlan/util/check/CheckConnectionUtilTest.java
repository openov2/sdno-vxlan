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

package org.openo.sdno.vxlan.util.check;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.model.ipsec.IkePolicy;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.model.servicemodel.mappingpolicy.VxlanMappingPolicy;

public class CheckConnectionUtilTest {

    private static CheckConnectionUtil checkConnectionUtil = new CheckConnectionUtil();

    @Before
    public void setUp() throws Exception {

    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionTechnologyTypeInvalid() throws ServiceException {
        OverlayVpn overlayVpn = new OverlayVpn();
        List<Connection> connectionList = new ArrayList<Connection>();
        connectionList.add(new Connection());
        overlayVpn.setVpnConnections(connectionList);
        List<String> connectionIdList = new ArrayList<String>();
        connectionIdList.add("id1");
        overlayVpn.setConnectionIds(connectionIdList);

        checkConnectionUtil.checkConnection(overlayVpn);
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionMappingPolicyIsNull() throws ServiceException {
        OverlayVpn overlayVpn = new OverlayVpn();
        List<Connection> connectionList = new ArrayList<Connection>();
        Connection con = new Connection();
        con.setTechnology("vxlan");
        connectionList.add(con);
        overlayVpn.setVpnConnections(connectionList);
        List<String> connectionIdList = new ArrayList<String>();
        connectionIdList.add("id1");
        overlayVpn.setConnectionIds(connectionIdList);

        checkConnectionUtil.checkConnection(overlayVpn);
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionTypeNotConsistency() throws ServiceException {
        OverlayVpn overlayVpn = new OverlayVpn();
        List<Connection> connectionList = new ArrayList<Connection>();
        Connection con = new Connection();
        con.setTechnology("vxlan");
        VxlanMappingPolicy vxlanMappingPolicy = new VxlanMappingPolicy();
        con.setVxlanMappingPolicy(vxlanMappingPolicy);
        connectionList.add(con);
        overlayVpn.setVpnConnections(connectionList);
        List<String> connectionIdList = new ArrayList<String>();
        connectionIdList.add("id1");
        overlayVpn.setConnectionIds(connectionIdList);

        checkConnectionUtil.checkConnection(overlayVpn);
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionMappingPolicyInvalid() throws ServiceException {
        OverlayVpn overlayVpn = new OverlayVpn();
        List<Connection> connectionList = new ArrayList<Connection>();
        Connection con = new Connection();
        con.setTechnology("vxlan");
        VxlanMappingPolicy vxlanMappingPolicy = new VxlanMappingPolicy();
        vxlanMappingPolicy.setUuid("vxlanMappingPolicy01");
        vxlanMappingPolicy.setType("vxlan");
        IkePolicy ikePolicy = new IkePolicy();
        vxlanMappingPolicy.setIkePolicy(ikePolicy);
        con.setVxlanMappingPolicy(vxlanMappingPolicy);
        connectionList.add(con);
        overlayVpn.setVpnConnections(connectionList);
        List<String> connectionIdList = new ArrayList<String>();
        connectionIdList.add("id1");
        overlayVpn.setConnectionIds(connectionIdList);

        checkConnectionUtil.checkConnection(overlayVpn);
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionMappingPolicyInvalid2() throws ServiceException {
        OverlayVpn overlayVpn = new OverlayVpn();
        List<Connection> connectionList = new ArrayList<Connection>();
        Connection con = new Connection();
        con.setTechnology("vxlan_over_ipsec");
        VxlanMappingPolicy vxlanMappingPolicy = new VxlanMappingPolicy();
        vxlanMappingPolicy.setUuid("vxlanMappingPolicy01");
        vxlanMappingPolicy.setType("vxlan_over_ipsec");
        con.setVxlanMappingPolicy(vxlanMappingPolicy);
        connectionList.add(con);
        overlayVpn.setVpnConnections(connectionList);
        List<String> connectionIdList = new ArrayList<String>();
        connectionIdList.add("id1");
        overlayVpn.setConnectionIds(connectionIdList);

        checkConnectionUtil.checkConnection(overlayVpn);
    }

    @Test(expected = ServiceException.class)
    public void testCheckConnectionEndpointGroupsIsEmpty() throws ServiceException {
        OverlayVpn overlayVpn = new OverlayVpn();
        List<Connection> connectionList = new ArrayList<Connection>();
        Connection con = new Connection();
        con.setTechnology("vxlan");
        VxlanMappingPolicy vxlanMappingPolicy = new VxlanMappingPolicy();
        vxlanMappingPolicy.setType("vxlan");
        con.setVxlanMappingPolicy(vxlanMappingPolicy);
        connectionList.add(con);
        overlayVpn.setVpnConnections(connectionList);
        List<String> connectionIdList = new ArrayList<String>();
        connectionIdList.add("id1");
        overlayVpn.setConnectionIds(connectionIdList);

        checkConnectionUtil.checkConnection(overlayVpn);
    }

    @Test
    public void testCheckConnectionInputValid() {
        OverlayVpn overlayVpn = new OverlayVpn();
        List<Connection> connectionList = new ArrayList<Connection>();
        Connection con = new Connection();
        con.setTechnology("vxlan");
        VxlanMappingPolicy vxlanMappingPolicy = new VxlanMappingPolicy();
        vxlanMappingPolicy.setType("vxlan");
        con.setVxlanMappingPolicy(vxlanMappingPolicy);

        EndpointGroup epg = new EndpointGroup();
        List<EndpointGroup> tempEndpointGroups = new ArrayList<EndpointGroup>();
        tempEndpointGroups.add(epg);
        con.setEndpointGroups(tempEndpointGroups);
        connectionList.add(con);
        overlayVpn.setVpnConnections(connectionList);

        List<String> connectionIdList = new ArrayList<String>();
        connectionIdList.add("id1");
        overlayVpn.setConnectionIds(connectionIdList);

        try {
            checkConnectionUtil.checkConnection(overlayVpn);
        } catch(ServiceException e) {
            fail("should not get ServiceException, " + e.getId());
        }
    }
}
