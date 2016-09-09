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

package org.openo.sdno.vxlan.service.impl.create;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.common.enums.ActionStatus;
import org.openo.sdno.overlayvpn.model.common.enums.AdminStatus;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInterface;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.objreflectoper.UuidAllocUtil;
import org.openo.sdno.vxlan.util.MultiCtrlUtil;
import org.openo.sdno.vxlan.util.check.CheckVxlanServiceUtil;
import org.openo.sdno.vxlan.util.vxlanbuilder.VxlanProducer;

/**
 * Create and deploy VxLAN service<br>
 * 
 * @author
 * @version SDNO 0.5 03-Jun-2016
 */
public class CreateVxlanService {

    private CreateVxlanService() {
    }

    /**
     * Deploy VxLAN instance on the controller<br>
     * 
     * @param createVxlanInstanceList - List of VxLAN services
     * @param neIdToCtrlMap - Device ID to Controller Mapping
     * @return Map of Controller UUID to VxLAN services
     * @throws ServiceException -when deploying on controller fails or updating database fails
     * @since SDNO 0.5
     */
    public static ResultRsp<Map<String, List<NeVxlanInstance>>>
            deployVxlanInstance(List<NeVxlanInstance> createVxlanInstanceList, Map<String, ControllerMO> neIdToCtrlMap)
                    throws ServiceException {

        // Get Controller UUID to VxLAN instance mapping
        Map<String, List<NeVxlanInstance>> ctrlUuidToVxlanListMap =
                MultiCtrlUtil.getCtrUuidToInstanceMap(createVxlanInstanceList, neIdToCtrlMap);

        // Deploy the VxLAN on the controller and get the operation result
        ResultRsp<Map<String, List<NeVxlanInstance>>> deployResult =
                VxlanSbiAdapter.deployVxlanInstanceByCtrl(ctrlUuidToVxlanListMap);

        // For all successfully deployed instances, change the status to active and update database
        List<NeVxlanInstance> createSuccVxlanInstances = new ArrayList<NeVxlanInstance>();
        List<NeVxlanTunnel> createSuccVxlanTunnels = new ArrayList<NeVxlanTunnel>();
        List<NeVxlanInterface> createSuccVxlanInterfaces = new ArrayList<NeVxlanInterface>();

        for(List<NeVxlanInstance> createVxlanInstances : deployResult.getData().values()) {
            if(CollectionUtils.isEmpty(createVxlanInstances)) {
                continue;
            }

            for(NeVxlanInstance createVxlanInstance : createVxlanInstances) {
                createVxlanInstance.setActionState(ActionStatus.NORMAL.getName());
                createVxlanInstance.setAdminStatus(AdminStatus.ACTIVE.getName());

                for(NeVxlanTunnel createVxlanTunnel : createVxlanInstance.getVxlanTunnelList()) {
                    createVxlanTunnel.setActionState(ActionStatus.NORMAL.getName());
                    createVxlanTunnel.setAdminStatus(AdminStatus.ACTIVE.getName());
                }
                createSuccVxlanTunnels.addAll(createVxlanInstance.getVxlanTunnelList());

                for(NeVxlanInterface createVxlanInterface : createVxlanInstance.getVxlanInterfaceList()) {
                    createVxlanInterface.setActionState(ActionStatus.NORMAL.getName());
                    createVxlanInterface.setAdminStatus(AdminStatus.ACTIVE.getName());
                }
                createSuccVxlanInterfaces.addAll(createVxlanInstance.getVxlanInterfaceList());
            }

            createSuccVxlanInstances.addAll(createVxlanInstances);
        }

        (new InventoryDaoUtil<NeVxlanInstance>()).getInventoryDao().update(NeVxlanInstance.class,
                createSuccVxlanInstances, "adminStatus,actionState,externalId");
        (new InventoryDaoUtil<NeVxlanTunnel>()).getInventoryDao().update(NeVxlanTunnel.class, createSuccVxlanTunnels,
                "adminStatus,actionState,externalId");
        (new InventoryDaoUtil<NeVxlanInterface>()).getInventoryDao().update(NeVxlanInterface.class,
                createSuccVxlanInterfaces, "adminStatus,actionState,externalId");

        return deployResult;
    }

