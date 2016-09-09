/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.vxlan.mockdata.VxlanServiceData;
import org.openo.sdno.vxlan.mococlass.MockInventoryDaoUtil;

import mockit.Mock;
import mockit.MockUp;

public class CheckVxlanServiceUtilTest {

    VxlanServiceData vxlanServiceData = new VxlanServiceData();

    @Before
    public void setUp() {
        new MockInventoryDaoUtil<NeVxlanTunnel>();
        new MockTunnelInventoryDao();
    }

    @Test
    public void testCheckSameTunnelInDb() throws ServiceException {
        CheckVxlanServiceUtil.checkSameTunnelInDb("tenantId", vxlanServiceData.neVxlanTunnelList());
    }

    private final class MockTunnelInventoryDao extends MockUp<InventoryDao<NeVxlanTunnel>> {

        @Mock
        ResultRsp queryByFilter(Class clazz, String filter, String queryResultFields) throws ServiceException {

            ResultRsp<List<NeVxlanTunnel>> resultRsp = new ResultRsp<List<NeVxlanTunnel>>(ErrorCode.OVERLAYVPN_SUCCESS);
            resultRsp.setData(vxlanServiceData.neVxlanTunnelList());
            return resultRsp;

        }

    }
}
