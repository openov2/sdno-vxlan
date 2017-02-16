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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;

/**
 * VxLAN Producer.<br>
 * 
 * @author
 * @version SDNO 0.5 02-June-2016
 */
public class VxlanProducer {

    private List<CommonVxlanBuilder> vxlanBuilders = new ArrayList<>();

    private List<NeVxlanInstance> operVxlanInstances = new ArrayList<>();

    private List<NeVxlanInstance> otherVxlanInstances = new ArrayList<>();

    VxlanProducer(List<CommonVxlanBuilder> vxlanBuilders) {
        if(CollectionUtils.isNotEmpty(vxlanBuilders)) {
            this.vxlanBuilders = vxlanBuilders;
        }
    }

    /**
     * Common VxLAN builder<br>
     * 
     * @throws ServiceException - when vxlanBuilders/neVxlanInstances parameter is invalid
     * @since SDNO 0.5
     */
    public void build() throws ServiceException {
        for(CommonVxlanBuilder vxlanBuilder : this.vxlanBuilders) {
            vxlanBuilder.buildNeVxlanInstance();
            this.operVxlanInstances.addAll(vxlanBuilder.getOperVxlanInstances());
            this.otherVxlanInstances.addAll(vxlanBuilder.getOtherVxlanInstances());
        }
    }

    public List<NeVxlanInstance> getOperVxlanInstances() {
        return this.operVxlanInstances;
    }

    public List<NeVxlanInstance> getOtherVxlanInstances() {
        return this.otherVxlanInstances;
    }

    /**
     * Get all NeVxlanInstance.<br>
     * 
     * @return List of NeVxlanInstance
     * @since SDNO 0.5
     */
    public List<NeVxlanInstance> getAllVxlanInstances() {
        List<NeVxlanInstance> allVxlanInstances = new ArrayList<>();
        allVxlanInstances.addAll(this.operVxlanInstances);
        allVxlanInstances.addAll(this.otherVxlanInstances);
        return allVxlanInstances;
    }

}
