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

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.testframework.checker.IChecker;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.testmanager.TestManager;

public class CreateVxlanBasicFail extends TestManager {

    private static final String CREATE_VXLAN_FAIL1_TESTCASE =
            "src/integration-test/resources/testcase/createvxlanfailneidsame.json";

    private static final String CREATE_VXLAN_FAIL2_TESTCASE =
            "src/integration-test/resources/testcase/createvxlanfailnetypewrong.json";

    @Before
    public void setup() throws ServiceException {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void VxlanFailTest1() throws ServiceException {
        execTestCase(new File(CREATE_VXLAN_FAIL1_TESTCASE), new SuccessChecker());
    }

    @Test
    public void VxlanFailTest2() throws ServiceException {
        execTestCase(new File(CREATE_VXLAN_FAIL2_TESTCASE), new SuccessChecker());
    }

    private class SuccessChecker implements IChecker {

        @Override
        public boolean check(HttpResponse response) {
            if(!HttpCode.isSucess(response.getStatus())) {
                return true;
            }

            return false;
        }

    }

}
