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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.consts.CommConst;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.common.enums.EndpointType;
import org.openo.sdno.overlayvpn.model.common.enums.topo.TopologyType;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVtep;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.res.ResourcesUtil;
import org.openo.sdno.vxlan.util.exception.ThrowVxlanExcpt;

/**
 * Utility function to convert from NBI structure to SBI structure.<br>
 * 
 * @author
 * @version SDNO 0.5 03-June-2016
 */
public class ModelConvertUtil {

    private ModelConvertUtil() {
    }

    /**
     * Convert OverlayVPN structure to NeVxlan instances<br>
     * 
     * @param overlayVpnService - overlayVpn Data
     * @param deviceIdToNeVtepMap - Map of deviceId to VTEP
     * @param deviceIdToWansubInfMap - Map of deviceId to WAN sub interface
     * @param vniToConnectionIdMap - Map of VNI to connection ID
     * @return VxlanProducer - which convert from overlayVPN to NEVxlan instances
     * @throws ServiceException - when input parameter is invalid
     * @since SDNO 0.5
     */
    public static VxlanProducer convertFromOverlayVpnService(OverlayVpn overlayVpnService,
            Map<String, NeVtep> deviceIdToNeVtepMap, Map<String, WanSubInterface> deviceIdToWansubInfMap,
            Map<String, String> vniToConnectionIdMap) throws ServiceException {

        List<CommonVxlanBuilder> vxlanBuilders = new ArrayList<>();

        for(Connection conn : overlayVpnService.getVpnConnections()) {
            String strVNI = vniToConnectionIdMap.get(conn.getUuid());
            CommonVxlanBuilder vxlanBuilder = null;

            String connTopo = conn.getTopology();
            if(TopologyType.FULL_MESH.getName().equals(connTopo)) {
                vxlanBuilder = new FullMeshVxlanBuilder(conn, deviceIdToNeVtepMap, deviceIdToWansubInfMap, null,
                        conn.getEndpointGroups(), strVNI);
                vxlanBuilders.add(vxlanBuilder);
            } else if(TopologyType.HUB_SPOKE.getName().equals(connTopo)) {
                vxlanBuilder = new HubSpokeVxlanBuilder(conn, deviceIdToNeVtepMap, deviceIdToWansubInfMap, null,
                        conn.getEndpointGroups(), strVNI);
                vxlanBuilders.add(vxlanBuilder);
            } else if(TopologyType.POINT_TO_POINT.getName().equals(connTopo)) {
                vxlanBuilder = new FullMeshVxlanBuilder(conn, deviceIdToNeVtepMap, deviceIdToWansubInfMap, null,
                        conn.getEndpointGroups(), strVNI);
                vxlanBuilders.add(vxlanBuilder);
            } else {
                ThrowVxlanExcpt.throwParmaterInvalid("Invalid Topology type", conn.getTopology());
            }
        }

        VxlanProducer vxlanProducer = new VxlanProducer(vxlanBuilders);
        vxlanProducer.build();
        return vxlanProducer;
    }

    /**
     * Get VNI for Connection<br>
     * 
     * @param connections - list of Connection
     * @param vniToConnectionIdMap - VNI to connection Id map
     * @throws ServiceException - when input is invalid
     * @since SDNO 0.5
     */
    public static void getVniForConn(List<Connection> connections, Map<String, String> vniToConnectionIdMap)
            throws ServiceException {
        String connectionId = "";
        String connectionVni = "";
        for(Connection conn : connections) {
            connectionId = conn.getUuid();
            for(EndpointGroup epg : conn.getEndpointGroups()) {
                if(EndpointType.VNI.getName().equals(epg.getType())) {
                    connectionVni = epg.getEndpoints();
                }
            }
            vniToConnectionIdMap.put(connectionId, connectionVni);
        }
    }

    /**
     * Allocate VNI for Connection<br>
     * 
     * @param connections - List of connection information
     * @return Map of Connection ID to VNI
     * @throws ServiceException - when allocate VNI fails from allocation service
     * @since SDNO 0.5
     */
    public static Map<String, String> allocVniForConn(List<Connection> connections) throws ServiceException {
        InventoryDao<NeVxlanInstance> neVxlanInstanceDao = new InventoryDaoUtil<NeVxlanInstance>().getInventoryDao();
        Map<String, Object> filterMap = new HashMap<>();

        filterMap.put("connectionServiceId", CollectionUtils.collect(connections, new Transformer() {

            @Override
            public Object transform(Object arg0) {
                return ((Connection)arg0).getUuid();
            }
        }));
        String filter = JsonUtil.toJson(filterMap);
        List<NeVxlanInstance> dbQueryVxlanInstances =
                neVxlanInstanceDao.queryByFilter(NeVxlanInstance.class, filter, "connectionServiceId,vni").getData();

        Map<String, String> connectionIdToVniMap = new ConcurrentHashMap<String, String>();
        for(NeVxlanInstance dbVxlanInstance : dbQueryVxlanInstances) {
            String connectionId = dbVxlanInstance.getConnectionServiceId();
            if(!connectionIdToVniMap.containsKey(connectionId)) {
                connectionIdToVniMap.put(connectionId, dbVxlanInstance.getVni());
            }
        }

        List<Connection> connectionsWithoutVni = new ArrayList<>();
        for(Connection connection : connections) {
            if(!connectionIdToVniMap.containsKey(connection.getUuid())) {
                connectionsWithoutVni.add(connection);
            }
        }

        int connAmount = connectionsWithoutVni.size();
        if(connAmount > 0) {
            List<Long> vniList = ResourcesUtil.requestGloabelValue(CommConst.RES_VNI_POOLNAME_VXLAN,
                    CommConst.RES_VXALN_USER_LABEL, connAmount, CommConst.VXLAN_VNI_MIN, CommConst.VXLAN_VNI_MAX);
            if(CollectionUtils.isEmpty(vniList) || (vniList.size() != connAmount)) {
                throw new ServiceException(ErrorCode.RESOURCE_ALLOC_FAILED, "allocate vni failed.");
            }

            for(int i = 0; i < connAmount; i++) {
                connectionIdToVniMap.put(connectionsWithoutVni.get(i).getUuid(), String.valueOf(vniList.get(i)));
            }
        }

        return connectionIdToVniMap;
    }
}
