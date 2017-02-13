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

package org.openo.sdno.vxlan.test;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.model.v2.vxlan.NbiVxlanTunnel;
import org.openo.sdno.testframework.checker.IChecker;
import org.openo.sdno.testframework.http.model.HttpModelUtils;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.testmanager.TestManager;
import org.openo.sdno.testframework.topology.Topology;
import org.openo.sdno.vxlan.drivermanager.DriverRegisterManager;
import org.openo.sdno.vxlan.mocoserver.SbiAdapterSuccessServer;

public class CreateVxlanSuccess extends TestManager {

    private SbiAdapterSuccessServer sbiAdapterServer = new SbiAdapterSuccessServer();

    private static final String CREATE_VXLAN_TESTCASE = "src/integration-test/resources/testcase/createvxlan.json";

    private static final String QUERY_VXLAN_BATCH = "src/integration-test/resources/testcase/queryvxlanbatch.json";

    private static final String QUERY_VXLAN_SINGLE = "src/integration-test/resources/testcase/queryvxlansingle.json";

    private static final String DELETE_VXLAN_TESTCASE = "src/integration-test/resources/testcase/deletenbivxlan.json";

    private static final String TOPODATA_PATH = "src/integration-test/resources/topodata";

    private Topology topo = new Topology(TOPODATA_PATH);

    @Before
    public void setup() throws ServiceException {
        topo.createInvTopology();
        DriverRegisterManager.registerDriver();
        sbiAdapterServer.start();
    }

    @After
    public void tearDown() throws ServiceException {
        sbiAdapterServer.stop();
        DriverRegisterManager.unRegisterDriver();
        topo.clearInvTopology();
    }

    @Test
    public void CreateVxlanSuccessTest1() throws ServiceException {

        HttpRquestResponse httpCreateObject = HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_VXLAN_TESTCASE);
        HttpRequest createRequest = httpCreateObject.getRequest();
        List<NbiVxlanTunnel> vxlanTunnels =
                JsonUtil.fromJson(createRequest.getData(), new TypeReference<List<NbiVxlanTunnel>>() {});

        createRequest.setData(JsonUtil.toJson(vxlanTunnels));
        execTestCase(createRequest, new SuccessChecker());
        System.out.println("create finished, start to undeploy.");

        HttpRquestResponse queryHttpObject = HttpModelUtils.praseHttpRquestResponseFromFile(QUERY_VXLAN_SINGLE);
        HttpRequest queryRequest = queryHttpObject.getRequest();
        execTestCase(queryRequest, new SuccessChecker());

        HttpRquestResponse queryHttpObjectBatch = HttpModelUtils.praseHttpRquestResponseFromFile(QUERY_VXLAN_BATCH);
        HttpRequest queryRequestBatch = queryHttpObject.getRequest();
        execTestCase(queryRequest, new SuccessChecker());

        HttpRquestResponse deleteHttpObject = HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_VXLAN_TESTCASE);
        HttpRequest deleteRequest = deleteHttpObject.getRequest();
        execTestCase(deleteRequest, new SuccessChecker());
    }

    private class SuccessChecker implements IChecker {

        @Override
        public boolean check(HttpResponse response) {
            if(HttpCode.isSucess(response.getStatus())) {
                return true;
            }

            return false;
        }

    }

}
