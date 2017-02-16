/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.consts.UrlAdapterConst;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCodeInfo;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.common.enums.AdminStatus;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.rest.ResponseUtils;
import org.openo.sdno.vxlan.util.VxlanRestParameterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * UnDeploy VxLAN instance.<br>
 * 
 * @author
 * @version SDNO 0.5 03-June-2016
 */
public class UndeployVxlanInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(UndeployVxlanInstance.class);

    private UndeployVxlanInstance() {

    }

    /**
     * Undeploy VxLAN service from controller<br>
     * 
     * @param tenantId - tenant ID
     * @param connectionUuid - Connection UUID
     * @param deviceId - Device ID
     * @return Operation result either Success or Failure
     * @throws ServiceException - when input is invalid or controller returns error
     * @since SDNO 0.5
     */
    public static ResultRsp<String> undeployInstance(String tenantId, String connectionUuid, String deviceId)
            throws ServiceException {

        // Query VxLan instances
        String vxlanInsfilter = getVxlanInsFilter(tenantId, connectionUuid, deviceId);

        ResultRsp<List<NeVxlanInstance>> queryResultRsp = (new InventoryDaoUtil<NeVxlanInstance>().getInventoryDao())
                .queryByFilter(NeVxlanInstance.class, vxlanInsfilter, null);
        if(!queryResultRsp.isSuccess()) {
            LOGGER.error("Vxlan undeployInstance queryByFilter return empty.");
            throw new ServiceException("Undeploy Instance occurs Error!!");
        }

        List<NeVxlanInstance> undeployVxlanInstances = queryResultRsp.getData();

        if(CollectionUtils.isEmpty(undeployVxlanInstances)) {
            LOGGER.warn("Vxlan. undeployInstance queryByFilter return empty.");
            return new ResultRsp<>(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        // Make the map - Controller UUID to NeVxlan Instance list
        Map<String, List<NeVxlanInstance>> ctrlUuidToInstanceListMap =
                buildCtrlUuidToInstanceMap(undeployVxlanInstances);

        // Undeploy from controller - send to adapter
        ResultRsp<String> deleteRsp = new ResultRsp<>(ErrorCode.OVERLAYVPN_SUCCESS);
        deleteRsp.setSmallErrorCodeList(new ArrayList<ErrorCodeInfo>());

        for(Entry<String, List<NeVxlanInstance>> tempEntry : ctrlUuidToInstanceListMap.entrySet()) {
            ResultRsp<List<NeVxlanInstance>> tempRsp =
                    deleteVxlanInstanceFromAdapter(tempEntry.getKey(), tempEntry.getValue(), false);

            if(CollectionUtils.isNotEmpty(tempRsp.getSmallErrorCodeList())) {
                deleteRsp.getSmallErrorCodeList().addAll(tempRsp.getSmallErrorCodeList());
            }
        }

        refreshDataInDb(undeployVxlanInstances);

        if(CollectionUtils.isEmpty(deleteRsp.getSmallErrorCodeList())) {
            return deleteRsp;
        }

        deleteRsp.setErrorCode(ErrorCode.OVERLAYVPN_FAILED);
        return deleteRsp;
    }

    private static Map<String, List<NeVxlanInstance>>
            buildCtrlUuidToInstanceMap(List<NeVxlanInstance> undeployVxlanInstances) {
        Map<String, List<NeVxlanInstance>> ctrlUuidToInstanceListMap = new HashMap<>();
        for(NeVxlanInstance tempNeVxlanInstance : undeployVxlanInstances) {
            String ctrlId = tempNeVxlanInstance.getControllerId();
            if(CollectionUtils.isEmpty(ctrlUuidToInstanceListMap.get(ctrlId))) {
                ctrlUuidToInstanceListMap.put(ctrlId, new ArrayList<NeVxlanInstance>());
            }

            ctrlUuidToInstanceListMap.get(ctrlId).add(tempNeVxlanInstance);
        }
        return ctrlUuidToInstanceListMap;
    }

    /**
     * Delete VxLAN Instance from controller<br>
     * 
     * @param ctrlUuid - Controller UUID
     * @param vxlanInstanceList - List of VxLAN Instance to be undeployed
     * @param isCreateInstance - whether need to delete from database or controller
     * @return List of NEVxLAN instances and operation result of delete
     * @throws ServiceException - when input is invalid or controller rest interface fails
     * @since SDNO 0.5
     */
    public static ResultRsp<List<NeVxlanInstance>> deleteVxlanInstanceFromAdapter(String ctrlUuid,
            List<NeVxlanInstance> vxlanInstanceList, boolean isCreateInstance) throws ServiceException {
        ResultRsp<List<NeVxlanInstance>> deleteRsp = new ResultRsp<>();
        deleteRsp.setData(vxlanInstanceList);

        List<ErrorCodeInfo> smallErrorCodeList = new ArrayList<>();
        List<NeVxlanInstance> deleteVxlanInstanceFailedList = new ArrayList<>();
        List<NeVxlanInstance> deleteVxlanInstanceOkList = new ArrayList<>();

        for(NeVxlanInstance vxlanInstance : vxlanInstanceList) {

            String deleteInstanceUrl =
                    MessageFormat.format(UrlAdapterConst.REMOVE_VXLAN_INSTANCE, vxlanInstance.getUuid());

            RestfulResponse response = RestfulProxy.delete(UrlAdapterConst.VXLAN_ADAPTER_BASE_URL + deleteInstanceUrl,
                    VxlanRestParameterUtil.getDeleteInstanceParam(vxlanInstanceList, ctrlUuid));

            try {
                String overlayVpnContent = ResponseUtils.transferResponse(response);
                ResultRsp<String> restResult =
                        JsonUtil.fromJson(overlayVpnContent, new TypeReference<ResultRsp<String>>() {});
                LOGGER.info("Vxlan. delete Vxlan instance finish, result = " + restResult);

                if(!restResult.isSuccess()) {
                    deleteVxlanInstanceFailedList.add(vxlanInstance);
                    ErrorCodeInfo tempErrorCodeInfo = new ErrorCodeInfo(restResult.getErrorCode());
                    tempErrorCodeInfo.setObjectId(vxlanInstance.getUuid());
                    smallErrorCodeList.add(tempErrorCodeInfo);
                    continue;
                }

                deleteVxlanInstanceOkList.add(vxlanInstance);
            } catch(ServiceException e) {
                LOGGER.error("Vxlan. delete Vxlan instance except info: ", e);
                deleteVxlanInstanceFailedList.add(vxlanInstance);
                ErrorCodeInfo tempErrorCodeInfo = new ErrorCodeInfo(ErrorCode.OVERLAYVPN_FAILED);
                tempErrorCodeInfo.setObjectId(vxlanInstance.getUuid());
                smallErrorCodeList.add(tempErrorCodeInfo);
            }
        }

        if(CollectionUtils.isNotEmpty(smallErrorCodeList)) {
            deleteRsp.setErrorCode(ErrorCode.OVERLAYVPN_FAILED);
            deleteRsp.setHttpCode(HttpCode.ERR_FAILED);
            deleteRsp.setSmallErrorCodeList(smallErrorCodeList);
        }

        if(isCreateInstance) {
            deleteRsp.setData(deleteVxlanInstanceFailedList);
        } else {
            refreshDataInDb(deleteVxlanInstanceOkList);
        }

        return deleteRsp;
    }

    private static void refreshDataInDb(List<NeVxlanInstance> dataList) throws ServiceException {
        for(NeVxlanInstance tempNeVxlanInstance : dataList) {
            tempNeVxlanInstance.setAdminStatus(AdminStatus.INACTIVE.getName());
            tempNeVxlanInstance.setExternalId("");
        }

        (new InventoryDaoUtil<NeVxlanInstance>().getInventoryDao()).update(NeVxlanInstance.class, dataList,
                "adminStatus,externalId");
    }

    private static String getVxlanInsFilter(String tenantId, String connectionUuid, String deviceId) {

        Map<String, Object> filterMap = new HashMap<>();

        filterMap.put("connectionServiceId", Arrays.asList(connectionUuid));
        if(StringUtils.hasLength(tenantId)) {
            filterMap.put("tenantId", Arrays.asList(tenantId));
        }
        if(StringUtils.hasLength(deviceId)) {
            filterMap.put("neId", Arrays.asList(deviceId));
        }
        return JsonUtil.toJson(filterMap);
    }
}
