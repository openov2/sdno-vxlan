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

package org.openo.sdno.vxlan.util.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.model.v2.vxlan.NbiVxlanTunnel;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanInstance;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.vxlanservice.adapter.CallSbiApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * deploy and undeploy vxlan service on adapter.<br>
 * 
 * @author
 * @version SDNO 0.5 Jan 15, 2017
 */
public class DeployService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployService.class);

    private DeployService() {
    }

    /**
     * deploy the vxlan of the given uuids.<br>
     * 
     * @param deploy list of uuid of the vxlan model to deploy.
     * @return list of uuid of the vxlan model deployed.
     * @throws ServiceException if inner error happens.
     * @since SDNO 0.5
     */
    public static List<String> deploy(List<String> deploy) throws ServiceException {
        List<NbiVxlanTunnel> nbiModels = VxlanTunnelDbHelper.getNbiVxlanById(deploy);
        List<SbiNeVxlanInstance> sbiModels = VxlanTunnelDbHelper.getSbiVxlansByNbiModelId(deploy);
        List<NetworkElementMO> neMos = new NetworkElementInvDao().getAllMO();
        for(SbiNeVxlanInstance sbiVxlan : sbiModels) {
            CreateVxlanHelper.replaceNeIdWithDeviceId(sbiVxlan, neMos);
        }
        LOGGER.info("=====deploy vxlan======");
        ResultRsp<List<SbiNeVxlanInstance>> result = CallSbiApi.create(sbiModels);
        VxlanTunnelDbHelper.updateDeployStatus(nbiModels, sbiModels, true);
        return deploy;

    }

    /**
     * undeploy the vxlan of the given uuids.<br>
     * 
     * @param undeploy list of uuid of the vxlan model to deploy.
     * @return list of uuid of the vxlan model deployed.
     * @throws ServiceException if inner error happens.
     * @since SDNO 0.5
     */
    public static List<String> undeploy(List<String> undeploy) throws ServiceException {
        List<NbiVxlanTunnel> nbiModels = VxlanTunnelDbHelper.getNbiVxlanById(undeploy);
        List<SbiNeVxlanInstance> sbiModels = VxlanTunnelDbHelper.getSbiVxlansByNbiModelId(undeploy);
        Map<String, List<SbiNeVxlanInstance>> deviceIdToSbiModels = new HashMap<>();
        for(SbiNeVxlanInstance sbiModel : sbiModels) {
            if(CollectionUtils.isEmpty(deviceIdToSbiModels.get(sbiModel.getDeviceId()))) {
                deviceIdToSbiModels.put(sbiModel.getDeviceId(), new ArrayList<SbiNeVxlanInstance>());
            }
            deviceIdToSbiModels.get(sbiModel.getDeviceId()).add(sbiModel);
        }

        List<String> retult = new ArrayList<>();
        LOGGER.info("=====undeploy vxlan======");
        for(String deviceId : deviceIdToSbiModels.keySet()) {
            ResultRsp<List<SbiNeVxlanInstance>> response =
                    CallSbiApi.delete(deviceIdToSbiModels.get(deviceId), deviceId);
        }

        VxlanTunnelDbHelper.updateDeployStatus(nbiModels, sbiModels, false);
        return undeploy;
    }

}
