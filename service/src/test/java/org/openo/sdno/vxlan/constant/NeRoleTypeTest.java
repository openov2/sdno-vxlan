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

/**
 * <br>
 * 
 * @author
 * @version SDNO 0.5 Feb 16, 2017
 */
public class NeRoleTypeTest {

    @Test
    public void test() {
        NeRoleType demo1 = NeRoleType.CLOUDCPE;
        NeRoleType demo2 = NeRoleType.DC_R;
        NeRoleType demo3 = NeRoleType.LOCALCPE;
        NeRoleType demo4 = NeRoleType.VPC;
        NeRoleType demo5 = NeRoleType.UNKNOWN;
        assertTrue(demo1.getName().equals("cloudcpe"));
        assertTrue(demo2.getName().equals("dc-r"));
        assertTrue(demo3.getName().equals("localcpe"));
        assertTrue(demo4.getName().equals("vpc"));
        assertTrue(demo5.getName().equals(""));

    }

}
