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

package org.openo.sdno.vxlan.util.vxlanbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.model.common.enums.EndpointType;
import org.openo.sdno.overlayvpn.model.common.enums.vxlan.VxlanAccessType;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVtep;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInterface;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;

/**
 * Utility function to Build VxLAN instances (SBI structure).<br>
 * 
 * @author
 * @version SDNO 0.5 03-Jun-2016
 */
public abstract class CommonVxlanBuilder implements VxlanBuilder {

    private final Connection connection;

    private final Map<String, NeVtep> deviceIdToNeVtepMap;

    protected Map<String, WanSubInterface> deviceIdToWansubInfMap;

    protected List<EndpointGroup> originalEpgs = new ArrayList<EndpointGroup>();

    protected List<EndpointGroup> operEpgs = new ArrayList<EndpointGroup>();

    protected List<EndpointGroup> allEpgs = new ArrayList<EndpointGroup>();

    protected List<NeVxlanInstance> operVxlanInstances = new ArrayList<NeVxlanInstance>();

    protected List<NeVxlanInstance> otherVxlanInstances = new ArrayList<NeVxlanInstance>();

    protected String vni;

    /**
     * Constructor.<br>
     * 
     * @since SDNO 0.5
     * @param connection - Connection Information
     * @param deviceIdToNeVtepMap - Mapping of device id to VTEP
     * @param deviceIdToWansubInfMap - Mapping of device id to Wan Sub inteface
     * @param originalEpgs - during modify this will be present, original EPG
     * @param operEpgs - Operation EPG
     * @param vni - VxLan identifier
     */
    public CommonVxlanBuilder(Connection connection, Map<String, NeVtep> deviceIdToNeVtepMap,
            Map<String, WanSubInterface> deviceIdToWansubInfMap, List<EndpointGroup> originalEpgs,
            List<EndpointGroup> operEpgs, String vni) {
        this.connection = connection;
        this.deviceIdToWansubInfMap = deviceIdToWansubInfMap;
        this.deviceIdToNeVtepMap = deviceIdToNeVtepMap;
        this.vni = vni;

        if(CollectionUtils.isNotEmpty(originalEpgs)) {
            this.originalEpgs = originalEpgs;
        }

        if(CollectionUtils.isNotEmpty(operEpgs)) {
            this.operEpgs = operEpgs;
        }

        if(CollectionUtils.isNotEmpty(this.originalEpgs)) {
            allEpgs.addAll(this.originalEpgs);
        }

        if(CollectionUtils.isNotEmpty(this.operEpgs)) {
            allEpgs.addAll(this.operEpgs);
        }
    }

    @Override
    public abstract void buildNeVxlanInstance() throws ServiceException;

    @Override
    public List<NeVxlanInstance> getOperVxlanInstances() {
        return this.operVxlanInstances;
    }

    @Override
    public List<NeVxlanInstance> getOtherVxlanInstances() {
        return this.otherVxlanInstances;
    }

    /**
     * Build Local VxLan instances<br>
     * 
     * @param localEpg - local End point groups
     * @param epgsInPeerNes - End points in peers
     * @return List of NeVxLAN instances
     * @throws ServiceException - when input is invalid
     * @since SDNO 0.5
     */
    protected List<NeVxlanInstance> buildLocalNeVxlanInstances(EndpointGroup localEpg,
            List<EndpointGroup> epgsInPeerNes) throws ServiceException {
        List<NeVxlanInstance> vxlanInstanceList = new ArrayList<NeVxlanInstance>();
        if(CollectionUtils.isEmpty(epgsInPeerNes)) {
            return vxlanInstanceList;
        }

        // Construct VxLan Instance
        NeVxlanInstance neVxlanInstance = buildBasicVxlanInstance();
        vxlanInstanceList.add(neVxlanInstance);

        neVxlanInstance.setVni(this.vni);
        neVxlanInstance.setNeId(localEpg.getDeviceId());

        // Construct VxLan Interface
        List<NeVxlanInterface> vxlanInterfaceList = buildNeVxlanInterface(localEpg);
        neVxlanInstance.setVxlanInterfaceList(vxlanInterfaceList);

        // Construct VxLan Tunnels
        neVxlanInstance.setVxlanTunnelList(new ArrayList<NeVxlanTunnel>());

        for(EndpointGroup peerEpg : epgsInPeerNes) {
            NeVxlanTunnel vxlanTunnel = buildNeVxlanTunnel(localEpg, peerEpg);
            neVxlanInstance.getVxlanTunnelList().add(vxlanTunnel);
        }

        return vxlanInstanceList;
    }

