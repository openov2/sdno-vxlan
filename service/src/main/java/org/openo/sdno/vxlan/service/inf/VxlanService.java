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

package org.openo.sdno.vxlan.service.inf;

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

/**
 * Interface for VxLAN service.<br>
 * 
 * @author
 * @version SDNO 0.5 2016-6-7
 */
public interface VxlanService {

    /**
     * Create VxLAN service.<br>
     * 
     * @param overlayVpn OverlayVPN Object
     * @param tenantId tenant Id
     * @param deviceIdToNeVtepMap Map of Device Id to NeVtep
     * @param deviceIdToWansubInfMap Map of Device Id to WanSubInterface
     * @param connectionIdToVniMap Map of Connection Id to VNI
     * @param deviceIdToCtrlMap Map of Device Id to Controller
     * @return OverlayVpn updated
     * @throws ServiceException - when input is invalid or controller returns error
     * @since SDNO 0.5
     */
    ResultRsp<OverlayVpn> create(OverlayVpn overlayVpn, String tenantId, Map<String, NeVtep> deviceIdToNeVtepMap,
            Map<String, WanSubInterface> deviceIdToWansubInfMap, Map<String, String> connectionIdToVniMap,
            Map<String, ControllerMO> deviceIdToCtrlMap) throws ServiceException;

    /**
     * Query VxLan tunnel.<br>
     * 
     * @param request HTTP context
     * @param connectionUuid Connection UUID
     * @param tenantId Tenant ID
     * @return Operation result and T Information
     * @throws ServiceException - when input is invalid or controller returns error
     * @since SDNO 0.5
     */
    ResultRsp<List<Tunnel>> queryTunnel(String connectionUuid, String tenantId) throws ServiceException;

    /**
     * Delete VxLAN service<br>
     * 
     * @param connectionUuid Connection Id
     * @param tenantId Tenant Id
     * @return Operation result
     * @throws ServiceException - when input is invalid or controller returns error
     * @since SDNO 0.5
     */
    ResultRsp<Connection> delete(String connectionUuid, String tenantId) throws ServiceException;

    /**
     * UnDeploy VxLAN service from controller<br>
     * 
     * @param connectionUuid Connection Id
     * @param tenantId Tenant Id
     * @return Operation result
     * @throws ServiceException - when input is invalid or controller returns failure
     * @since SDNO 0.5
     */
    ResultRsp<String> undeploy(String connectionUuid, String tenantId) throws ServiceException;
}
