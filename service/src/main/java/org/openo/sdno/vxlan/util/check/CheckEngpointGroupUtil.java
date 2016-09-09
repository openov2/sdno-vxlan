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

package org.openo.sdno.vxlan.util.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.brs.invdao.LogicalTernminationPointInvDao;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.model.LogicalTernminationPointMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.consts.ValidationConsts;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.common.enums.EndpointType;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.util.check.UuidUtil;
import org.openo.sdno.vxlan.util.exception.ThrowVxlanExcpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Utility function to check EngpointGroup Information.<br>
 * 
 * @author
 * @version SDNO 0.5 03-Jun-2016
 */
public class CheckEngpointGroupUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckEngpointGroupUtil.class);

    private CheckEngpointGroupUtil() {
    }

    /**
     * Check EndPoint Data.<br>
     * 
     * @param epg EndpointGroup data
     * @throws ServiceException throws when data is wrong
     * @since SDNO 0.5
     */
    public static void checkEndpoints(EndpointGroup epg) throws ServiceException {
        String endpoints = epg.getEndpoints();
        try {
            List<String> endpointList = JsonUtil.fromJson(endpoints, new TypeReference<List<String>>() {});

            if(CollectionUtils.isEmpty(endpointList)) {
                ThrowVxlanExcpt.throwParmaterInvalid("Endpoints", endpoints);
            }

            String epgType = epg.getType();
            if(EndpointType.PORT.getName().equals(epgType)) {
                epg.setPortNativeIdToVlanMap(checkEndpointsForPort(endpointList));
            } else if(EndpointType.PORT_VLAN.getName().equals(epgType)) {
                epg.setPortNativeIdToVlanMap(checkEndpointsForPortVlan(endpointList));
            } else if(EndpointType.CIDR.getName().equals(epgType)) {
                checkEndpointsForCidr(endpointList);
            } else {
                ThrowVxlanExcpt.throwParmaterInvalid("Invalid Epg Type", epgType);
            }

            epg.setEndpointList(endpointList);
        } catch(IllegalArgumentException e) {
            LOGGER.error("content of endpoints is not a valid list of string:" + endpoints, e);
            ThrowVxlanExcpt.throwParmaterInvalid("Endpoints", endpoints);
        }
    }

    /**
     * Check EndpointGroup data.<br>
     * 
     * @param epg EndpointGroup data
     * @param deviceIdToNeMap Map of device Id to NetworkElement
     * @throws ServiceException throws when data is wrong
     * @since SDNO 0.5
     */
    public static void checkResourceInEpg(EndpointGroup epg, Map<String, NetworkElementMO> deviceIdToNeMap)
            throws ServiceException {
        String epgNeId = epg.getNeId();

        NetworkElementInvDao neDao = new NetworkElementInvDao();
        NetworkElementMO tempNetworkElement = neDao.query(epgNeId);

        if(null == tempNetworkElement) {
            LOGGER.error("ne not exist: " + epgNeId);
            ThrowVxlanExcpt.throwResNotExistAsBadReq(ErrorCode.RESOURCE_NETWORKELEMENT_NOT_EXIST, epgNeId);
            return;
        }

        String deviceId = tempNetworkElement.getNativeID();
        if(!StringUtils.hasLength(deviceId)) {
            ThrowVxlanExcpt.throwParmaterInvalid("device id", tempNetworkElement.getName());
        }

        epg.setDeviceId(deviceId);
        if(!deviceIdToNeMap.containsKey(deviceId)) {
            deviceIdToNeMap.put(deviceId, tempNetworkElement);
        }

        if(EndpointType.CIDR.getName().equals(epg.getType())) {
            setLanAccess(epg);
        } else {
            checkLtpResource(epg);
        }
    }

    private static Map<String, List<String>> checkEndpointsForPort(List<String> endpointList) throws ServiceException {
        Map<String, List<String>> portIdToVlanMap = new HashMap<String, List<String>>();
        for(String portId : endpointList) {
            if(!UuidUtil.validate(portId)) {
                ThrowVxlanExcpt.throwParmaterInvalid("PortId", portId);
            }

            portIdToVlanMap.put(portId, new ArrayList<String>());
        }

        return portIdToVlanMap;
    }

    private static Map<String, List<String>> checkEndpointsForPortVlan(List<String> endpointList)
            throws ServiceException {
        Map<String, List<String>> portIdToVlanMap = new HashMap<String, List<String>>();
        for(String endpoint : endpointList) {
            String[] portIdAndVlan = endpoint.split("/");
            if(null == portIdAndVlan || portIdAndVlan.length != 2) {
                LOGGER.error("the format of endpoint is not valid:" + endpoint);
            }

            String portId = portIdAndVlan[0];
            if(!UuidUtil.validate(portId)) {
                ThrowVxlanExcpt.throwParmaterInvalid("PortId", portId);
            }

            if(!portIdToVlanMap.containsKey(portId)) {
                portIdToVlanMap.put(portId, new ArrayList<String>());
            }

            String vlanRangesStr = portIdAndVlan[1];
            String[] vlanRanges = vlanRangesStr.split(",");
            List<String> vlans = new ArrayList<String>();
            for(String vlanRange : vlanRanges) {
                if(!StringUtils.hasLength(vlanRange)) {
                    ThrowVxlanExcpt.throwParmaterInvalid("vlan", vlanRangesStr);
                }

                String[] vlanBoundary = vlanRange.split("-");
                if(vlanBoundary.length == 1) {
                    vlans.add(vlanBoundary[0]);
                } else if(vlanBoundary.length == 2) {
                    for(int i = Integer.parseInt(vlanBoundary[0]); i <= Integer.parseInt(vlanBoundary[1]); i++) {
                        vlans.add(String.valueOf(i));
                    }
                }
            }

            portIdToVlanMap.get(portId).addAll(vlans);
        }

        return portIdToVlanMap;
    }

    private static void checkEndpointsForCidr(List<String> endpointList) throws ServiceException {
        for(String endpoint : endpointList) {
            if(!endpoint.matches(ValidationConsts.IP_MASK_REGEX)) {
                ThrowVxlanExcpt.throwParmaterInvalid("Endpoint", endpoint);
            }
        }
    }

    private static void checkLtpResource(EndpointGroup epg) throws ServiceException {

        Map<String, List<String>> portIdToVlanMap = epg.getPortNativeIdToVlanMap();
        List<String> portIds = new ArrayList<String>(portIdToVlanMap.keySet());

        LogicalTernminationPointInvDao ltpDao = new LogicalTernminationPointInvDao();

        Map<String, List<String>> portNativeIdToVlanMap = new HashMap<String, List<String>>();
        String epgNeId = epg.getNeId();
        for(String ltpId : portIds) {
            LogicalTernminationPointMO ltp = ltpDao.query(ltpId);
            if(null == ltp) {
                LOGGER.error("port uuid:" + ltpId + " not exists");
                ThrowVxlanExcpt.throwResNotExistAsBadReq("Termination Point", ltpId);
                return;
            }

            String ltpNativeId = ltp.getNativeID();
            if(!StringUtils.hasLength(ltpNativeId)) {
                LOGGER.error("ltp:" + ltpId + " doesn't contain native id");
                ThrowVxlanExcpt.throwParmaterInvalid("Termination Point", ltpId);
            }

            if(!epgNeId.equals(ltp.getMeID())) {
                LOGGER.error("port id:" + ltpId + "doesn't belong to ne:" + epgNeId);
                ThrowVxlanExcpt.throwParmaterInvalid("Termination Point", ltpId);
            }

            portNativeIdToVlanMap.put(ltpNativeId, portIdToVlanMap.get(ltpId));
        }

        epg.setPortNativeIdToVlanMap(portNativeIdToVlanMap);
    }

    private static void setLanAccess(EndpointGroup epg) throws ServiceException {
        epg.setType(EndpointType.VLAN.getName());
        epg.setEndpointList(Arrays.asList("1"));
    }
}
