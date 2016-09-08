/*
 * Copyright (c) 2016, Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openo.sdno.vxlan.util.check;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.util.check.ValidationUtil;
import org.openo.sdno.overlayvpn.util.objreflectoper.UuidAllocUtil;
import org.openo.sdno.vxlan.util.exception.ThrowVxlanExcpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Utility function to check Overlay VPN information.<br/>
 * 
 * @author
 * @version SDNO 0.5 03-Jun-2016
 */
public class CheckOverlayVpnUtil {

    private static CheckConnectionUtil checkConnectionUtil = new CheckConnectionUtil();

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckOverlayVpnUtil.class);

    private CheckOverlayVpnUtil() {
    }

    /**
     * Validate overlay VPN information <br/>
     * 
     * @param overlayVpn - Overlay VPN
     * @param tenantId - tenant ID
     * @param deviceIdToNeMap - Device ID to NE information mapping
     * @throws ServiceException - when overlay VPN information is invalid
     * @since SDNO 0.5
     */
    public static void check(OverlayVpn overlayVpn, String tenantId, Map<String, NetworkElementMO> deviceIdToNeMap)
            throws ServiceException {

        // Tenant ID from OverlayVpn
        if(!tenantId.equals(overlayVpn.getTenantId())) {
            ThrowVxlanExcpt.throwTenantIdInvalid(overlayVpn.getTenantId(), tenantId);
        }

        // Validate OverlayVpn model
        checkModelData(overlayVpn);

        // Validate UUID
        UuidAllocUtil.checkUuid(overlayVpn);

        // Check whether connection exists
        if(CollectionUtils.isEmpty(overlayVpn.getVpnConnections())) {
            ThrowVxlanExcpt.throwParmaterInvalid("VxLAN Connection", "Empty");
        }

        // Validate connection
        List<EndpointGroup> epgList = checkConnectionUtil.checkConnection(overlayVpn);

        // Validate End points
        for(EndpointGroup epg : epgList) {
            CheckEngpointGroupUtil.checkEndpoints(epg);
            CheckEngpointGroupUtil.checkResourceInEpg(epg, deviceIdToNeMap);
            if(null != epg.getGateway()) {
                CheckEngpointGroupUtil.checkResourceInGateway(epg.getGateway());
            }
        }

        // Validate Topology role
        CheckEngpointGroupUtil.checkTopoRole(overlayVpn);
    }

    private static void checkModelData(OverlayVpn overlayVpn) throws ServiceException {

        // check model data
        ValidationUtil.validateModel(overlayVpn);

        if(CollectionUtils.isEmpty(overlayVpn.getVpnConnections())) {
            ThrowVxlanExcpt.throwParmaterInvalid("Connection", "empty");
        }

        // check epg data
        for(Connection connection : overlayVpn.getVpnConnections()) {
            for(EndpointGroup epg : connection.getEndpointGroups()) {
                if(!StringUtils.hasLength(epg.getNeId())) {
                    ThrowVxlanExcpt.throwParmaterInvalid("NeID", "empty");
                }
            }
        }

        LOGGER.info("check cloudvpn model OK, name = " + overlayVpn.getName());
    }
}
