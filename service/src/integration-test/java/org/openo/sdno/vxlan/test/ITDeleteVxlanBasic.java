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

import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.testframework.checker.ServiceExceptionChecker;
import org.openo.sdno.testframework.http.model.HttpModelUtils;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.replace.PathReplace;
import org.openo.sdno.testframework.testmanager.TestManager;
import org.openo.sdno.vxlan.checker.SuccessChecker;

public class ITDeleteVxlanBasic extends TestManager {

    private static final String DELETE_VXLAN_FAIL_TESTCASE =
            "src/integration-test/resources/testcase/deletevxlan.json";

    @Test
    public void DeleteVxlanFailTest() throws ServiceException {

        HttpRquestResponse httpObject = HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_VXLAN_FAIL_TESTCASE);
        HttpRequest request = httpObject.getRequest();
        request.setUri(PathReplace.replaceUuid("connectionid", request.getUri(), "8590&4888*"));

        execTestCase(request, new ServiceExceptionChecker(ErrorCode.OVERLAYVPN_PARAMETER_INVALID));
    }

    @Test
    public void DeleteVxlanSuccessTest() throws ServiceException {
        HttpRquestResponse httpObject = HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_VXLAN_FAIL_TESTCASE);
        HttpRequest request = httpObject.getRequest();
        request.setUri(PathReplace.replaceUuid("connectionid", request.getUri(), "testconnection"));

        execTestCase(request, new SuccessChecker());
    }
}