    private List<NeVxlanInterface> buildNeVxlanInterface(EndpointGroup epg) throws ServiceException {

        List<NeVxlanInterface> vxlanInterfaceList = new ArrayList<NeVxlanInterface>();

        String epgType = epg.getType();

        if(EndpointType.VLAN.equals(epgType)) {
            for(String vlan : epg.getEndpointList()) {
                vxlanInterfaceList.add(buildVlanTypeInterface(epg.getNeId(), vlan));
            }
        }

        for(Entry<String, List<String>> entry : epg.getPortNativeIdToVlanMap().entrySet()) {

            if(EndpointType.PORT.equals(epg.getType())) {
                vxlanInterfaceList.add(buildPortTypeInterface(epg.getNeId(), entry.getKey()));
            } else if(EndpointType.PORT_VLAN.equals(epg.getType())) {
                for(String vlan : entry.getValue()) {
                    vxlanInterfaceList.add(buildPortVlanTypeInterface(epg.getNeId(), entry.getKey(), vlan));
                }
            }
        }

        return vxlanInterfaceList;
    }

    private NeVxlanTunnel buildNeVxlanTunnel(EndpointGroup localEpg, EndpointGroup peerEpg) throws ServiceException {
        String localDeviceId = localEpg.getDeviceId();
        WanSubInterface localWanSubIf = this.deviceIdToWansubInfMap.get(localDeviceId);
        NeVtep localVtep = this.deviceIdToNeVtepMap.get(localDeviceId);
        NeVtep peerVtep = this.deviceIdToNeVtepMap.get(peerEpg.getDeviceId());

        NeVxlanTunnel neVxlanTunnel = buildBasicVxlanTunnel();

        neVxlanTunnel.setVni(this.vni);
        if(null != localWanSubIf) {
            neVxlanTunnel.setSourceIfId(localWanSubIf.getName());
        }
        neVxlanTunnel.setSourceAddress(localVtep.getVtepIp());
        neVxlanTunnel.setDestAddress(peerVtep.getVtepIp());
        neVxlanTunnel.setNeId(localDeviceId);
        neVxlanTunnel.setPeerNeId(peerEpg.getDeviceId());
        return neVxlanTunnel;
    }

    private NeVxlanInstance buildBasicVxlanInstance() {
        NeVxlanInstance neVxlanInstance = new NeVxlanInstance();
        neVxlanInstance.setConnectionServiceId(this.connection.getUuid());
        neVxlanInstance.setTenantId(this.connection.getTenantId());
        neVxlanInstance.setName(this.connection.getName());
        neVxlanInstance.setDescription(this.connection.getDescription());
        return neVxlanInstance;
    }

    private NeVxlanInterface buildBasicVxlanInterface() {
        NeVxlanInterface neVxlanInterface = new NeVxlanInterface();
        neVxlanInterface.setConnectionServiceId(this.connection.getUuid());
        neVxlanInterface.setTenantId(this.connection.getTenantId());
        neVxlanInterface.setName(this.connection.getName());
        neVxlanInterface.setDescription(this.connection.getDescription());
        return neVxlanInterface;
    }

    private NeVxlanTunnel buildBasicVxlanTunnel() {
        NeVxlanTunnel neVxlanTunnel = new NeVxlanTunnel();
        neVxlanTunnel.setConnectionServiceId(this.connection.getUuid());
        neVxlanTunnel.setTenantId(this.connection.getTenantId());
        neVxlanTunnel.setName(this.connection.getName());
        neVxlanTunnel.setDescription(this.connection.getDescription());
        return neVxlanTunnel;
    }

    private NeVxlanInterface buildVlanTypeInterface(String neId, String vlan) {
        NeVxlanInterface neVxlanInterface = buildBasicVxlanInterface();

        neVxlanInterface.setAccessType(VxlanAccessType.DOT1Q.getName());
        neVxlanInterface.setDeviceId(neId);
        neVxlanInterface.setDot1qVlanBitmap(vlan);

        return neVxlanInterface;
    }

    private NeVxlanInterface buildPortTypeInterface(String neId, String portNativeId) {
        NeVxlanInterface neVxlanInterface = buildBasicVxlanInterface();

        neVxlanInterface.setAccessType(VxlanAccessType.PORT.getName());
        neVxlanInterface.setDeviceId(neId);
        neVxlanInterface.setPortId(portNativeId);

        return neVxlanInterface;
    }

    private NeVxlanInterface buildPortVlanTypeInterface(String neId, String portNativeId, String vlan) {
        NeVxlanInterface neVxlanInterface = buildBasicVxlanInterface();

        neVxlanInterface.setAccessType(VxlanAccessType.DOT1Q.getName());
        neVxlanInterface.setDeviceId(neId);
        neVxlanInterface.setPortId(portNativeId);
        neVxlanInterface.setDot1qVlanBitmap(vlan);

        return neVxlanInterface;
    }
}
