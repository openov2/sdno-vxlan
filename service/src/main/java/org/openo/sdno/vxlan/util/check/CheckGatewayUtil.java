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

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.servicemodel.Gateway;
import org.openo.sdno.vxlan.util.exception.ThrowVxlanExcpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Utility function to check Gateway information.<br>
 * 
 * @author
 * @version SDNO 0.5 Aug 22, 2016
 */
public class CheckGatewayUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckEngpointGroupUtil.class);

    private CheckGatewayUtil() {
    }

    /**
     * Check Gateway Data.<br>
     * 
     * @param gateway Gateway data
     * @throws ServiceException throws when NetworkElement data is invalid
     * @since SDNO 0.5
     */
    public static void checkResourceInGateway(Gateway gateway) throws ServiceException {
        NetworkElementInvDao neDao = new NetworkElementInvDao();

        NetworkElementMO tempNetworkElement = neDao.query(gateway.getNeId());
        if(null == tempNetworkElement) {
            LOGGER.error("ne not exist: " + gateway.getNeId());
            ThrowVxlanExcpt.throwParmaterInvalid(ErrorCode.RESOURCE_NETWORKELEMENT_NOT_EXIST, gateway.getNeId());
            return;
        }

        String deviceId = tempNetworkElement.getNativeID();
        if(!StringUtils.hasLength(deviceId)) {
            ThrowVxlanExcpt.throwParmaterInvalid("device id", tempNetworkElement.getName());
        }

        gateway.setDeviceId(deviceId);
    }
}
