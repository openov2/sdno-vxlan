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
 * Ne type constant class<br>
 *
 * @author
 * @version SDNO 0.5 2017年1月9日
 */
public enum NeRoleType {
    UNKNOWN(-1), LOCALCPE(0), CLOUDCPE(1), VPC(2), DC_R(3);

    private int value;

    NeRoleType(int value) {
        this.value = value;
    }

    public String getName() {

        switch(value) {
            case 0:
                return "localcpe";

            case 1:
                return "cloudcpe";

            case 2:
                return "vpc";

            case 3:
                return "dc-r";

            default:
                return "";
        }
    }
}
