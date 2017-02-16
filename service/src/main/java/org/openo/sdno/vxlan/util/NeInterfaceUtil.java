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

package org.openo.sdno.vxlan.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.consts.UrlAdapterConst;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVtep;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.ctrlconnection.ControllerUtil;
import org.openo.sdno.rest.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility function to get NE Interface Information.<br>
 * 
 * @author
 * @version SDNO 0.5 03-June-2016
 */
public class NeInterfaceUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeInterfaceUtil.class);

    private NeInterfaceUtil() {
    }

    /**
     * Query VTEP interface information from controller<br>
     * 
     * @param deviceIdToNeMap - Map of device ID and NE information
     * @param deviceIdToCtrlMap - Device ID to Controller information
     * @return Map of Device ID and VTEP information
     * @throws ServiceException - when input is invalid or controller returns error
     * @since SDNO 0.5
     */
    public static ResultRsp<Map<String, NeVtep>> queryVtepForVxlan(Map<String, NetworkElementMO> deviceIdToNeMap,
            Map<String, ControllerMO> deviceIdToCtrlMap) throws ServiceException {
        ResultRsp<Map<String, NeVtep>> rsp = new ResultRsp<Map<String, NeVtep>>();

        // Make NE UUID list
        List<String> neUuidList = new ArrayList<>(deviceIdToNeMap.size());
        for(NetworkElementMO tempNeMo : deviceIdToNeMap.values()) {
            neUuidList.add(tempNeMo.getId());
        }

        // Test whether controller is reachable or not
        ResultRsp<Map<String, ControllerMO>> testCtrlResult = ControllerUtil.testCtrlConnection(neUuidList);
        Map<String, ControllerMO> neUuidToCtrlMap = testCtrlResult.getData();

        // Make NE UUID to controller Map
        for(Entry<String, NetworkElementMO> entry : deviceIdToNeMap.entrySet()) {
            deviceIdToCtrlMap.put(entry.getKey(), neUuidToCtrlMap.get(entry.getValue().getId()));
        }

        // Query VTEP information
        Map<String, NeVtep> deviceIdToWanSubInfMap = new ConcurrentHashMap<String, NeVtep>();
        for(Entry<String, ControllerMO> entry : deviceIdToCtrlMap.entrySet()) {
            String ctrlUuid = entry.getValue().getObjectId();
            String deviceId = entry.getKey();

            // Get VTEP
            ResultRsp<NeVtep> queryResult = getNeVtep(ctrlUuid, deviceId);
            if(!queryResult.isValid()) {
                LOGGER.error("failed to query vtep ip for deviceid:" + deviceId);
                return new ResultRsp<Map<String, NeVtep>>(ErrorCode.OVERLAYVPN_FAILED, deviceIdToWanSubInfMap);
            }

            deviceIdToWanSubInfMap.put(deviceId, queryResult.getData());
        }

        rsp.setData(deviceIdToWanSubInfMap);
        return rsp;
    }

    private static ResultRsp<NeVtep> getNeVtep(String contrUuid, String devId) throws ServiceException {
        RestfulParametes restParametes = VxlanRestParameterUtil.getQueryVtepParam(contrUuid);
        String getUrl =
                UrlAdapterConst.VXLAN_ADAPTER_BASE_URL + MessageFormat.format(UrlAdapterConst.QUERY_VTEP, devId);

        RestfulResponse restResp = RestfulProxy.get(getUrl, restParametes);
        if(null != restResp) {
            LOGGER.info("Service trace : url:" + getUrl + " contrUuid: " + contrUuid + " body:"
                    + restResp.getResponseContent());
            dealSvcException(restResp);
        } else {
            LOGGER.error("get deviceId = " + devId + "getNeVtep failed!adapter plugin return null");
            throw new ServiceException("getNeVtep failed");
        }

        try {
            String responseStr = ResponseUtils.transferResponse(restResp);
            ResultRsp<NeVtep> result = JsonUtil.fromJson(responseStr, new TypeReference<ResultRsp<NeVtep>>() {});
            LOGGER.info("query vtep finish, result = " + result.toString());
            return result;
        } catch(ServiceException e) {
            LOGGER.error("query vtep except info: ", e);
            return new ResultRsp<NeVtep>(e.getId(), e.getExceptionArgs());
        }
    }

    private static void dealSvcException(RestfulResponse response) throws ServiceException {
        if(null == response.getResponseContent()) {
            return;
        }

        if(response.getResponseContent().contains("exceptionId")) {
            LOGGER.error("Plugin deal failed! Plugin return status:" + response.getStatus() + ", exception:"
                    + response.getResponseContent());
            ResponseUtils.checkResonseAndThrowException(response);
        }
    }
}
