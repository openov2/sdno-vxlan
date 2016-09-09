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

package org.openo.sdno.vxlan.mococlass;

import java.util.Arrays;
import java.util.List;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInterface;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanTunnel;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.EndpointGroup;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.result.ResultRsp;

import mockit.Mock;
import mockit.MockUp;

public final class MockInventoryDao<T> extends MockUp<InventoryDao<T>> {

    @Mock
    ResultRsp queryByFilter(Class clazz, String filter, String queryResultFields) throws ServiceException {

        if(NeVxlanTunnel.class.equals(clazz)) {
            NeVxlanTunnel neVxlanTunnel = new NeVxlanTunnel();
            return new ResultRsp<List<NeVxlanTunnel>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(neVxlanTunnel));
        } else if(NeVxlanInterface.class.equals(clazz)) {
            NeVxlanInterface neVxlanInterface = new NeVxlanInterface();
            return new ResultRsp<List<NeVxlanInterface>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(neVxlanInterface));
        } else if(NeVxlanInstance.class.equals(clazz)) {
            NeVxlanInstance neVxlanInstance = new NeVxlanInstance();
            neVxlanInstance.setControllerId("TestCtrlId");
            neVxlanInstance.setVni("12");
            neVxlanInstance.setConnectionServiceId("TestConnection");
            return new ResultRsp<List<NeVxlanInstance>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(neVxlanInstance));
        } else if(OverlayVpn.class.equals(clazz)) {
            OverlayVpn overlayVpn = new OverlayVpn();
            return new ResultRsp<List<OverlayVpn>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(overlayVpn));
        } else if(Connection.class.equals(clazz)) {
            Connection connection = new Connection();
            return new ResultRsp<List<Connection>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(connection));
        } else if(EndpointGroup.class.equals(clazz)) {
            EndpointGroup endpointGroup = new EndpointGroup();
            return new ResultRsp<List<EndpointGroup>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(endpointGroup));
        }

        return null;
    }

    @Mock
    ResultRsp<String> batchDelete(Class clazz, List<String> uuids) throws ServiceException {
        return new ResultRsp<String>();
    }

    @Mock
    public ResultRsp update(Class clazz, List oriUpdateList, String updateFieldListStr) {
        return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
    }

    @Mock
    public ResultRsp<T> insert(T data) throws ServiceException {
        return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
    }

    @Mock
    public ResultRsp<List<T>> batchInsert(List<T> dataList) {
        return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
    }
}
