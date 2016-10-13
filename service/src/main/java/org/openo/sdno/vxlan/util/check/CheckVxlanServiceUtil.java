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

package org.openo.sdno.vxlan.util.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;

/**
 * Utility function to support VxLAN service deployment.<br>
 * 
 * @author
 * @version SDNO 0.5 03-June-2016
 */
public class CheckVxlanServiceUtil {

    private CheckVxlanServiceUtil() {
    }

    /**
     * Check whether same VxLAN tunnel exists in the database<br>
     * 
     * @param tenantId - Tenant ID
     * @param createVxlanServiceList - List of VxLAN services
     * @return List of VxLAN tunnels
     * @throws ServiceException - when input is invalid or query from database fails
     * @since SDNO 0.5
     */
    public static List<NeVxlanTunnel> checkSameTunnelInDb(String tenantId, List<NeVxlanTunnel> createVxlanServiceList)
            throws ServiceException {

        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("connectionServiceId", CollectionUtils.collect(createVxlanServiceList, new Transformer() {

            @Override
            public Object transform(Object arg0) {
                return ((NeVxlanTunnel)arg0).getConnectionServiceId();
            }
        }));

        filterMap.put("tenantId", Arrays.asList(tenantId));
        String filter = JsonUtil.toJson(filterMap);

        InventoryDao<NeVxlanTunnel> vxlanTunnelDao = new InventoryDaoUtil<NeVxlanTunnel>().getInventoryDao();
        List<NeVxlanTunnel> dbQueryTunnels =
                vxlanTunnelDao
                        .queryByFilter(NeVxlanTunnel.class, filter,
                                "connectionServiceId,vxlanInstanceId,neId,sourceAddress,peerNeId,destAddress")
                        .getData();

        Map<String, NeVxlanTunnel> tunnelUniqueIdToTunnelMap = new HashMap<String, NeVxlanTunnel>();
        for(NeVxlanTunnel dbTunnel : dbQueryTunnels) {
            tunnelUniqueIdToTunnelMap.put(dbTunnel.generateUniqueServiceId(dbTunnel), dbTunnel);
        }

        List<NeVxlanTunnel> tunnelToInsertDb = new ArrayList<NeVxlanTunnel>();
        List<NeVxlanTunnel> tunnelToAbandon = new ArrayList<NeVxlanTunnel>();
        List<NeVxlanTunnel> sameTunnelInDb = new ArrayList<NeVxlanTunnel>();
        for(NeVxlanTunnel tunnelToCreate : createVxlanServiceList) {
            String vxlanTunnelUniqueId = tunnelToCreate.generateUniqueServiceId(tunnelToCreate);

            if(tunnelUniqueIdToTunnelMap.containsKey(vxlanTunnelUniqueId)) {
                tunnelToAbandon.add(tunnelToCreate);
                sameTunnelInDb.add(tunnelUniqueIdToTunnelMap.get(vxlanTunnelUniqueId));
            } else {
                tunnelToInsertDb.add(tunnelToCreate);
            }
        }

        if(CollectionUtils.isNotEmpty(tunnelToAbandon)) {
            createVxlanServiceList.removeAll(tunnelToAbandon);
        }
        if(CollectionUtils.isNotEmpty(sameTunnelInDb)) {
            createVxlanServiceList.addAll(sameTunnelInDb);
        }

        return tunnelToInsertDb;
    }

}
