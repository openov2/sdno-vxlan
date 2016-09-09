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

package org.openo.sdno.vxlan.util.vxlanbuilder;

import java.util.List;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;

/**
 * Interface Class of VxLAN builder.<br>
 * 
 * @author
 * @version SDNO 0.5 03-Jun-2016
 */
public interface VxlanBuilder {

    /**
     * Build VxLAN instance.<br>
     * 
     * @throws ServiceException
     * @since SDNO 0.5
     */
    void buildNeVxlanInstance() throws ServiceException;

    /**
     * Get VxLAN instances.<br>
     * 
     * @return list of Oper VxLAN instances
     * @since SDNO 0.5
     */
    List<NeVxlanInstance> getOperVxlanInstances();

    /**
     * Get other VxLAN instances.<br>
     * 
     * @return list of Other VxLAN instances
     * @since SDNO 0.5
     */
    List<NeVxlanInstance> getOtherVxlanInstances();

}
