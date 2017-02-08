/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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

package org.openo.sdno.vxlanservice.adapter;

import java.text.MessageFormat;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.exception.InnerErrorServiceException;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanInstance;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.rest.ResponseUtils;
import org.openo.sdno.vxlan.constant.AdapterUrl;
import org.openo.sdno.vxlan.util.builder.VxlanHttpParameterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>
 * 
 * @author
 * @version SDNO 0.5 Jan 16, 2017
 */
public class CallSbiApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallSbiApi.class);

    /**
     * <br>
     * 
     * @param sbiModels
     * @return
     * @throws ServiceException
     * @since SDNO 0.5
     */
    public static ResultRsp<List<SbiNeVxlanInstance>> create(List<SbiNeVxlanInstance> sbiModels)
            throws ServiceException {
        String ctrlUuid = sbiModels.get(0).getControllerId();
        RestfulParametes restfulParametes = VxlanHttpParameterUtil.getCreateVxlanInstanceParam(sbiModels, ctrlUuid);
        LOGGER.info("=====post adapter=====");
        LOGGER.info("==========" + JsonUtil.toJson(sbiModels));
        LOGGER.info("=====ctrlUuid is =====" + ctrlUuid);
        RestfulResponse response = RestfulProxy
                .post(AdapterUrl.VXLAN_ADAPTER_BASE_URL + AdapterUrl.BATCH_CREATE_VXLAN_INSTANCE, restfulParametes);

        if(!HttpCode.isSucess(response.getStatus())) {
            LOGGER.error("Adapter Return Error when create Vxlan.");
            throw new ServiceException(ErrorCode.OVERLAYVPN_FAILED);
        }

        try {
            LOGGER.info("==========return from adapter" + JsonUtil.toJson(response));
            String rspContent = ResponseUtils.transferResponse(response);
            LOGGER.info("==========return from adapter" + JsonUtil.toJson(rspContent));
            ResultRsp<List<SbiNeVxlanInstance>> restResult =
                    JsonUtil.fromJson(rspContent, new TypeReference<ResultRsp<List<SbiNeVxlanInstance>>>() {});
            LOGGER.info("Vxlan. creat Vxlan service finish, result = " + restResult.toString());
            return new ResultRsp<List<SbiNeVxlanInstance>>(restResult, restResult.getData());
        } catch(ServiceException e) {
            LOGGER.error("Vxlan. except info: ", e);
            throw e;
        }

    }

    /**
     * <br>
     * 
     * @param sbiModels
     * @throws ServiceException
     * @since SDNO 0.5
     */
    public static ResultRsp<List<SbiNeVxlanInstance>> delete(List<SbiNeVxlanInstance> sbiModels, String deviceId)
            throws ServiceException {
        LOGGER.info("=====start to delete vxlan on adapter, deviceId= " + deviceId);
        String ctrlUuid = sbiModels.get(0).getControllerId();
        RestfulParametes restfulParametes = VxlanHttpParameterUtil.getDeleteInstanceParam(sbiModels, ctrlUuid);
        String deleteInstanceUrl = MessageFormat.format(AdapterUrl.BATCH_DELETE_VXLAN_INSTANCE, deviceId);
        RestfulResponse response =
                RestfulProxy.post(AdapterUrl.VXLAN_ADAPTER_BASE_URL + deleteInstanceUrl, restfulParametes);

        if(!HttpCode.isSucess(response.getStatus())) {
            LOGGER.error("Adapter Return Error when delete Vxlan.");
            throw new InnerErrorServiceException("Adapter Return Error when delete Vxlan.");
        }

        try {
            String rspContent = ResponseUtils.transferResponse(response);
            LOGGER.info("==========return from adapter" + JsonUtil.toJson(response));
            LOGGER.info("==========return from adapter" + JsonUtil.toJson(rspContent));
            ResultRsp<List<SbiNeVxlanInstance>> restResult =
                    JsonUtil.fromJson(rspContent, new TypeReference<ResultRsp<List<SbiNeVxlanInstance>>>() {});
            LOGGER.info("Vxlan. delete Vxlan service finish, result = " + restResult.toString());
            return new ResultRsp<List<SbiNeVxlanInstance>>(restResult, restResult.getData());
        } catch(ServiceException e) {
            LOGGER.error("Vxlan. except info: ", e);
            throw e;
        }

    }

}
