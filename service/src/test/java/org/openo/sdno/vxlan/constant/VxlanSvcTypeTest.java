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

package org.openo.sdno.vxlan.constant;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VxlanSvcTypeTest {

    VxlanSvcType demo1 = VxlanSvcType.IPV4_OVER_IPV6;

    VxlanSvcType demo2 = VxlanSvcType.IPV6_OVER_IPV4;

    VxlanSvcType demo3 = VxlanSvcType.L3_GW_VXLAN;

    VxlanSvcType demo4 = VxlanSvcType.VXLAN;

    @Test
    public void test() {
        assertTrue(demo1.getName().equals("ipv4 over ipv6"));
        assertTrue(demo2.getName().equals("ipv6 over ipv4"));
        assertTrue(demo3.getName().equals("l3-gw-vxlan"));
        assertTrue(demo4.getName().equals("vxlan"));
    }

}
