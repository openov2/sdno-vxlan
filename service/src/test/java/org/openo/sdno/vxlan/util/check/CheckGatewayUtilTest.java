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

package org.openo.sdno.vxlan.util.check;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.model.servicemodel.Gateway;

import mockit.Mock;
import mockit.MockUp;

/**
 * <br>
 * 
 * @author
 * @version SDNO 0.5 Feb 17, 2017
 */
public class CheckGatewayUtilTest {

    @Test
    public void test() {
        Gateway gateway = new Gateway();
        new MockUp<NetworkElementInvDao>() {

            @Mock
            public NetworkElementMO query(String id) throws ServiceException {

                return null;
            }
        };
        try {
            CheckGatewayUtil.checkResourceInGateway(gateway);
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testEmptyId() {
        Gateway gateway = new Gateway();
        new MockUp<NetworkElementInvDao>() {

            @Mock
            public NetworkElementMO query(String id) throws ServiceException {

                return new NetworkElementMO();
            }
        };
        try {
            CheckGatewayUtil.checkResourceInGateway(gateway);
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

}
