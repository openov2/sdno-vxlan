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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVtep;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class of FullMesh VxLan Builder.<br>
 * 
 * @author
 * @version SDNO 0.5 21-July-2016
 */
public class FullMeshVxlanBuilder extends CommonVxlanBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FullMeshVxlanBuilder.class);

    /**
     * Constructor<br>
     * 
     * @param connection OverlayVpn Connection
     * @param deviceIdToNeVtepMap Map of device id to VTEP
     * @param deviceIdToWansubInfMap Map of Device id to WanSubInterface
     * @param originalEpgs during modify this will be present, original EPG
     * @param operEpgs Operation EPG
     * @param vni VxLan identifier
     * @since SDNO 0.5
     */
    public FullMeshVxlanBuilder(Connection connection, Map<String, NeVtep> deviceIdToNeVtepMap,
            Map<String, WanSubInterface> deviceIdToWansubInfMap, List<EndpointGroup> originalEpgs,
            List<EndpointGroup> operEpgs, String vni) {
        super(connection, deviceIdToNeVtepMap, deviceIdToWansubInfMap, originalEpgs, operEpgs, vni);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void buildNeVxlanInstance() throws ServiceException {
        if(CollectionUtils.isEmpty(this.operEpgs) || MapUtils.isEmpty(this.deviceIdToWansubInfMap)) {
            LOGGER.warn("No data need to build!!");
            return;
        }

        for(EndpointGroup operEpg : this.operEpgs) {
            final String operEpgNeId = operEpg.getNeId();

            List<EndpointGroup> epgsInPeerNes = new ArrayList<>(CollectionUtils.select(this.allEpgs, new Predicate() {

                @Override
                public boolean evaluate(Object arg0) {
                    return !operEpgNeId.equals(((EndpointGroup)arg0).getNeId());
                }
            }));

            this.operVxlanInstances.addAll(super.buildLocalNeVxlanInstances(operEpg, epgsInPeerNes));
        }

        for(EndpointGroup oriEpg : originalEpgs) {
            final String oriEpgNeId = oriEpg.getNeId();

            List<EndpointGroup> epgsInPeerNes = new ArrayList<>(CollectionUtils.select(this.allEpgs, new Predicate() {

                @Override
                public boolean evaluate(Object arg0) {
                    return !oriEpgNeId.equals(((EndpointGroup)arg0).getNeId());
                }
            }));

            this.otherVxlanInstances.addAll(super.buildLocalNeVxlanInstances(oriEpg, epgsInPeerNes));
        }
    }
}
