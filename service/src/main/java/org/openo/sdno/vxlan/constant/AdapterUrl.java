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
 * adapter url constance calss.<br>
 * 
 * @author
 * @version SDNO 0.5 Jan 16, 2017
 */
public class AdapterUrl {

    public static final String VXLAN_ADAPTER_BASE_URL = "/openoapi/sbi-vxlan";

    public static final String BATCH_CREATE_VXLAN_INSTANCE = "/v1/batch-create-vxlan";

    public static final String BATCH_DELETE_VXLAN_INSTANCE = "/v1/device/{0}/batch-delete-vxlan";

    private AdapterUrl() {
        // private constructor
    }

}
