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

package org.openo.sdno.vxlan.mococlass;

import java.util.Arrays;
import java.util.List;

import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.res.AllocIdResource;
import org.openo.sdno.overlayvpn.res.AllocIdResourceInvDao;
import org.openo.sdno.overlayvpn.result.ResultRsp;

import mockit.Mock;
import mockit.MockUp;

public final class MockAllocIdResourceInvDao extends MockUp<AllocIdResourceInvDao> {

    @Mock
    public ResultRsp<List<AllocIdResource>> batchQuery(String poolName, List<Long> idList) {
        AllocIdResource idResource = new AllocIdResource();
        return new ResultRsp<List<AllocIdResource>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(idResource));
    }

    @Mock
    public void batchDelete(List<AllocIdResource> resourceList) {
        return;
    }

    @Mock
    public void batchInsert(List<AllocIdResource> resourceList) {
        return;
    }

}
