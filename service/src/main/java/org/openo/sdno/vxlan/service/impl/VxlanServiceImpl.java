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

package org.openo.sdno.vxlan.service.impl;

import java.util.List;
import java.util.Map;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVtep;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.model.tunnel.Tunnel;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.vxlan.service.impl.create.CreateVxlanService;
import org.openo.sdno.vxlan.service.impl.delete.DeleteVxlanService;
import org.openo.sdno.vxlan.service.impl.query.QueryVxlanService;
import org.openo.sdno.vxlan.service.impl.undeploy.UndeployVxlanService;
import org.openo.sdno.vxlan.service.inf.VxlanService;
import org.openo.sdno.vxlan.util.vxlanbuilder.ModelConvertUtil;
import org.openo.sdno.vxlan.util.vxlanbuilder.VxlanProducer;

/**
 * Implementation class of VxlanService.<br>
 * 
 * @author
 * @version SDNO 0.5 03-July-2016
 */
public class VxlanServiceImpl implements VxlanService {

    @Override
    public ResultRsp<OverlayVpn> create(OverlayVpn overlayVpn, String tenantId, Map<String, NeVtep> deviceIdToNeVtepMap,
            Map<String, WanSubInterface> deviceIdToWansubInfMap, Map<String, String> connectionIdToVniMap,
            Map<String, ControllerMO> deviceIdToCtrlMap) throws ServiceException {
        // Convert OverlayVPN to NeVxLanInstance
        VxlanProducer vxlanProducer = ModelConvertUtil.convertFromOverlayVpnService(overlayVpn, deviceIdToNeVtepMap,
                deviceIdToWansubInfMap, connectionIdToVniMap);
        // Create VxLAN on the controller
        return CreateVxlanService.createVxlanComponents(tenantId, deviceIdToCtrlMap, vxlanProducer);
    }

    @Override
    public ResultRsp<List<Tunnel>> queryTunnel(String connectionUuid, String tenantId) throws ServiceException {
        return QueryVxlanService.query(tenantId, connectionUuid);
    }

    @Override
    public ResultRsp<Connection> delete(String connectionUuid, String tenantId) throws ServiceException {
        return DeleteVxlanService.delete(connectionUuid, tenantId);
    }

    @Override
    public ResultRsp<String> undeploy(String connectionUuid, String tenantId) throws ServiceException {
        return UndeployVxlanService.undeploy(connectionUuid, tenantId);
    }
}
