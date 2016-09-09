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

package org.openo.sdno.vxlan.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.sdno.overlayvpn.consts.CommConst;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.security.authentication.HttpContext;

public class VxlanRestParameterUtilTest {

    @Test
    public void testGetQueryVtepParam() {
        RestfulParametes restfulParametes = VxlanRestParameterUtil.getQueryVtepParam("testUuid");
        Map<String, String> headerMap = restfulParametes.getHeaderMap();
        assertEquals(headerMap.get(HttpContext.CONTENT_TYPE_HEADER), HttpContext.MEDIA_TYPE_JSON);
    }

    @Test
    public void testGetCreateVxlanInstanceParam() {

        List<NeVxlanInstance> vxlanNeInstanceList = new ArrayList<NeVxlanInstance>();
        NeVxlanInstance neVxlanInstance = new NeVxlanInstance();
        neVxlanInstance.setNeId("test");
        neVxlanInstance.setAdminStatus("active");
        neVxlanInstance.setOperStatus("active");
        vxlanNeInstanceList.add(neVxlanInstance);
        String expected = "[{\"tenantId\":null,\"name\":null,\"description\":null,\"modifyMask\":\"NOMODIFY\","
                + "\"operStatus\":\"active\",\"adminStatus\":\"active\",\"createTime\":null,\"controllerId\":null,\"vni\":null,"
                + "\"arpProxy\":\"false\",\"arpBroadcastSuppress\":\"false\",\"neId\":\"test\","
                + "\"vxlanInterfaceList\":null,\"vxlanTunnelList\":null,\"id\":null}]";

        try {
            RestfulParametes res = VxlanRestParameterUtil.getCreateVxlanInstanceParam(vxlanNeInstanceList, "testUuid");
            String resRawData = res.getRawData();

            assertEquals(expected, resRawData);
        } catch(ServiceException e) {
            fail("exception occured");
        }

    }

    /**
     * Tests getDeleteInstanceParam method. checks the header has X-Auth-Token <br>
     * 
     * @since SDNO 0.5
     */
    @Test
    public void testGetDeleteInstanceParam() {

        try {
            List<NeVxlanInstance> vxlanNeInstanceList = new ArrayList<NeVxlanInstance>();
            NeVxlanInstance neVxlanInstance = new NeVxlanInstance();
            neVxlanInstance.setNeId("test");
            neVxlanInstance.setAdminStatus("active");
            neVxlanInstance.setOperStatus("active");
            vxlanNeInstanceList.add(neVxlanInstance);
            RestfulParametes restfulParametes =
                    VxlanRestParameterUtil.getDeleteInstanceParam(vxlanNeInstanceList, "testUuid");
            Map<String, String> headerMap = restfulParametes.getHeaderMap();
        } catch(ServiceException e) {
            fail("exception occured");
        }

    }

    /**
     * Tests GetQueryWanInterfaceParam method. check whether input reflects in parameter map. <br>
     * 
     * @since SDNO 0.5
     */
    @Test
    public void testGetQueryWanInterfaceParam() {

        RestfulParametes res = VxlanRestParameterUtil.getQueryWanInterfaceParam("TestType", "testUuid");

        assertEquals("TestType", res.getParamMap().get(CommConst.DEVICE_WAN_SUB_INTERFACE_TYPE_PARAMETER));

    }
}
