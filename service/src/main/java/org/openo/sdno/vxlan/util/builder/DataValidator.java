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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.model.v2.vxlan.NbiVxlanTunnel;
import org.openo.sdno.overlayvpn.model.v2.vxlan.PortVlan;
import org.openo.sdno.vxlan.constant.NeRoleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * validation class, used to check data during creating vxlan.<br>
 *
 * @author
 * @version SDNO 0.5 Jan 11, 2017
 */
public class DataValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataValidator.class);

    private static final String VLAN_RANGE_REGEX = "\\d+(-\\d+)?((,\\d+)+(-\\d+)?)*";

    private DataValidator() {
    }

    /**
     * check whether input ne is illegal<br>
     *
     * @param vxlanTunnels nbi vxlan tunnels to validate.
     * @throws ServiceException if inner error happens
     * @since SDNO 0.5
     */
    public static Map<String, NetworkElementMO> checkInputNe(List<NbiVxlanTunnel> vxlanTunnels)
            throws ServiceException {

        for(NbiVxlanTunnel vxlanTunnel : vxlanTunnels) {
            isNeIdNotSame(vxlanTunnel);
            isNeRoleLegal(vxlanTunnel);
        }

        Set<String> neIdSet = new HashSet<>();
        for(NbiVxlanTunnel vxlanTunnel : vxlanTunnels) {
            neIdSet.add(vxlanTunnel.getSrcNeId());
            neIdSet.add(vxlanTunnel.getDestNeId());
        }

        return checkNeResouce(neIdSet, vxlanTunnels);

    }

    /**
     * check port vlan resource is valid.<br>
     * 
     * @param vxlanTunnels nbi vxlan tunnels to check.
     * @throws ServiceException if inner error happens
     * @since SDNO 0.5
     */
    public static void checkVlanPortResource(List<NbiVxlanTunnel> vxlanTunnels) throws ServiceException {
        Map<String, List<PortVlan>> tunnelIdToPortVlan = checkPortVlan(vxlanTunnels);
        writeBackPortVlanToTunnel(tunnelIdToPortVlan, vxlanTunnels);

    }

    private static void writeBackPortVlanToTunnel(Map<String, List<PortVlan>> tunnelIdToPortVlan,
            List<NbiVxlanTunnel> vxlanTunnels) {
        for(NbiVxlanTunnel tempTunnel : vxlanTunnels) {
            tempTunnel.setPortVlans(tunnelIdToPortVlan.get(tempTunnel.getUuid()));
        }
    }

    private static Map<String, List<PortVlan>> checkPortVlan(List<NbiVxlanTunnel> vxlanTunnels)
            throws ServiceException {
        Map<String, List<PortVlan>> tunnelIdToPortVlan = new HashMap<>();

        for(NbiVxlanTunnel vxlanTunnel : vxlanTunnels) {
            String tempId = vxlanTunnel.getUuid();
            List<PortVlan> portList =
                    JsonUtil.fromJson(vxlanTunnel.getPortVlanList(), new TypeReference<List<PortVlan>>() {});

            if(CollectionUtils.isEmpty(portList)) {
                LOGGER.error("port list in vxlan nbi model is empty.");
                throw new ParameterServiceException("port list in vxlan nbi model is empty.");
            }
            checkPortVlanBase(vxlanTunnel, portList);
            tunnelIdToPortVlan.put(tempId, portList);
        }

        return tunnelIdToPortVlan;

    }

    private static void checkNeBaseData(String tempId, NetworkElementMO tempNe) throws ParameterServiceException {
        if(null == tempNe) {
            LOGGER.error("missing ne id is:" + tempId);
            throw new ParameterServiceException("missing ne.");
        }
        if(StringUtils.isEmpty(tempNe.getNativeID())) {
            LOGGER.error("native id of ne is null:" + tempId);
            throw new ParameterServiceException("native id of ne is null.");
        }
        if(StringUtils.isEmpty(tempNe.getNeRole())) {
            LOGGER.error("native role of ne is null:" + tempId);
            throw new ParameterServiceException("native role of ne is null.");
        }

    }

    private static Map<String, NetworkElementMO> checkNeResouce(Set<String> neIdSet, List<NbiVxlanTunnel> vxlanTunnels)
            throws ServiceException {

        if(neIdSet.isEmpty()) {
            LOGGER.error("ne list id empty.");
            throw new ParameterServiceException("ne list id empty.");
        }
        List<NetworkElementMO> neFromBrs = NeControllerUtil.getAllFullNe();

        Map<String, NetworkElementMO> idToNeMap = new HashMap<>();

        for(String tempId : neIdSet) {
            NetworkElementMO tempNe = new NetworkElementMO();
            for(NetworkElementMO tempNeMo : neFromBrs) {
                if(tempId.equals(tempNeMo.getId())) {
                    tempNe = tempNeMo;
                    break;
                }
            }

            checkNeBaseData(tempId, tempNe);

            if(!idToNeMap.containsKey(tempNe.getId())) {
                idToNeMap.put(tempNe.getId(), tempNe);
            }
        }

        checkNeRole(idToNeMap, vxlanTunnels);

        return idToNeMap;

    }

    private static void checkNeRole(Map<String, NetworkElementMO> idToNeMap, List<NbiVxlanTunnel> vxlanTunnels)
            throws ParameterServiceException {
        Map<String, String> check = new HashMap<>();
        for(NbiVxlanTunnel tunnel : vxlanTunnels) {
            check.put(tunnel.getSrcNeId(), tunnel.getSrcNeRole());
            check.put(tunnel.getDestNeId(), tunnel.getDestNeRole());
        }

        for(Map.Entry<String, String> entry : check.entrySet()) {
            NeRoleType neRoleType = NeRoleType.convertFromBrsNeRoleType(idToNeMap.get(entry.getKey()).getNeRole());
            if(!entry.getValue().equals(neRoleType.getName())) {
                LOGGER.error("ne role from brs is not same as in vxlanTunnel model.");
                throw new ParameterServiceException("ne role from brs is not same as in vxlanTunnel model.");
            }

        }
    }

    private static void checkPortVlanBase(NbiVxlanTunnel vxlanTunnel, List<PortVlan> portList) throws ServiceException {
        String srcNeId = vxlanTunnel.getSrcNeId();
        String destNeId = vxlanTunnel.getDestNeId();
        for(PortVlan port : portList) {
            port.setVxlanTunnelId(vxlanTunnel.getUuid());
            checkBasicPort(srcNeId, destNeId, port);

            if(!StringUtils.isEmpty(port.getVlan())) {
                checkVlan(port);
            }
        }

        if(NeRoleType.LOCALCPE.getName().equals(vxlanTunnel.getSrcNeRole())) {
            checkTunnelHasVlan(portList, srcNeId);
        }
        if(NeRoleType.LOCALCPE.getName().equals(vxlanTunnel.getSrcNeRole())) {
            checkTunnelHasVlan(portList, destNeId);
        }

    }

    private static void checkBasicPort(String srcNeId, String destNeId, PortVlan port) throws ServiceException {
        if(!port.getNeId().equals(srcNeId) && !port.getNeId().equals(destNeId)) {
            LOGGER.error("Ne id do not match srcId/destId in vxlantunnel." + port.getNeId());
            throw new ParameterServiceException("Ne id do not match srcId/destId in vxlantunnel.");
        }

        if(StringUtils.isEmpty(port.getVlan()) && StringUtils.isEmpty(port.getPort())) {
            LOGGER.error("both vlan and port of portvlan is empty." + port.getNeId());
            throw new ParameterServiceException("both vlan and port of portvlan is empty.");
        }

    }

    private static void checkTunnelHasVlan(List<PortVlan> portList, String neid) throws ParameterServiceException {
        boolean hasPortVlan = false;
        for(PortVlan portVlan : portList) {
            if(neid.equals(portVlan.getNeId())) {
                hasPortVlan = true;
                break;
            }
        }
        if(!hasPortVlan) {
            LOGGER.error("Ne" + neid + "not has port vlan.");
            throw new ParameterServiceException("Ne not has port vlan.");
        }
    }

    private static void checkVlan(PortVlan port) throws ParameterServiceException {
        if(null == port.getVlanList()) {
            port.setVlanList(new ArrayList<String>());
        }

        String vlanRangeList = port.getVlan();
        if(!vlanRangeList.matches(VLAN_RANGE_REGEX)) {
            LOGGER.error("invalid vlan format" + port.getPortName());
            throw new ParameterServiceException("invalid vlan format.");
        }

        String[] vlanRanges = vlanRangeList.split(",");
        if(CollectionUtils.isEmpty(Arrays.asList(vlanRanges))) {
            LOGGER.error("empty vlan list." + port.getPortName());
            throw new ParameterServiceException("empty vlan list.");
        }

        for(String vlanRange : vlanRanges) {
            buildVlan(port, vlanRange);
        }

    }

    private static void buildVlan(PortVlan port, String vlanRange) throws ParameterServiceException {
        String[] vlanRangeBound = vlanRange.split("-");
        try {
            int vlanLBound = Integer.parseInt(vlanRangeBound[0]);
            int vlanUBound = Integer.parseInt(vlanRangeBound[vlanRangeBound.length - 1]);

            if(vlanLBound >= vlanUBound) {
                LOGGER.error("invalid vlan bound of lower bound:{0} and upper bound:{1}." + port.getPortName());
                throw new ParameterServiceException("invalid vlan bound of lower bound:{0} and upper bound:{1}.");
            } else if(vlanLBound < 1 || vlanUBound > 4094) {
                LOGGER.error("vlan out of bound: 1-4094l." + port.getPortName());
                throw new ParameterServiceException("vlan out of bound: 1-4094.");
            }

            for(int vlan = vlanLBound; vlan < vlanUBound; vlan++) {
                port.getVlanList().add(String.valueOf(vlan));
            }

        } catch(NumberFormatException e) {
            LOGGER.error("vlan parse error." + e.getMessage());
            throw new ParameterServiceException("vlan parse error.");
        }

    }

    private static void isNeIdNotSame(NbiVxlanTunnel vxlanTunnel) throws ServiceException {
        if(vxlanTunnel.getSrcNeId().equals(vxlanTunnel.getDestNeId())) {
            LOGGER.error("srcNeId and destNeId of vxlanTunnel can not be the same.");
            throw new ParameterServiceException("srcNeId and destNeId of vxlanTunnel can not be the same.");
        }

    }

    private static void isNeRoleLegal(NbiVxlanTunnel vxlanTunnel) throws ParameterServiceException {
        String vpc = NeRoleType.VPC.getName();
        if(vxlanTunnel.getSrcNeId().equals(vpc) || vxlanTunnel.getDestNeId().equals(vpc)) {
            LOGGER.error("ne role of vpc is not supported in vxlan tunnel.");
            throw new ParameterServiceException("ne role of vpc is not supported in vxlan tunnel.");
        }
    }

}
