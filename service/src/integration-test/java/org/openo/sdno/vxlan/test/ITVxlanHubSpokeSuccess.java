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

package org.openo.sdno.vxlan.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.testframework.checker.IChecker;
import org.openo.sdno.testframework.http.model.HttpModelUtils;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.replace.PathReplace;
import org.openo.sdno.testframework.testmanager.TestManager;
import org.openo.sdno.testframework.topology.ResourceType;
import org.openo.sdno.testframework.topology.Topology;
import org.openo.sdno.vxlan.mocoserver.SbiAdapterSuccessServer;

public class ITVxlanHubSpokeSuccess extends TestManager {

    private SbiAdapterSuccessServer sbiAdapterServer = new SbiAdapterSuccessServer();

    private static final String CREATE_VXLAN_TESTCASE =
            "src/integration-test/resources/testcase/createhubspokevxlan.json";

    private static final String QUERY_VXLAN_TESTCASE = "src/integration-test/resources/testcase/queryvxlansuccess.json";

    private static final String DELETE_VXLAN_TESTCASE =
            "src/integration-test/resources/testcase/deletevxlansuccess.json";

    private static final String TOPODATA_PATH = "src/integration-test/resources/topodata";

    private Topology topo = new Topology(TOPODATA_PATH);

    @Before
    public void setup() throws ServiceException {
        topo.createInvTopology();
        sbiAdapterServer.start();
    }

    @After
    public void tearDown() throws ServiceException {
        sbiAdapterServer.stop();
        topo.clearInvTopology();
    }

    @Test
    public void CreateVxlanSuccessTest1() throws ServiceException {

        HttpRquestResponse httpCreateObject = HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_VXLAN_TESTCASE);
        HttpRequest createRequest = httpCreateObject.getRequest();
        OverlayVpn newVpnData = JsonUtil.fromJson(createRequest.getData(), OverlayVpn.class);
        List<Connection> connectionList = newVpnData.getVpnConnections();
        List<EndpointGroup> epgList = connectionList.get(0).getEndpointGroups();
        epgList.get(0).setNeId(topo.getResourceUuid(ResourceType.NETWORKELEMENT, "Ne1"));
        epgList.get(1).setNeId(topo.getResourceUuid(ResourceType.NETWORKELEMENT, "Ne2"));

        List<String> endPointList1 = new ArrayList<String>();
        endPointList1.add(topo.getResourceUuid(ResourceType.LOGICALTERMINATIONPOINT, "Ltp1"));
        endPointList1.add(topo.getResourceUuid(ResourceType.LOGICALTERMINATIONPOINT, "Ltp2"));
        epgList.get(0).setEndpoints(JsonUtil.toJson(endPointList1));

        List<String> endPointList2 = new ArrayList<String>();
        endPointList2.add(topo.getResourceUuid(ResourceType.LOGICALTERMINATIONPOINT, "Ltp3"));
        endPointList2.add(topo.getResourceUuid(ResourceType.LOGICALTERMINATIONPOINT, "Ltp4"));
        epgList.get(1).setEndpoints(JsonUtil.toJson(endPointList2));

        createRequest.setData(JsonUtil.toJson(newVpnData));

        execTestCase(createRequest, new SuccessChecker());

        HttpRquestResponse queryHttpObject = HttpModelUtils.praseHttpRquestResponseFromFile(QUERY_VXLAN_TESTCASE);
        HttpRequest queryRequest = queryHttpObject.getRequest();

        queryRequest.setUri(PathReplace.replaceUuid("connectionid", queryRequest.getUri(), "connection1id"));

        execTestCase(queryRequest, new SuccessChecker());

        HttpRquestResponse deleteHttpObject = HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_VXLAN_TESTCASE);
        HttpRequest deleteRequest = deleteHttpObject.getRequest();

        deleteRequest.setUri(PathReplace.replaceUuid("connectionid", deleteRequest.getUri(), "connection1id"));

        execTestCase(deleteRequest, new SuccessChecker());
    }

    private class SuccessChecker implements IChecker {

        @Override
        public boolean check(HttpResponse response) {
            if(HttpCode.isSucess(response.getStatus())) {
                if(response.getData().contains(ErrorCode.OVERLAYVPN_SUCCESS)) {
                    return true;
                }
            }

            return false;
        }

    }

}
