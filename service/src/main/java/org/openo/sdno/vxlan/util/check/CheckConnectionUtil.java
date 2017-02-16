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
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.vxlan.util.exception.ThrowVxlanExcpt;

/**
 * Utilities for connection check.<br>
 * 
 * @author
 * @version SDNO 0.5 2016-6-8
 */
public class CheckConnectionUtil {

    /**
     * Validate connection information inside the OverlayVPN.<br>
     * 
     * @param overlayVpn - OverlayVpn Object
     * @return List of EndPoint group inside the connection
     * @throws ServiceException - when OverlayVpn Information is invalid
     * @since SDNO 0.5
     */
    public List<EndpointGroup> checkConnection(OverlayVpn overlayVpn) throws ServiceException {
        List<EndpointGroup> epgList = new ArrayList<>();
        for(Connection curConnection : overlayVpn.getVpnConnections()) {
            List<EndpointGroup> curEndpointGroups = curConnection.getEndpointGroups();
            if(CollectionUtils.isEmpty(curEndpointGroups)) {
                ThrowVxlanExcpt.throwParmaterInvalid("endpointGroups", "Not Null");
            }
            epgList.addAll(curEndpointGroups);
        }
        return epgList;
    }
}
