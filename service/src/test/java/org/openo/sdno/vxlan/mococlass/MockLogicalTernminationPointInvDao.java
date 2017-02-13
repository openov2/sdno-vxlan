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
import java.util.List;

import org.openo.sdno.overlayvpn.brs.invdao.LogicalTernminationPointInvDao;
import org.openo.sdno.overlayvpn.brs.model.LogicalTernminationPointMO;

import mockit.Mock;
import mockit.MockUp;

public class MockLogicalTernminationPointInvDao extends MockUp<LogicalTernminationPointInvDao> {

    @Mock
    public List<LogicalTernminationPointMO> getAllMO() {
        List<LogicalTernminationPointMO> datas = new ArrayList<LogicalTernminationPointMO>();
        LogicalTernminationPointMO ltp1 = new LogicalTernminationPointMO();
        ltp1.setId("neid1");
        ltp1.setIpAddress("192.168.1.1");
        ltp1.setName("Ltp1");

        LogicalTernminationPointMO ltp2 = new LogicalTernminationPointMO();
        ltp2.setId("neid2");
        ltp2.setIpAddress("192.168.2.1");
        ltp2.setName("Ltp2");

        datas.add(ltp2);
        datas.add(ltp1);
        return datas;
    }

}
