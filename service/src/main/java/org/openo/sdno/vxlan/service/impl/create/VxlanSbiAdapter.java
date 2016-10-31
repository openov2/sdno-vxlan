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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.consts.UrlAdapterConst;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCodeInfo;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.rest.ResponseUtils;
import org.openo.sdno.vxlan.service.impl.undeploy.UndeployVxlanInstance;
import org.openo.sdno.vxlan.util.VxlanRestParameterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * VxLAN south bound interfaces.<br>
 * 
 * @author
 * @version SDNO 0.5 03-Jun-2016
 */
public class VxlanSbiAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxlanSbiAdapter.class);

    private VxlanSbiAdapter() {
    }

    /**
     * Deploy the VxLAN instance on the controller<br>
     * 
     * @param ctrlIdToVxlanListMap - Map of controller ID to list of VxLAN
     * @return Map of Controller UUID to list of VxLAN instances
     * @throws ServiceException - when input is invalid or controller returns error
     * @since SDNO 0.5
     */
    public static ResultRsp<Map<String, List<NeVxlanInstance>>>
            deployVxlanInstanceByCtrl(Map<String, List<NeVxlanInstance>> ctrlIdToVxlanListMap) throws ServiceException {
        ResultRsp<Map<String, List<NeVxlanInstance>>> totalResult =
                new ResultRsp<Map<String, List<NeVxlanInstance>>>(ErrorCode.OVERLAYVPN_SUCCESS);

        Map<String, List<NeVxlanInstance>> ctrlIdToVxlanInsSuccMap =
                new ConcurrentHashMap<String, List<NeVxlanInstance>>();
        for(Entry<String, List<NeVxlanInstance>> tempEntry : ctrlIdToVxlanListMap.entrySet()) {
            String ctrlUuid = tempEntry.getKey();
            List<NeVxlanInstance> vlanIns = tempEntry.getValue();

            try {
                // Deploy the VxLAN instance on the controller
                ResultRsp<List<NeVxlanInstance>> tempCreateVxlanResult = deployVxlanInstance(ctrlUuid, vlanIns);
                List<ErrorCodeInfo> errorCodeInfoLst = new ArrayList<ErrorCodeInfo>();
                if(!tempCreateVxlanResult.isSuccess()) {
                    LOGGER.error("deploy vxlan tunnel fail, ctrl uuid: " + ctrlUuid);
                    errorCodeInfoLst.addAll(tempCreateVxlanResult.getSmallErrorCodeList());
                }

                if(CollectionUtils.isNotEmpty(errorCodeInfoLst)) {
                    totalResult.copyInfo(tempCreateVxlanResult);
                    totalResult.setSmallErrorCodeList(errorCodeInfoLst);
                }

                List<NeVxlanInstance> createRspInstanceTunnelList = tempCreateVxlanResult.getData();

                // Populate the successful vxlan instances
                List<NeVxlanInstance> tempCreateOkTunnelList = new ArrayList<NeVxlanInstance>();
                for(NeVxlanInstance tempVxlanNeVpnNeVpn : createRspInstanceTunnelList) {
                    if((StringUtils.hasLength(tempVxlanNeVpnNeVpn.getExternalId()))
                            && (StringUtils.hasLength(tempVxlanNeVpnNeVpn.getName()))) {
                        tempCreateOkTunnelList.add(tempVxlanNeVpnNeVpn);
                    }
                }
                // Need to update database for all teh successful vxlan instances
                ctrlIdToVxlanInsSuccMap.put(ctrlUuid, tempCreateOkTunnelList);

            } catch(ServiceException e) {
                if(e.getHttpCode() == HttpCode.BAD_REQUEST) {
                    undeployVxlanInstanceByCtrl(ctrlIdToVxlanInsSuccMap);
                }

                ResultRsp<Map<String, List<NeVxlanInstance>>> rsp =
                        new ResultRsp<Map<String, List<NeVxlanInstance>>>(e);
                rsp.setData(ctrlIdToVxlanInsSuccMap);
                return rsp;
            }
        }

        totalResult.setData(ctrlIdToVxlanInsSuccMap);

        return totalResult;
    }

    private static ResultRsp<List<NeVxlanInstance>> deployVxlanInstance(String ctrlUuid,
            List<NeVxlanInstance> vxlanInstancesList) throws ServiceException {
        RestfulParametes restfulParametes =
                VxlanRestParameterUtil.getCreateVxlanInstanceParam(vxlanInstancesList, ctrlUuid);
        RestfulResponse response = RestfulProxy.post(
                UrlAdapterConst.VXLAN_ADAPTER_BASE_URL + UrlAdapterConst.BATCH_CREATE_VXLAN_INSTANCE, restfulParametes);
        if(response.getStatus() == HttpCode.NOT_FOUND) {
            return new ResultRsp<List<NeVxlanInstance>>(ErrorCode.RESTFUL_COMMUNICATION_FAILED);
        }

        if(!HttpCode.isSucess(response.getStatus())) {
            LOGGER.error("Adapter Return Error when create Vxlan.");
            throw new ServiceException(ErrorCode.OVERLAYVPN_FAILED);
        }

        try {
            String rspContent = ResponseUtils.transferResponse(response);
            ResultRsp<List<NeVxlanInstance>> restResult =
                    JsonUtil.fromJson(rspContent, new TypeReference<ResultRsp<List<NeVxlanInstance>>>() {});
            LOGGER.info("Vxlan. creat Vxlan service finish, result = " + restResult.toString());
            return new ResultRsp<List<NeVxlanInstance>>(restResult, restResult.getData());
        } catch(ServiceException e) {
            LOGGER.error("Vxlan. except info: ", e);
            throw e;
        }
    }

    private static void undeployVxlanInstanceByCtrl(Map<String, List<NeVxlanInstance>> ctrlUuidToVxlanMap)
            throws ServiceException {
        Map<String, List<NeVxlanInstance>> deleteFailedMap = new ConcurrentHashMap<String, List<NeVxlanInstance>>();
        List<String> deleteSuccessCtrlIds = new ArrayList<>();
        for(Entry<String, List<NeVxlanInstance>> tempEntry : ctrlUuidToVxlanMap.entrySet()) {
            String ctrlUuid = tempEntry.getKey();
            ResultRsp<List<NeVxlanInstance>> tempDeleteVxlanInstanceFailedList =
                    UndeployVxlanInstance.deleteVxlanInstanceFromAdapter(ctrlUuid, tempEntry.getValue(), true);

            if(CollectionUtils.isNotEmpty(tempDeleteVxlanInstanceFailedList.getData())) {
                deleteFailedMap.put(ctrlUuid, tempDeleteVxlanInstanceFailedList.getData());
            } else {
                deleteSuccessCtrlIds.add(ctrlUuid);
            }
        }

        if(MapUtils.isNotEmpty(deleteFailedMap)) {
            ctrlUuidToVxlanMap.putAll(deleteFailedMap);
        }

        if(CollectionUtils.isNotEmpty(deleteSuccessCtrlIds)) {
            for(String deleteSuccessCtrlUuid : deleteSuccessCtrlIds) {
                ctrlUuidToVxlanMap.remove(deleteSuccessCtrlUuid);
            }
        }
    }
}
