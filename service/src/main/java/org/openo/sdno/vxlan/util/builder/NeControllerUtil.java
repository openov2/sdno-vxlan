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

package org.openo.sdno.vxlan.util.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.esr.invdao.SdnControllerDao;
import org.openo.sdno.overlayvpn.esr.model.SdnController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to deal with Ne and controller database operation.<br>
 * 
 * @author
 * @version SDNO 0.5 Jan 12, 2017
 */
public class NeControllerUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeControllerUtil.class);

    private static final SdnControllerDao sdnDao = new SdnControllerDao();

    private static final NetworkElementInvDao neDao = new NetworkElementInvDao();

    /**
     * build Ne to Controller map with given neid to ne map.<br>
     * 
     * @param neIdToNeMap neid to ne map.
     * @return Ne to Controller map
     * @throws ParameterServiceException if input paramater is illegal.
     * @since SDNO 0.5
     */
    public static Map<NetworkElementMO, ControllerMO> getNeCtrlMap(Map<String, NetworkElementMO> neIdToNeMap)
            throws ParameterServiceException {
        Map<NetworkElementMO, ControllerMO> neToCtrlMap = new HashMap<NetworkElementMO, ControllerMO>();
        LOGGER.error("===============getNeCtrlMap1");
        for(NetworkElementMO ne : neIdToNeMap.values()) {
            try {
                for(String ctrlId : ne.getControllerID()) {
                    neToCtrlMap.put(ne, getController(ctrlId));
                }
            } catch(ServiceException e) {
                LOGGER.error("controller of ne is missing, ne id is" + ne.getId());
                throw new ParameterServiceException("controller of ne is missing.");
            }
        }

        return neToCtrlMap;

    }

    private static ControllerMO getController(String uuid) throws ServiceException {
        ControllerMO controller = new ControllerMO();
        LOGGER.error("===============getNeCtrlMap2");
        SdnController sdncontroller = sdnDao.querySdnControllerById(uuid);
        LOGGER.error("===============getNeCtrlMap3");
        controller.setId(sdncontroller.getSdnControllerId());
        controller.setName(sdncontroller.getName());
        controller.setProductName(sdncontroller.getProductName());
        controller.setDescription(sdncontroller.getDescription());
        controller.setVersion(sdncontroller.getVersion());
        controller.setVendor(sdncontroller.getVendor());
        return controller;
    }

    /**
     * get controller by ne id.<br>
     * 
     * @param neToCtrlMap ne to controller map.
     * @param neId ne uuid
     * @return controller corresponding to given neid.
     * @since SDNO 0.5
     */
    public static ControllerMO findCtrlByNeId(Map<NetworkElementMO, ControllerMO> neToCtrlMap, String neId) {
        for(Entry<NetworkElementMO, ControllerMO> entry : neToCtrlMap.entrySet()) {
            if(neId.equals(entry.getKey().getId())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static List<NetworkElementMO> getAllFullNe() throws ServiceException {
        List<NetworkElementMO> nes = neDao.getAllMO();
        List<NetworkElementMO> fullNes = new ArrayList<NetworkElementMO>();
        for(NetworkElementMO ne : nes) {
            NetworkElementMO newne = neDao.query(ne.getId());
            fullNes.add(newne);
        }

        return fullNes;
    }
}
