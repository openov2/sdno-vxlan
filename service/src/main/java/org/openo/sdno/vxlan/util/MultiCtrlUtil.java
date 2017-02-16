/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;

/**
 * Utility file to deploy VxLAN service on multiple controller.<br>
 * 
 * @author
 * @version SDNO 0.5 03-June-2016
 */
public class MultiCtrlUtil {

    private MultiCtrlUtil() {

    }

    /**
     * Get controller UUID to list of NE VxLan instance<br>
     * 
     * @param createVxlanInstanceList - NeVxlan Instance list
     * @param neIdToCtrlMap - NE ID to controller info mapping
     * @return Map of Controller UUID to list of NE VxLan instances
     * @throws ServiceException - when input is invalid
     * @since SDNO 0.5
     */
    public static Map<String, List<NeVxlanInstance>> getCtrUuidToInstanceMap(
            List<NeVxlanInstance> createVxlanInstanceList, Map<String, ControllerMO> neIdToCtrlMap)
            throws ServiceException {

        if(CollectionUtils.isEmpty(createVxlanInstanceList) || MapUtils.isEmpty(neIdToCtrlMap)) {
            throw new ServiceException("Instance List is empty or neIdToCtrlMap is empty");
        }

        Map<String, List<NeVxlanInstance>> ctrlUuidToVxlanListMap = new ConcurrentHashMap<>();
        for(NeVxlanInstance tempVxlanInstance : createVxlanInstanceList) {
            ControllerMO tempControllerMO = neIdToCtrlMap.get(tempVxlanInstance.getNeId());
            if(null == tempControllerMO) {
                throw new ServiceException();
            }

            String contronllerUuid = tempControllerMO.getObjectId();
            if(null == ctrlUuidToVxlanListMap.get(contronllerUuid)) {
                ctrlUuidToVxlanListMap.put(contronllerUuid, new ArrayList<NeVxlanInstance>());
            }

            ctrlUuidToVxlanListMap.get(contronllerUuid).add(tempVxlanInstance);
        }

        return ctrlUuidToVxlanListMap;
    }

}
