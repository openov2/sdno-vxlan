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

package org.openo.sdno.vxlan.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.consts.UrlAdapterConst;
import org.openo.sdno.overlayvpn.enums.WanInterfaceUsedType;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.ctrlconnection.ControllerUtil;
import org.openo.sdno.rest.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility function to get WAN Sub Interface information.<br/>
 * 
 * @author
 * @version SDNO 0.5 03-Jun-2016
 */
public class WanSubInterfaceUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(WanSubInterfaceUtil.class);

    private WanSubInterfaceUtil() {

    }

    /**
     * Query NE WAN interface for VxLAN service.<br/>
     * 
     * @param deviceIdToNeMap - Map of Device ID to NE information
     * @param deviceIdToCtrlMap - Map of Device ID to Controller information
     * @return - Map of Device ID to WAN Sub Interface information
     * @throws ServiceException - when input is invalid or controller returns error
     * @since SDNO 0.5
     */
    public static ResultRsp<Map<String, WanSubInterface>> queryNeWanInfForVxlan(
            Map<String, NetworkElementMO> deviceIdToNeMap, Map<String, ControllerMO> deviceIdToCtrlMap)
            throws ServiceException {
        deviceIdToCtrlMap.clear();
        ResultRsp<Map<String, WanSubInterface>> rsp = new ResultRsp<Map<String, WanSubInterface>>();

        // Make UUID list
        List<String> neUuidList = new ArrayList<String>(deviceIdToNeMap.size());
        for(NetworkElementMO tempNeMo : deviceIdToNeMap.values()) {
            neUuidList.add(tempNeMo.getId());
        }

        // Check whether controllers are reachable or not
        ResultRsp<Map<String, ControllerMO>> testCtrlResult = ControllerUtil.testCtrlConnection(neUuidList);
        Map<String, ControllerMO> neUuidToCtrlMap = testCtrlResult.getData();

        // Prepare device ID to controller MO map
        for(Entry<String, NetworkElementMO> entry : deviceIdToNeMap.entrySet()) {
            deviceIdToCtrlMap.put(entry.getKey(), neUuidToCtrlMap.get(entry.getValue().getId()));
        }

        // Get NE WAN Interface information for each device
        Map<String, WanSubInterface> deviceIdToWanSubInfMap = new ConcurrentHashMap<String, WanSubInterface>();
        for(Entry<String, ControllerMO> entry : deviceIdToCtrlMap.entrySet()) {
            String ctrlUuid = entry.getValue().getObjectId();
            String deviceId = entry.getKey();

            ResultRsp<List<WanSubInterface>> queryResult =
                    getNeWanSubInterface(ctrlUuid, deviceId, WanInterfaceUsedType.VXLAN.getName());
            if(!queryResult.isSuccess() || CollectionUtils.isEmpty(queryResult.getData())) {
                LOGGER.error("failed to query wan sub interface for deviceid:" + deviceId);
                return new ResultRsp<Map<String, WanSubInterface>>(ErrorCode.OVERLAYVPN_FAILED, deviceIdToWanSubInfMap);
            }

            WanSubInterface tempWanSubInterface = queryResult.getData().get(0);

            // If we have IP address, return the result else need to enable DHCP
            if(StringUtils.isNotEmpty(tempWanSubInterface.getIpAddress())) {
                deviceIdToWanSubInfMap.put(deviceId, tempWanSubInterface);
                continue;
            }

            deviceIdToWanSubInfMap.put(deviceId, tempWanSubInterface);
        }

        rsp.setData(deviceIdToWanSubInfMap);
        return rsp;
    }

    private static ResultRsp<List<WanSubInterface>> getNeWanSubInterface(String cltuuid, String deviceId,
            String subInterUsedType) throws ServiceException {
        RestfulParametes restParam = VxlanRestParameterUtil.getQueryWanInterfaceParam(subInterUsedType);
        String queryUrl = UrlAdapterConst.ADAPTER_BASE_URL + cltuuid
                + MessageFormat.format(UrlAdapterConst.QUERY_WAN_INTERFACE, deviceId);

        RestfulResponse resp = RestfulProxy.get(queryUrl, restParam);

        if(null != resp) {
            LOGGER.info("Service trace : url: " + queryUrl + " body:" + resp.getResponseContent());
            dealSvcException(resp);
        } else {
            LOGGER.error("get neId = " + deviceId + "getNeWanSubInterface failed!adapter plugin return null");
            throw new ServiceException("getNeWanSubInterface failed");
        }

        try {
            String responseStr = ResponseUtils.transferResponse(resp);
            ResultRsp<List<WanSubInterface>> result =
                    JsonUtil.fromJson(responseStr, new TypeReference<ResultRsp<List<WanSubInterface>>>() {});
            LOGGER.info("get Wan Interfacefinish, result = " + result.toString());
            return result;
        } catch(ServiceException e) {
            LOGGER.error("get Wan Interface except info: ", e);
            return new ResultRsp<List<WanSubInterface>>(e.getId(), e.getExceptionArgs());
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
