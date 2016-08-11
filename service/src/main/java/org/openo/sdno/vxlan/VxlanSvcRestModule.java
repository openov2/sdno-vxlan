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

package org.openo.sdno.vxlan;

import org.openo.sdno.overlayvpn.inventory.sdk.DbOwerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VxLAN Service Rest Module Initialization.<br/>
 * 
 * @author
 * @version SDNO 0.5 2016-6-8
 */
public class VxlanSvcRestModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxlanSvcRestModule.class);

    /**
     * Start Rest Module.<br/>
     * 
     * @since SDNO 0.5
     */
    public void start() {
        LOGGER.info("=====Start Vxlan svc roa module=====");
        DbOwerInfo.init("vxlanSvc", "vxlandb");
    }

    /**
     * Stop Rest Module.<br/>
     * 
     * @since SDNO 0.5
     */
    public void stop() {
        LOGGER.info("=====Stop Vxlan svc roa module=====");
    }
}
