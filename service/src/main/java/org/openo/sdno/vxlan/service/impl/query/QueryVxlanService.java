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

package org.openo.sdno.vxlan.service.impl.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;
import org.openo.sdno.overlayvpn.model.tunnel.Tunnel;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Query VxLAN service.<br>
 * 
 * @author
 * @version SDNO 0.5 03-Jun-2016
 */
public class QueryVxlanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryVxlanService.class);

    private static final String CONNECTION_ID_KEY = "connectionServiceId";

    private static final String TENANT_ID_KEY = "tenantId";

    private QueryVxlanService() {
    }

    /**
     * Query VxLAN tunnel information<br>
     * 
     * @param tenantId - tenant ID
     * @param connectionUuid - Connection UUID
     * @return List of tunnel information consisting of source and destination information
     * @throws ServiceException - when input is invalid or query database fails
     * @since SDNO 0.5
     */
    @SuppressWarnings("unchecked")
    public static ResultRsp<List<Tunnel>> query(String tenantId, String connectionUuid) throws ServiceException {
        ResultRsp<List<Tunnel>> resultRsp = new ResultRsp<List<Tunnel>>();

        String filter = getFilter(tenantId, connectionUuid);

        InventoryDao<NeVxlanTunnel> neVxlanTunnelDao = new InventoryDaoUtil<NeVxlanTunnel>().getInventoryDao();
        ResultRsp<List<NeVxlanTunnel>> tunnelResponse =
                neVxlanTunnelDao.queryByFilter(NeVxlanTunnel.class, filter, null);

        if(!tunnelResponse.isSuccess()) {
            LOGGER.error("Query vxlan tunnel failed, connectionUuid:{0}", connectionUuid);
            resultRsp.setErrorCode(ErrorCode.OVERLAYVPN_FAILED);
            return resultRsp;
        }

        return convertNeVxlanTunnel(tunnelResponse);
    }

    private static ResultRsp<List<Tunnel>> convertNeVxlanTunnel(ResultRsp<List<NeVxlanTunnel>> neVxlanTunnelRsp) {

        List<Tunnel> tunnelList = new ArrayList<Tunnel>();

        for(NeVxlanTunnel neTunnel : neVxlanTunnelRsp.getData()) {
            boolean isExist = false;
            for(Tunnel existTunnel : tunnelList) {
                if((existTunnel.getSrcNeId().equals(neTunnel.getNeId())
                        && existTunnel.getDstNeId().equals(neTunnel.getPeerNeId()))
                        || (existTunnel.getDstNeId().equals(neTunnel.getNeId())
                                && existTunnel.getSrcNeId().equals(neTunnel.getPeerNeId()))) {
                    isExist = true;
                    break;
                }
            }
            if(isExist) {
                continue;
            }

            Tunnel tunnel = new Tunnel();
            tunnel.setDstIp(neTunnel.getDestAddress());
            tunnel.setSrcIp(neTunnel.getSourceAddress());

            tunnel.setDstNeId(neTunnel.getPeerNeId());
            tunnel.setSrcNeId(neTunnel.getNeId());
            tunnel.setTunnelId(neTunnel.getUuid());

            tunnel.setVni(neTunnel.getVni());

            tunnelList.add(tunnel);
        }

        return new ResultRsp<>(ErrorCode.OVERLAYVPN_SUCCESS, tunnelList);
    }

    private static String getFilter(String tenantId, String connectionUuid) {
        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put(CONNECTION_ID_KEY, Arrays.asList(connectionUuid));

        if(StringUtils.isNotEmpty(tenantId)) {
            filterMap.put(TENANT_ID_KEY, Arrays.asList(tenantId));
        }

        return JsonUtil.toJson(filterMap);
    }
}
