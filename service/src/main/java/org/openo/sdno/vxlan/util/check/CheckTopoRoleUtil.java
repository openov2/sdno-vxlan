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
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.model.common.enums.topo.TopologyRole;
import org.openo.sdno.overlayvpn.model.common.enums.topo.TopologyType;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.vxlan.util.exception.ThrowVxlanExcpt;

/**
 * Utility function to check topo role information.<br>
 * 
 * @author
 * @version SDNO 0.5 Aug 22, 2016
 */
public class CheckTopoRoleUtil {

    private CheckTopoRoleUtil() {
    }

    /**
     * Check TopoRole data.<br>
     * 
     * @param overlayVpn OverlayVpn data
     * @throws ServiceException throws when data is wrong
     * @since SDNO 0.5
     */
    @SuppressWarnings("unchecked")
    public static void checkTopoRole(OverlayVpn overlayVpn) throws ServiceException {
        for(Connection tempConnection : overlayVpn.getVpnConnections()) {
            if(TopologyType.HUB_SPOKE.getName().equals(tempConnection.getTopology())) {
                List<EndpointGroup> hubEpgs =
                        new ArrayList<>(CollectionUtils.select(tempConnection.getEndpointGroups(), new Predicate() {

                            @Override
                            public boolean evaluate(Object arg0) {
                                EndpointGroup epg = (EndpointGroup)arg0;
                                return TopologyRole.HUB.getName().equals(epg.getTopologyRole());
                            }
                        }));

                List<EndpointGroup> spokeEpgs =
                        new ArrayList<>(CollectionUtils.select(tempConnection.getEndpointGroups(), new Predicate() {

                            @Override
                            public boolean evaluate(Object arg0) {
                                EndpointGroup epg = (EndpointGroup)arg0;
                                return TopologyRole.SPOKE.getName().equals(epg.getTopologyRole());
                            }
                        }));

                if(CollectionUtils.isEmpty(hubEpgs) || hubEpgs.size() > 1) {
                    ThrowVxlanExcpt.throwParmaterInvalid("Hub Endpointgroups",
                            JsonUtil.toJson(hubEpgs) + " is empty or size exceeds 1");
                }

                if(CollectionUtils.isEmpty(hubEpgs)) {
                    ThrowVxlanExcpt.throwParmaterInvalid("connection topology type no hub", "");
                }

                if(CollectionUtils.isEmpty(spokeEpgs)) {
                    ThrowVxlanExcpt.throwParmaterInvalid("connection topology type no spoke", "");
                }
            } else if((TopologyType.POINT_TO_POINT.getName().equals(tempConnection.getTopology()))
                    && (tempConnection.getEndpointGroups().size() != 2)) {

                ThrowVxlanExcpt.throwParmaterInvalid("point_to_point connection should only contain 2 epgs", "");

            }
        }
    }
}
