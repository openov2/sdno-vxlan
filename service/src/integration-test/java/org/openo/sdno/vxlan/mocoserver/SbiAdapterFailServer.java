/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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

package org.openo.sdno.vxlan.mocoserver;

import org.openo.sdno.testframework.moco.MocoHttpServer;

public class SbiAdapterFailServer extends MocoHttpServer {

    private static final String CREATE_VXLAN_FAIL_FILE =
            "src/integration-test/resources/vxlansbiadapter/createvxlanfail.json";

    private static final String DELETE_VXLAN_FILE =
            "src/integration-test/resources/vxlansbiadapter/deletesbivxlan.json";

    private static final String DELETE_VXLAN_FILE_OLD =
            "src/integration-test/resources/vxlansbiadapter/deletevxlan.json";

    private static final String QUERY_WANSUBINF_FILE =
            "src/integration-test/resources/vxlansbiadapter/querywansubinterface.json";

    private static final String QUERY_VTEP_FILE = "src/integration-test/resources/vxlansbiadapter/queryvtep.json";

    @Override
    public void addRequestResponsePairs() {
        this.addRequestResponsePair(CREATE_VXLAN_FAIL_FILE);

        this.addRequestResponsePair(DELETE_VXLAN_FILE, new DeleteVxlanResponseHandler());

        this.addRequestResponsePair(DELETE_VXLAN_FILE_OLD, new DeleteVxlanResponseHandler());

        this.addRequestResponsePair(QUERY_WANSUBINF_FILE);

        this.addRequestResponsePair(QUERY_VTEP_FILE, new QueryVtepResponseHandler());
    }
}
