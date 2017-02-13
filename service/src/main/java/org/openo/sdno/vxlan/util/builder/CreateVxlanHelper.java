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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.model.common.enums.vxlan.VxlanAccessType;
import org.openo.sdno.overlayvpn.model.v2.vxlan.Ip;
import org.openo.sdno.overlayvpn.model.v2.vxlan.NbiVxlanTunnel;
import org.openo.sdno.overlayvpn.model.v2.vxlan.PortVlan;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanInstance;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanInterface;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanTunnel;
import org.openo.sdno.vxlan.constant.NeRoleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create sbi model from nbi models.<br>
 * 
 * @author
 * @version SDNO 0.5 Jan 12, 2017
 */
public class CreateVxlanHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateVxlanHelper.class);

    /**
     * fill back ip object to nbi vxlan tunnels.<br>
     * 
     * @param neIdToIpMap ne id to ip map.
     * @param vxlanTunnels vxlantunnels to fill ips.
     * @since SDNO 0.5
     */
    public static void fillIpBackToTunnel(Map<String, Ip> neIdToIpMap, List<NbiVxlanTunnel> vxlanTunnels) {
        LOGGER.info("=====fill back ip to vxlan tunnel=====");
        for(NbiVxlanTunnel vxlanTunnel : vxlanTunnels) {

            String srcNeId = vxlanTunnel.getSrcNeId();
            String destNeId = vxlanTunnel.getDestNeId();

            if(null != neIdToIpMap.get(srcNeId)) {
                Ip srcIp = copyIp(neIdToIpMap.get(srcNeId));
                srcIp.allocateUuid();
                srcIp.setVxlanTunnelId(vxlanTunnel.getUuid());
                vxlanTunnel.setSrcIp(srcIp);
                vxlanTunnel.getSrcIp().setNeId(srcNeId);
            }
            if(null != neIdToIpMap.get(destNeId)) {
                Ip destIp = copyIp(neIdToIpMap.get(destNeId));
                destIp.allocateUuid();
                destIp.setVxlanTunnelId(vxlanTunnel.getUuid());
                vxlanTunnel.setDestIp(destIp);
                vxlanTunnel.getDestIp().setNeId(destNeId);
            }

        }

    }

    private static Ip copyIp(Ip from) {
        Ip to = new Ip();
        to.setDeviceId(from.getDeviceId());
        to.setNeId(from.getNeId());
        to.setIpv4(from.getIpv4());
        to.setIpMask(from.getIpMask());
        to.setPrefixLength(from.getPrefixLength());
        to.setIpv6(from.getIpv6());
        to.setVxlanTunnelId(from.getVxlanTunnelId());
        return to;
    }

    /**
     * build sbi models from nbi models.<br>
     * 
     * @param neToCtrlMap ne to controller map.
     * @param vxlanTunnels nbi vxlantunnels to transfer.
     * @return list of SbiNeVxlanInstance created from given nbi models.
     * @since SDNO 0.5
     */
    public static List<SbiNeVxlanInstance> nbiToSbi(Map<NetworkElementMO, ControllerMO> neToCtrlMap,
            List<NbiVxlanTunnel> vxlanTunnels) {
        LOGGER.info("=====start transfer nbi model to sbi model=====");
        List<SbiNeVxlanInstance> sbivxlanList = new ArrayList<SbiNeVxlanInstance>();

        for(NbiVxlanTunnel nbiVxlan : vxlanTunnels) {
            SbiNeVxlanInstance srcVxlanInstance =
                    buildNeVxlanInstance(nbiVxlan, nbiVxlan.getSrcNeId(), nbiVxlan.getSrcNeRole());
            sbivxlanList.add(srcVxlanInstance);

            SbiNeVxlanInstance destVxlanInstance =
                    buildNeVxlanInstance(nbiVxlan, nbiVxlan.getDestNeId(), nbiVxlan.getDestNeRole());
            sbivxlanList.add(destVxlanInstance);
        }

        return sbivxlanList;
    }

    private static SbiNeVxlanInstance buildNeVxlanInstance(NbiVxlanTunnel nbiVxlan, String neId, String neRole) {
        LOGGER.info("=====start building sbi model=====");
        SbiNeVxlanInstance sbiVxlanInstance = buildBasicField(nbiVxlan);
        sbiVxlanInstance.setExternalId(sbiVxlanInstance.getUuid());
        sbiVxlanInstance.setNbiVxlanTunnelId(nbiVxlan.getUuid());
        sbiVxlanInstance.setVni(nbiVxlan.getVni());
        sbiVxlanInstance.setDeviceId(neId);
        if(neId.equals(nbiVxlan.getSrcNeId())) {
            sbiVxlanInstance.setPeerDeviceId(nbiVxlan.getDestNeId());
        } else {
            sbiVxlanInstance.setPeerDeviceId(nbiVxlan.getSrcNeId());
        }

        String vxlanInsId = sbiVxlanInstance.getUuid();
        sbiVxlanInstance.setVxlanInterfaceList(new ArrayList<SbiNeVxlanInterface>());

        if(NeRoleType.LOCALCPE.getName().equals(neRole)) {
            sbiVxlanInstance.setVxlanInterfaceList(buildVxlanInterfaceList(nbiVxlan, neId));
            for(SbiNeVxlanInterface vxlanIf : sbiVxlanInstance.getVxlanInterfaceList()) {
                vxlanIf.setVxlanInstanceId(vxlanInsId);
            }
        }

        SbiNeVxlanTunnel neVxlanTunnel = buildNeVxlanTunnel(nbiVxlan, neId);
        neVxlanTunnel.setVxlanInstanceId(vxlanInsId);
        sbiVxlanInstance.setVxlanTunnelList(Arrays.asList(neVxlanTunnel));

        return sbiVxlanInstance;
    }

    private static SbiNeVxlanTunnel buildNeVxlanTunnel(NbiVxlanTunnel nbiVxlan, String neId) {
        SbiNeVxlanTunnel neVxlanTunnel = buildBasicVxlanTunnel(nbiVxlan);
        neVxlanTunnel.setVni(nbiVxlan.getVni());

        if(neId.equals(nbiVxlan.getSrcNeId())) {
            neVxlanTunnel.setSourceIfId(nbiVxlan.getSrcPortName());
            neVxlanTunnel.setDeviceId(nbiVxlan.getSrcNeId());
            neVxlanTunnel.setPeerDeviceId(nbiVxlan.getDestNeId());
        } else {
            neVxlanTunnel.setSourceIfId(nbiVxlan.getDestPortName());
            neVxlanTunnel.setDeviceId(nbiVxlan.getDestNeId());
            neVxlanTunnel.setPeerDeviceId(nbiVxlan.getSrcNeId());
        }
        return neVxlanTunnel;
    }

    private static SbiNeVxlanTunnel buildBasicVxlanTunnel(NbiVxlanTunnel nbiVxlan) {
        SbiNeVxlanTunnel neVxlanTunnel = new SbiNeVxlanTunnel();
        neVxlanTunnel.allocateUuid();
        neVxlanTunnel.setConnectionId(nbiVxlan.getConnectionId());
        neVxlanTunnel.setTenantId(nbiVxlan.getTenantId());
        neVxlanTunnel.setName(nbiVxlan.getName());
        neVxlanTunnel.setDescription(nbiVxlan.getDescription());
        neVxlanTunnel.setDeployStatus(nbiVxlan.getDeployStatus());
        neVxlanTunnel.setActiveStatus(nbiVxlan.getActiveStatus());
        return neVxlanTunnel;

    }

    private static List<SbiNeVxlanInterface> buildVxlanInterfaceList(NbiVxlanTunnel nbiVxlan, String neId) {
        List<SbiNeVxlanInterface> vxlanInterfaceList = new ArrayList<SbiNeVxlanInterface>();
        if(CollectionUtils.isEmpty(nbiVxlan.getPortVlans())) {
            return vxlanInterfaceList;
        }
        for(PortVlan portVlan : nbiVxlan.getPortVlans()) {
            if(!neId.equals(portVlan.getNeId())) {
                continue;
            }
            buildNeVxlanInterface(nbiVxlan, neId, portVlan, vxlanInterfaceList);
        }
        return vxlanInterfaceList;
    }

    private static void buildNeVxlanInterface(NbiVxlanTunnel nbiVxlan, String neId, PortVlan portVlan,
            List<SbiNeVxlanInterface> vxlanInterfaceList) {
        String portId = portVlan.getPort();
        String vlxnRangeListStr = portVlan.getVlan();
        LOGGER.info("start build NeVxlanInterface");

        if((!StringUtils.isEmpty(portId)) && (!StringUtils.isEmpty(vlxnRangeListStr))) {
            for(String vlan : portVlan.getVlanList()) {
                vxlanInterfaceList.add(buildInterfaceAsPortVlan(neId, portVlan, vlan, nbiVxlan));
            }
        } else if((!StringUtils.isEmpty(portId)) && (StringUtils.isEmpty(vlxnRangeListStr))) {
            vxlanInterfaceList.add(buildInterfaceAsPort(neId, portVlan, nbiVxlan));
        } else if((StringUtils.isEmpty(portId)) && (!StringUtils.isEmpty(vlxnRangeListStr))) {
            for(String vlan : portVlan.getVlanList()) {
                vxlanInterfaceList.add(buildInterfaceAsVlan(neId, vlan, nbiVxlan));
            }
        }
    }

    private static SbiNeVxlanInterface buildInterfaceAsVlan(String neId, String vlan, NbiVxlanTunnel nbiVxlan) {
        SbiNeVxlanInterface neVxlanInterface = buildBasicVxlanInterface(nbiVxlan);
        neVxlanInterface.setAccessType(VxlanAccessType.DOT1Q.getName());
        neVxlanInterface.setDeviceId(neId);
        neVxlanInterface.setDot1qVlanBitmap(vlan);
        return neVxlanInterface;
    }

    private static SbiNeVxlanInterface buildInterfaceAsPort(String neId, PortVlan portVlan, NbiVxlanTunnel nbiVxlan) {
        SbiNeVxlanInterface neVxlanInterface = buildBasicVxlanInterface(nbiVxlan);
        neVxlanInterface.setAccessType(VxlanAccessType.PORT.getName());
        neVxlanInterface.setDeviceId(neId);
        neVxlanInterface.setPortNativeId(portVlan.getPortNativeId());
        neVxlanInterface.setLocalName(portVlan.getPortName());
        return neVxlanInterface;
    }

    private static SbiNeVxlanInterface buildInterfaceAsPortVlan(String neId, PortVlan portVlan, String vlan,
            NbiVxlanTunnel nbiVxlan) {
        SbiNeVxlanInterface neVxlanInterface = buildBasicVxlanInterface(nbiVxlan);
        neVxlanInterface.setAccessType(VxlanAccessType.DOT1Q.getName());
        neVxlanInterface.setDeviceId(neId);
        neVxlanInterface.setPortNativeId(portVlan.getPortNativeId());
        neVxlanInterface.setLocalName(portVlan.getPortName());
        neVxlanInterface.setDot1qVlanBitmap(vlan);
        return neVxlanInterface;
    }

    private static SbiNeVxlanInterface buildBasicVxlanInterface(NbiVxlanTunnel nbiVxlan) {
        SbiNeVxlanInterface neVxlanInterface = new SbiNeVxlanInterface();
        neVxlanInterface.allocateUuid();
        neVxlanInterface.setConnectionId(nbiVxlan.getConnectionId());
        neVxlanInterface.setTenantId(nbiVxlan.getTenantId());
        neVxlanInterface.setName(nbiVxlan.getName());
        neVxlanInterface.setDescription(nbiVxlan.getDescription());
        neVxlanInterface.setDeployStatus(nbiVxlan.getDeployStatus());
        neVxlanInterface.setActiveStatus(nbiVxlan.getActiveStatus());
        return neVxlanInterface;
    }

    private static SbiNeVxlanInstance buildBasicField(NbiVxlanTunnel nbiVxlan) {
        SbiNeVxlanInstance sbiVxlanInstance = new SbiNeVxlanInstance();
        sbiVxlanInstance.allocateUuid();
        sbiVxlanInstance.setConnectionId(nbiVxlan.getConnectionId());
        sbiVxlanInstance.setTenantId(nbiVxlan.getTenantId());
        sbiVxlanInstance.setName(nbiVxlan.getName());
        sbiVxlanInstance.setDescription(nbiVxlan.getDescription());
        sbiVxlanInstance.setDeployStatus(nbiVxlan.getDeployStatus());
        sbiVxlanInstance.setActiveStatus(nbiVxlan.getActiveStatus());
        return sbiVxlanInstance;
    }

    /**
     * replace ne id with device id before sent to adapter.<br>
     * 
     * @param sbivxlan sbi vxlan model to change.
     * @param keySet ne set.
     * @since SDNO 0.5
     */
    public static void replaceNeIdWithDeviceId(SbiNeVxlanInstance sbivxlan, Collection<NetworkElementMO> keySet) {
        Map<String, String> neIdToDeviceId = buildNeIdToDeviceId(keySet);
        sbivxlan.setDeviceId(neIdToDeviceId.get(sbivxlan.getDeviceId()));
        sbivxlan.setPeerDeviceId(neIdToDeviceId.get(sbivxlan.getPeerDeviceId()));

        if(CollectionUtils.isNotEmpty(sbivxlan.getVxlanTunnelList())) {
            for(SbiNeVxlanTunnel vxlanTunnel : sbivxlan.getVxlanTunnelList()) {
                vxlanTunnel.setDeviceId(neIdToDeviceId.get(vxlanTunnel.getDeviceId()));
                vxlanTunnel.setPeerDeviceId(neIdToDeviceId.get(vxlanTunnel.getPeerDeviceId()));
            }
        }

        if(CollectionUtils.isNotEmpty(sbivxlan.getVxlanInterfaceList())) {
            for(SbiNeVxlanInterface vxlaninterface : sbivxlan.getVxlanInterfaceList()) {
                vxlaninterface.setDeviceId(neIdToDeviceId.get(vxlaninterface.getDeviceId()));
            }

        }

    }

    private static Map<String, String> buildNeIdToDeviceId(Collection<NetworkElementMO> keySet) {
        Map<String, String> neIdToDeviceId = new HashMap<String, String>();
        for(NetworkElementMO ne : keySet) {
            String neId = ne.getId();
            if(!neIdToDeviceId.containsKey(neId)) {
                neIdToDeviceId.put(neId, ne.getNativeID());
            }
        }
        return neIdToDeviceId;
    }

}
