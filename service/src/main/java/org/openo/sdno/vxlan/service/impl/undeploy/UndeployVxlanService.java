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

package org.openo.sdno.vxlan.service.impl.undeploy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCodeInfo;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Un-deploy VxLAN Service.<br>
 * 
 * @author
 * @version SDNO 0.5 03-Jun-2016
 */
public class UndeployVxlanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UndeployVxlanService.class);

    private UndeployVxlanService() {
    }

    /**
     * UnDeploy VxLan Instance - UnDeploy connection, or a single EPG<br>
     * 
     * @param connectionUuid - Connection Uuid
     * @param tenantId - Tenant Id
     * @return Operation result- Success or Failure
     * @throws ServiceException - when input is invalid or controller returns error or database
     *             update fails
     * @since SDNO 0.5
     */
    public static ResultRsp<String> undeploy(String connectionUuid, String tenantId) throws ServiceException {
        return undeployConnectionOrEpg(connectionUuid, tenantId, null);
    }

    private static ResultRsp<String> undeployConnectionOrEpg(String connectionUuid, String tenantId, String deviceId)
            throws ServiceException {
        ResultRsp<String> totalResult = new ResultRsp<String>(ErrorCode.OVERLAYVPN_SUCCESS);

        ResultRsp<String> resultRsp = new ResultRsp<String>(ErrorCode.OVERLAYVPN_SUCCESS);

        List<ErrorCodeInfo> smallErrorList = new ArrayList<ErrorCodeInfo>();

        resultRsp = UndeployVxlanInstance.undeployInstance(tenantId, connectionUuid, deviceId);
        if(!resultRsp.isSuccess()) {
            LOGGER.error("undeployConnection: undeploy vxlan tunnel failed. connectionUuid:" + connectionUuid);
            smallErrorList.addAll(resultRsp.getSmallErrorCodeList());
        }

        if(CollectionUtils.isNotEmpty(smallErrorList)) {
            totalResult.setSmallErrorCodeList(smallErrorList);
            totalResult.setErrorCode(ErrorCode.OVERLAYVPN_FAILED);
        }

        return totalResult;
    }
}