    /**
     * Create VxLan Components.<br>
     * 
     * @param tenantId tenant id
     * @param deviceIdToCtrlMap Map of Device id to Controller
     * @param vxlanProducer VxLan Producer
     * @return OverlayVpn Object
     * @throws ServiceException when failed
     * @since SDNO 0.5
     */
    public static ResultRsp<OverlayVpn> createVxlanComponents(String tenantId,
            Map<String, ControllerMO> deviceIdToCtrlMap, VxlanProducer vxlanProducer) throws ServiceException {

        List<NeVxlanInstance> vxlanInstanceList = vxlanProducer.getAllVxlanInstances();

        setIdAndStatus(vxlanInstanceList, deviceIdToCtrlMap);

        List<NeVxlanTunnel> vxlanTunnelList = new ArrayList<NeVxlanTunnel>();
        List<NeVxlanInterface> vxlanInterfaceList = new ArrayList<NeVxlanInterface>();

        for(NeVxlanInstance tmpVxlanInstanceVpn : vxlanInstanceList) {
            vxlanTunnelList.addAll(
                    CheckVxlanServiceUtil.checkSameTunnelInDb(tenantId, tmpVxlanInstanceVpn.getVxlanTunnelList()));
            vxlanInterfaceList.addAll(tmpVxlanInstanceVpn.getVxlanInterfaceList());
        }

        (new InventoryDaoUtil<NeVxlanInstance>()).getInventoryDao().batchInsert(vxlanInstanceList);
        (new InventoryDaoUtil<NeVxlanInterface>()).getInventoryDao().batchInsert(vxlanInterfaceList);
        (new InventoryDaoUtil<NeVxlanTunnel>()).getInventoryDao().batchInsert(vxlanTunnelList);

        return deployVxlanComponents(tenantId, deviceIdToCtrlMap, vxlanInstanceList);
    }

    private static void setIdAndStatus(List<NeVxlanInstance> createVxlanInstanceList,
            Map<String, ControllerMO> deviceIdToCtrlMap) {
        for(NeVxlanInstance tempNeVxlanInstance : createVxlanInstanceList) {
            UuidAllocUtil.allocUuid(tempNeVxlanInstance);
            tempNeVxlanInstance.setAdminStatus(AdminStatus.INACTIVE.getName());
            tempNeVxlanInstance.setActionState(ActionStatus.CREATING.getName());

            String instanceUuid = tempNeVxlanInstance.getUuid();
            String controllerId = deviceIdToCtrlMap.get(tempNeVxlanInstance.getNeId()).getObjectId();
            tempNeVxlanInstance.setControllerId(controllerId);

            for(NeVxlanTunnel neVxlanTunnel : tempNeVxlanInstance.getVxlanTunnelList()) {
                neVxlanTunnel.setVxlanInstanceId(instanceUuid);
                neVxlanTunnel.setControllerId(controllerId);
                neVxlanTunnel.setAdminStatus(AdminStatus.INACTIVE.getName());
                neVxlanTunnel.setActionState(ActionStatus.CREATING.getName());
            }

            for(NeVxlanInterface neVxlanInterface : tempNeVxlanInstance.getVxlanInterfaceList()) {
                neVxlanInterface.setVxlanInstanceId(instanceUuid);
                neVxlanInterface.setControllerId(controllerId);
                neVxlanInterface.setAdminStatus(AdminStatus.INACTIVE.getName());
                neVxlanInterface.setActionState(ActionStatus.CREATING.getName());
            }
        }
    }

    /**
     * Deploy VxLan Components.<br>
     * 
     * @param tenantId Tenant Id
     * @param deviceIdToCtrlMap Map of Device Id to Controller
     * @param createVxlanInstances VxLan Instance need to deploy
     * @return operation result with OverlayVpn data
     * @throws ServiceException throws when operate failed
     * @since SDNO 0.5
     */
    public static ResultRsp<OverlayVpn> deployVxlanComponents(String tenantId,
            Map<String, ControllerMO> deviceIdToCtrlMap, List<NeVxlanInstance> createVxlanInstances)
            throws ServiceException {

        ResultRsp<Map<String, List<NeVxlanInstance>>> createInstanceRsp =
                CreateVxlanService.deployVxlanInstance(createVxlanInstances, deviceIdToCtrlMap);
        if(!createInstanceRsp.isSuccess()) {
            return new ResultRsp<OverlayVpn>(createInstanceRsp);
        }

        return new ResultRsp<OverlayVpn>(ErrorCode.OVERLAYVPN_SUCCESS);
    }

}
