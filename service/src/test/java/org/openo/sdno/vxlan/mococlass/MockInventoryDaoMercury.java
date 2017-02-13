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

package org.openo.sdno.vxlan.mococlass;

import java.util.Arrays;
import java.util.List;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.v2.vxlan.Ip;
import org.openo.sdno.overlayvpn.model.v2.vxlan.NbiVxlanTunnel;
import org.openo.sdno.overlayvpn.model.v2.vxlan.PortVlan;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanInstance;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanInterface;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanTunnel;
import org.openo.sdno.overlayvpn.result.ResultRsp;

import mockit.Mock;
import mockit.MockUp;

public final class MockInventoryDaoMercury<T> extends MockUp<InventoryDao<T>> {

    @Mock
    ResultRsp queryByFilter(Class clazz, String filter, String queryResultFields) throws ServiceException {

        if(SbiNeVxlanInstance.class.equals(clazz)) {
            SbiNeVxlanInstance neVxlanTunnel = new SbiNeVxlanInstance();
            return new ResultRsp<List<SbiNeVxlanInstance>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(neVxlanTunnel));
        } else if(NbiVxlanTunnel.class.equals(clazz)) {
            return new ResultRsp<List<NbiVxlanTunnel>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(getNbiModel()));
        } else if(PortVlan.class.equals(clazz)) {
            PortVlan neVxlanInterface = new PortVlan();
            return new ResultRsp<List<PortVlan>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(neVxlanInterface));
        } else if(Ip.class.equals(clazz)) {
            Ip neVxlanInterface = new Ip();
            return new ResultRsp<List<Ip>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(neVxlanInterface));
        } else if(SbiNeVxlanInterface.class.equals(clazz)) {
            SbiNeVxlanInterface neVxlanInterface = new SbiNeVxlanInterface();
            return new ResultRsp<List<SbiNeVxlanInterface>>(ErrorCode.OVERLAYVPN_SUCCESS,
                    Arrays.asList(neVxlanInterface));
        } else if(SbiNeVxlanTunnel.class.equals(clazz)) {
            SbiNeVxlanTunnel neVxlanInterface = new SbiNeVxlanTunnel();
            return new ResultRsp<List<SbiNeVxlanTunnel>>(ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(neVxlanInterface));
        }
        return null;
    }

    @Mock
    public ResultRsp query(Class clazz, String uuid, String tenantId) {
        if(NbiVxlanTunnel.class.equals(clazz)) {
            return new ResultRsp<NbiVxlanTunnel>(ErrorCode.OVERLAYVPN_SUCCESS, getNbiModel());
        }
        return null;
    }

    @Mock
    ResultRsp<String> batchDelete(Class clazz, List<String> uuids) throws ServiceException {
        return new ResultRsp<String>();
    }

    @Mock
    public ResultRsp<String> delete(Class clazz, String uuid) {
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

    private NbiVxlanTunnel getNbiModel() {
        NbiVxlanTunnel nbiTunnel = new NbiVxlanTunnel();
        nbiTunnel.setUuid("vxlanid");
        nbiTunnel.setName("vpn1");
        nbiTunnel.setDestNeId("neid2");
        nbiTunnel.setSrcNeId("neid1");
        nbiTunnel.setDestNeRole("localcpe");
        nbiTunnel.setSrcNeRole("localcpe");
        nbiTunnel.setDestPortName("Ltp2");
        nbiTunnel.setSrcPortName("Ltp1");
        nbiTunnel.setVni("188");
        nbiTunnel.setDeployStatus("undeploy");
        nbiTunnel.setPortVlanList("[{\"neId\":\"neid1\",\"vlan\":\"5-6\"},{\"neId\":\"neid2\",\"vlan\":\"7-8\"}]");
        nbiTunnel.setSrcIp(new Ip());
        nbiTunnel.setDestIp(new Ip());
        nbiTunnel.setPortVlans(Arrays.asList(new PortVlan()));
        return nbiTunnel;
    }
}
