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

package org.openo.sdno.vxlan.service.impl.delete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.consts.CommConst;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInterface;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.res.ResourcesUtil;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete VxLAN Service.<br>
 * 
 * @author
 * @version SDNO 0.5 03-Jun-2016
 */
public class DeleteVxlanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteVxlanService.class);

    private DeleteVxlanService() {

    }

    /**
     * Delete the VxLAN service from the database<br>
     * 
     * @param connectionUuid - Connection Id
     * @param tenantId - Tenant Id
     * @param deviceId - Device Id
     * @return - Connection information for the deleted VxLan service
     * @throws ServiceException - when input is invalid or database delete fails
     * @since SDNO 0.5
     */
    public static ResultRsp<Connection> delete(String connectionUuid, String tenantId) throws ServiceException {
        ResultRsp<String> resultRsp = new ResultRsp<String>();
        boolean isDelSuccess = true;

        // Delete Interface Data
        ResultRsp<String> delVxlanInfResult = deleteInterface(connectionUuid, tenantId);
        if(!delVxlanInfResult.isSuccess()) {
            LOGGER.error("Delete Vxlan Interface Failed!!");
            isDelSuccess = false;
        }

        // Delete Tunnel Data
        ResultRsp<String> delVxlanTunnelResult = deleteTunnel(connectionUuid, tenantId);
        if(!delVxlanTunnelResult.isSuccess()) {
            LOGGER.error("Delete Vxlan Tunnel Failed!!");
            isDelSuccess = false;
        }

        // Delete Instance Data
        ResultRsp<String> delVxlanInstanceResult = deleteInstance(connectionUuid, tenantId);
        if(!delVxlanInstanceResult.isSuccess()) {
            LOGGER.error("Delete Vxlan Instance Failed!!");
            isDelSuccess = false;
        }

        if(!isDelSuccess) {
            resultRsp.setErrorCode(ErrorCode.OVERLAYVPN_FAILED);
        }

        return new ResultRsp<Connection>(resultRsp);
    }

    private static void freeVniRes(List<NeVxlanInstance> deleteVxlanInstanceList) throws ServiceException {
        List<Long> vniipList = new ArrayList<Long>();
        for(NeVxlanInstance instance : deleteVxlanInstanceList) {
            vniipList.add(Long.valueOf(instance.getVni()));
        }

        ResultRsp<String> freeResult = ResourcesUtil.freeGlobalValueList(CommConst.RES_VNI_POOLNAME_VXLAN,
                CommConst.RES_VXALN_USER_LABEL, vniipList);
        if(!freeResult.isSuccess()) {
            LOGGER.warn("freeVniRes error.");
        }
    }

    private static ResultRsp<String> deleteInterface(String connectionUuid, String tenantId) throws ServiceException {

        InventoryDao<NeVxlanInterface> neVxlanInterfaceDao = new InventoryDaoUtil<NeVxlanInterface>().getInventoryDao();
        ResultRsp<List<NeVxlanInterface>> vxlanInfResultRsp =
                neVxlanInterfaceDao.queryByFilter(NeVxlanInterface.class, getFilter(connectionUuid, tenantId), null);
        if(!vxlanInfResultRsp.isSuccess()) {
            LOGGER.error("Query Vxlan Interface failed!!");
            throw new ServiceException("Query Vxlan Interface failed");
        }

        List<NeVxlanInterface> vxlanInterfaceList = vxlanInfResultRsp.getData();
        if(CollectionUtils.isEmpty(vxlanInterfaceList)) {
            LOGGER.warn("Query Vxlan Interface return empty!!");
            return new ResultRsp<String>(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        @SuppressWarnings("unchecked")
        List<String> deleteInfUuidList =
                new ArrayList<String>(CollectionUtils.collect(vxlanInterfaceList, new Transformer() {

                    @Override
                    public Object transform(Object arg0) {
                        return ((NeVxlanInterface)arg0).getUuid();
                    }
                }));

        return neVxlanInterfaceDao.batchDelete(NeVxlanInterface.class, deleteInfUuidList);
    }

    @SuppressWarnings("unchecked")
    private static ResultRsp<String> deleteTunnel(String connectionUuid, String tenantId) throws ServiceException {

        InventoryDao<NeVxlanTunnel> neVxlanTunnelDao = new InventoryDaoUtil<NeVxlanTunnel>().getInventoryDao();
        ResultRsp<List<NeVxlanTunnel>> vxlanTunnelResultRsp =
                neVxlanTunnelDao.queryByFilter(NeVxlanTunnel.class, getFilter(connectionUuid, tenantId), null);
        if(!vxlanTunnelResultRsp.isSuccess()) {
            LOGGER.error("Query Vxlan Tunnel failed!!");
            throw new ServiceException("Query Vxlan Tunnel failed");
        }

        List<NeVxlanTunnel> vxlanTunnelList = vxlanTunnelResultRsp.getData();
        if(CollectionUtils.isEmpty(vxlanTunnelList)) {
            LOGGER.warn("Query Vxlan Tunnel return empty!!");
            return new ResultRsp<String>(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        List<String> deleteTunnelUuidList =
                new ArrayList<String>(CollectionUtils.collect(vxlanTunnelList, new Transformer() {

                    @Override
                    public Object transform(Object arg0) {
                        return ((NeVxlanTunnel)arg0).getUuid();
                    }
                }));

        return neVxlanTunnelDao.batchDelete(NeVxlanTunnel.class, deleteTunnelUuidList);
    }

    @SuppressWarnings("unchecked")
    private static ResultRsp<String> deleteInstance(String connectionUuid, String tenantId) throws ServiceException {

        InventoryDao<NeVxlanInstance> neVxlanInstanceDao = new InventoryDaoUtil<NeVxlanInstance>().getInventoryDao();
        ResultRsp<List<NeVxlanInstance>> vxlanInstanceResultRsp =
                neVxlanInstanceDao.queryByFilter(NeVxlanInstance.class, getFilter(connectionUuid, tenantId), null);
        if(!vxlanInstanceResultRsp.isSuccess()) {
            LOGGER.error("Query Vxlan Instance failed!!");
            throw new ServiceException("Query Vxlan Instance failed");
        }

        List<NeVxlanInstance> vxlanInstanceList = vxlanInstanceResultRsp.getData();
        if(CollectionUtils.isEmpty(vxlanInstanceList)) {
            LOGGER.warn("Query Vxlan Instance return empty!!");
            return new ResultRsp<String>(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        List<String> deleteInstanceUuidList =
                new ArrayList<>(CollectionUtils.collect(vxlanInstanceList, new Transformer() {

                    @Override
                    public Object transform(Object arg0) {
                        return ((NeVxlanInstance)arg0).getUuid();
                    }
                }));

        ResultRsp<String> delVxlanInsResult =
                neVxlanInstanceDao.batchDelete(NeVxlanInstance.class, deleteInstanceUuidList);

        if(delVxlanInsResult.isSuccess()) {

            // Free All VNI resource
            freeVniRes(vxlanInstanceList);
        }

        return delVxlanInsResult;
    }

    private static String getFilter(String connectionUuid, String tenantId) {
        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("connectionServiceId", Arrays.asList(connectionUuid));
        if(StringUtils.isNotEmpty(tenantId)) {
            filterMap.put("tenantId", Arrays.asList(tenantId));
        }
        return JsonUtil.toJson(filterMap);
    }
}
