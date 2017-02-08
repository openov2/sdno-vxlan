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

/**
 * vxlan service type constant class<br>
 *
 * @author
 * @version SDNO 0.5 2017年1月10日
 */
public enum VxlanSvcType {
    VXLAN(0), IPV6_OVER_IPV4(1), IPV4_OVER_IPV6(2), L3_GW_VXLAN(3);

    private int value;

    /**
     * Constructor<br>
     *
     * @param value
     * @since SDNO 0.5
     */
    VxlanSvcType(int value) {
        this.value = value;
    }

    /**
     * return name of the element.<br>
     *
     * @return
     * @since SDNO 0.5
     */
    public String getName() {
        switch(value) {
            case 0:
                return "vxlan";
            case 2:
                return "ipv6 over ipv4";
            case 3:
                return "ipv4 over ipv4";
            case 4:
                return "l3-gw-vxlan";
            default:
                return "";

        }
    }
}
