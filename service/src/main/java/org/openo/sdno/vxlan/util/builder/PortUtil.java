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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.overlayvpn.brs.invdao.LogicalTernminationPointInvDao;
import org.openo.sdno.overlayvpn.brs.model.LogicalTernminationPointMO;
import org.openo.sdno.overlayvpn.model.v2.vxlan.Ip;
import org.openo.sdno.overlayvpn.model.v2.vxlan.NbiVxlanTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * portVlan helper class, used to build port-ip map for nbi vxlan tunnel models.<br>
 * 
 * @author
 * @version SDNO 0.5 Jan 12, 2017
 */
public class PortUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortUtil.class);

    /**
     * build port-ip map for nbi vxlan tunnel models.<br>
     * 
     * @param vxlanTunnels nbi vxlantunnels need to deal.
     * @return port-ip map build from given nbi vxlan models.
     * @throws ServiceException if inner error happens.
     * @since SDNO 0.5
     */
    public static Map<String, Ip> buildPortIpMap(List<NbiVxlanTunnel> vxlanTunnels) throws ServiceException {
        Map<String, String> neIdToPortNameMap = new HashMap<>();
        for(NbiVxlanTunnel tempVxlan : vxlanTunnels) {
            neIdToPortNameMap.put(tempVxlan.getSrcNeId(), tempVxlan.getSrcPortName());
            neIdToPortNameMap.put(tempVxlan.getDestNeId(), tempVxlan.getDestPortName());
        }
        return buildNeIdToIp(neIdToPortNameMap);
    }

    private static Map<String, Ip> buildNeIdToIp(Map<String, String> neIdToPortNameMap) throws ServiceException {
        Map<String, Ip> neIdToIp = new HashMap<>();
        List<LogicalTernminationPointMO> ltpMos = new LogicalTernminationPointInvDao().getAllMO();
        for(String neId : neIdToPortNameMap.keySet()) {
            String portName = neIdToPortNameMap.get(neId);
            for(LogicalTernminationPointMO ltp : ltpMos) {
                if(!ltp.getName().equals(portName)) {
                    continue;
                }
                neIdToIp.put(neId, getIpFromLtp(ltp));
            }
        }

        return neIdToIp;
    }

    private static Ip getIpFromLtp(LogicalTernminationPointMO ltp) throws ParameterServiceException {
        String ipAddress = ltp.getIpAddress();
        if(StringUtils.isEmpty(ipAddress) || ("0.0.0.0").equals(ipAddress)) {
            LOGGER.error("ip of port:" + ltp.getName() + "is empty");
            throw new ParameterServiceException("ip of port is empty.");
        }

        String ipMask = ltp.getIpMask();
        int mask = StringUtils.isEmpty(ipMask) ? (32) : Integer.parseInt(ipMask);
        Ip result = new Ip();
        result.setIpMask(String.valueOf(mask));
        result.setIpv4(ipAddress);
        return result;

    }

}
