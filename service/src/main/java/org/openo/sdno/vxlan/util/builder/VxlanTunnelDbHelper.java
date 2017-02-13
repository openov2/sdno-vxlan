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
import org.apache.commons.collections.Predicate;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.framework.container.util.UuidUtils;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.v2.vxlan.Ip;
import org.openo.sdno.overlayvpn.model.v2.vxlan.NbiVxlanTunnel;
import org.openo.sdno.overlayvpn.model.v2.vxlan.PortVlan;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanInstance;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanInterface;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanTunnel;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.vxlan.constant.DeployStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * <br>
 * 
 * @author
 * @version SDNO 0.5 Jan 12, 2017
 */
public class VxlanTunnelDbHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxlanTunnelDbHelper.class);

    public static void insertNbiVxlanTunnelList(List<NbiVxlanTunnel> vxlanTunnels) throws ServiceException {
        LOGGER.info("=====insert NbiVxlanTunnel list:" + JsonUtil.toJson(vxlanTunnels));
        for(NbiVxlanTunnel nbimodel : vxlanTunnels) {
            nbimodel.setDeployStatus(DeployStatus.UNDEPLOY.getName());
        }
        insertIp(vxlanTunnels);
        insertPortVlans(vxlanTunnels);
        new InventoryDaoUtil<NbiVxlanTunnel>().getInventoryDao().batchInsert(vxlanTunnels);
    }

    private static void insertPortVlans(List<NbiVxlanTunnel> vxlanTunnels) throws ServiceException {

        List<PortVlan> portvlans = new ArrayList<PortVlan>();
        for(NbiVxlanTunnel vxlan : vxlanTunnels) {
            portvlans.addAll(vxlan.getPortVlans());
        }
        LOGGER.info("=====insert portVlan list:" + JsonUtil.toJson(portvlans));
        new InventoryDaoUtil<PortVlan>().getInventoryDao().batchInsert(portvlans);

    }

    private static void insertIp(List<NbiVxlanTunnel> vxlanTunnels) throws ServiceException {
        List<Ip> ips = new ArrayList<Ip>();
        for(NbiVxlanTunnel vxlan : vxlanTunnels) {
            ips.add(vxlan.getDestIp());
            ips.add(vxlan.getSrcIp());
        }
        LOGGER.info("=====insert Ip list:" + JsonUtil.toJson(ips));
        new InventoryDaoUtil<Ip>().getInventoryDao().batchInsert(ips);

    }

    /**
     * <br>
     * 
     * @param sbiVxlans
     * @throws ServiceException
     * @since SDNO 0.5
     */
    public static void insertSbiVxlanInstanceList(List<SbiNeVxlanInstance> sbiVxlans) throws ServiceException {
        LOGGER.info("insert SbiNeVxlanInstance list:" + JsonUtil.toJson(sbiVxlans));
        for(SbiNeVxlanInstance sbiVxlan : sbiVxlans) {
            sbiVxlan.setExternalId(UuidUtils.createUuid());
        }
        new InventoryDaoUtil<SbiNeVxlanInstance>().getInventoryDao().batchInsert(sbiVxlans);
        for(SbiNeVxlanInstance vxlanIns : sbiVxlans) {
            new InventoryDaoUtil<SbiNeVxlanInterface>().getInventoryDao().batchInsert(vxlanIns.getVxlanInterfaceList());
        }
        for(SbiNeVxlanInstance vxlanIns : sbiVxlans) {
            new InventoryDaoUtil<SbiNeVxlanTunnel>().getInventoryDao().batchInsert(vxlanIns.getVxlanTunnelList());
        }
        LOGGER.info("=====finish insert SBI models=====");
    }

    /**
     * <br>
     * 
     * @return
     * @throws ServiceException
     * @since SDNO 0.5
     */
    public static List<SbiNeVxlanInstance> getSbiVxlansByNbiModelId(List<String> deploy) throws ServiceException {
        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("nbiVxlanTunnelId", deploy);
        LOGGER.info("=====get sbi model by nbi uuid:" + deploy.get(0));
        String filter = JSONObject.fromObject(filterMap).toString();
        List<SbiNeVxlanInstance> sbiModels = new InventoryDaoUtil<SbiNeVxlanInstance>().getInventoryDao()
                .queryByFilter(SbiNeVxlanInstance.class, filter, null).getData();
        for(SbiNeVxlanInstance sbiModel : sbiModels) {
            fillComplexSbiModel(sbiModel);
        }
        return sbiModels;
    }

    /**
     * <br>
     * 
     * @param deploy
     * @return
     * @throws ServiceException
     * @since SDNO 0.5
     */
    public static List<NbiVxlanTunnel> getNbiVxlanById(List<String> uuids) throws ServiceException {
        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("uuid", uuids);
        String filter = JSONObject.fromObject(filterMap).toString();
        return new InventoryDaoUtil<NbiVxlanTunnel>().getInventoryDao()
                .queryByFilter(NbiVxlanTunnel.class, filter, null).getData();
    }

    /**
     * <br>
     * 
     * @param vxlanTunnelId
     * @return
     * @throws ServiceException
     * @since SDNO 0.5
     */
    public static NbiVxlanTunnel getComplexNbiVxlanById(String vxlanTunnelId) throws ServiceException {
        ResultRsp<NbiVxlanTunnel> result = new InventoryDaoUtil<NbiVxlanTunnel>().getInventoryDao()
                .query(NbiVxlanTunnel.class, vxlanTunnelId, null);
        if(!result.isValid()) {
            LOGGER.error("get nbi complex model failed.");
            return null;
        }
        NbiVxlanTunnel sbiVxlan = result.getData();
        fillePortVxlan(Arrays.asList(sbiVxlan));
        return sbiVxlan;

    }

    /**
     * <br>
     * 
     * @param asList
     * @throws ServiceException
     * @since SDNO 0.5
     */
    private static void fillePortVxlan(List<NbiVxlanTunnel> sbiVxlans) throws ServiceException {
        List<String> uuids = new ArrayList<String>();
        for(NbiVxlanTunnel sbiVxlan : sbiVxlans) {
            uuids.add(sbiVxlan.getUuid());
        }
        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("vxlanTunnelId", uuids);
        String filter = JSONObject.fromObject(filterMap).toString();
        List<PortVlan> portVlans =
                new InventoryDaoUtil<PortVlan>().getInventoryDao().batchQuery(PortVlan.class, filter).getData();
        LOGGER.info("query portvlans for nbi model:" + JsonUtil.toJson(portVlans));
        List<Ip> ips = new InventoryDaoUtil<Ip>().getInventoryDao().batchQuery(Ip.class, filter).getData();
        LOGGER.info("query ips for nbi model::" + JsonUtil.toJson(ips));
        fillPortVlanToTunnel(sbiVxlans, portVlans);
        fillIpToTunnel(sbiVxlans, ips);

    }

    /**
     * <br>
     * 
     * @param sbiVxlans
     * @param ips
     * @since SDNO 0.5
     */
    private static void fillIpToTunnel(List<NbiVxlanTunnel> sbiVxlans, List<Ip> ips) {
        for(Ip ip : ips) {
            for(NbiVxlanTunnel sbiVxlan : sbiVxlans) {

                if(!sbiVxlan.getUuid().equals(ip.getVxlanTunnelId())) {
                    continue;
                }

                String ipNeId = ip.getNeId();

                if(sbiVxlan.getSrcNeId().equals(ipNeId)) {
                    sbiVxlan.setSrcIp(ip);
                }

                if(sbiVxlan.getDestNeId().equals(ipNeId)) {
                    sbiVxlan.setDestIp(ip);
                }
            }
        }

    }

    /**
     * <br>
     * 
     * @param sbiVxlans
     * @param portVlans
     * @since SDNO 0.5
     */
    @SuppressWarnings("unchecked")
    private static void fillPortVlanToTunnel(List<NbiVxlanTunnel> sbiVxlans, List<PortVlan> portVlans) {
        for(NbiVxlanTunnel sbiVxlan : sbiVxlans) {
            final String tunnelId = sbiVxlan.getUuid();

            Collection<PortVlan> getPortVxlanByTunnelId = CollectionUtils.select(portVlans, new Predicate() {

                @Override
                public boolean evaluate(Object arg0) {
                    return tunnelId.equals(((PortVlan)arg0).getVxlanTunnelId());
                }
            });
            sbiVxlan.setPortVlans(new ArrayList<PortVlan>(getPortVxlanByTunnelId));
            sbiVxlan.setPortVlanList(JsonUtil.toJson(sbiVxlan.getPortVlans()));

        }

    }

    /**
     * <br>
     * 
     * @param sbiModels
     * @throws ServiceException
     * @since SDNO 0.5
     */
    public static void fillComplexSbiModel(SbiNeVxlanInstance sbiModel) throws ServiceException {

        List<String> uuids = new ArrayList<String>();
        uuids.add(sbiModel.getUuid());
        Map<String, Object> filterMap = new HashMap<String, Object>();
        filterMap.put("vxlanInstanceId", uuids);
        String filter = JSONObject.fromObject(filterMap).toString();
        List<SbiNeVxlanInterface> interfaces = new InventoryDaoUtil<SbiNeVxlanInterface>().getInventoryDao()
                .batchQuery(SbiNeVxlanInterface.class, filter).getData();
        if(interfaces != null) {
            sbiModel.setVxlanInterfaceList(interfaces);
        }
        List<SbiNeVxlanTunnel> tunnels = new InventoryDaoUtil<SbiNeVxlanTunnel>().getInventoryDao()
                .batchQuery(SbiNeVxlanTunnel.class, filter).getData();
        if(tunnels != null) {

            sbiModel.setVxlanTunnelList(tunnels);
        }
    }

    /**
     * delete nbi and sbi model.<br>
     * 
     * @param nbiModel sbi model to delete.
     * @param sbiModels nbi model to delete.
     * @throws ServiceException
     * @since SDNO 0.5
     */
    public static void deleteNbiAndSbi(NbiVxlanTunnel nbiModel, List<SbiNeVxlanInstance> sbiModels)
            throws ServiceException {
        LOGGER.info("=====Deleting SBI Models=====");
        for(SbiNeVxlanInstance sbivxlan : sbiModels) {
            deleteComplexSbiModel(sbivxlan);
        }

        LOGGER.info("=====Deleting NBI Models=====");
        deleteComplexNbiModel(nbiModel);
    }

    /**
     * <br>
     * 
     * @param nbiModel
     * @throws ServiceException
     * @since SDNO 0.5
     */
    private static void deleteComplexNbiModel(NbiVxlanTunnel nbiModel) throws ServiceException {

        List<PortVlan> portvlans = nbiModel.getPortVlans();
        List<Ip> ips = new ArrayList<Ip>();
        ips.add(nbiModel.getDestIp());
        ips.add(nbiModel.getSrcIp());
        deleteIps(ips);
        deletPortVlans(portvlans);
        new InventoryDaoUtil<NbiVxlanTunnel>().getInventoryDao().delete(NbiVxlanTunnel.class, nbiModel.getUuid());

    }

    private static void deleteIps(List<Ip> ips) throws ServiceException {
        List<String> uuids = new ArrayList<String>();
        for(Ip ip : ips) {
            uuids.add(ip.getUuid());
        }
        LOGGER.info("=====start delete ip=====:");
        LOGGER.info("=====uuids:" + JsonUtil.toJson(uuids));
        new InventoryDaoUtil<Ip>().getInventoryDao().batchDelete(Ip.class, uuids);
    }

    /**
     * <br>
     * 
     * @param portvlans
     * @throws ServiceException
     * @since SDNO 0.5
     */
    private static void deletPortVlans(List<PortVlan> portvlans) throws ServiceException {

        LOGGER.info("=====start delete port=====");
        List<String> uuids = new ArrayList<String>();
        for(PortVlan portvlan : portvlans) {
            uuids.add(portvlan.getUuid());
        }
        LOGGER.info("=====uuids:" + JsonUtil.toJson(uuids));
        new InventoryDaoUtil<PortVlan>().getInventoryDao().batchDelete(PortVlan.class, uuids);

    }

    /**
     * <br>
     * 
     * @param sbiNeVxlanInstance
     * @throws ServiceException
     * @since SDNO 0.5
     */
    private static void deleteComplexSbiModel(SbiNeVxlanInstance sbiNeVxlanInstance) throws ServiceException {

        LOGGER.info("=====Deleting SBI tunnels=====");
        deleteTunnels(sbiNeVxlanInstance.getVxlanTunnelList());

        LOGGER.info("=====Deleting SBI interfaces=====");
        deleteInterfaces(sbiNeVxlanInstance.getVxlanInterfaceList());

        LOGGER.info("=====Deleting SBI instances=====");
        new InventoryDaoUtil<SbiNeVxlanInstance>().getInventoryDao().delete(SbiNeVxlanInstance.class,
                sbiNeVxlanInstance.getUuid());

    }

    /**
     * <br>
     * 
     * @param sbiInterfaces
     * @throws ServiceException
     * @since SDNO 0.5
     */
    private static void deleteInterfaces(List<SbiNeVxlanInterface> sbiInterfaces) throws ServiceException {

        List<String> uuids = new ArrayList<String>();
        for(SbiNeVxlanInterface sbiinterface : sbiInterfaces) {
            uuids.add(sbiinterface.getUuid());
        }
        new InventoryDaoUtil<SbiNeVxlanInterface>().getInventoryDao().batchDelete(SbiNeVxlanInterface.class, uuids);

    }

    /**
     * <br>
     * 
     * @param sbiTunnels
     * @throws ServiceException
     * @since SDNO 0.5
     */
    private static void deleteTunnels(List<SbiNeVxlanTunnel> sbiTunnels) throws ServiceException {

        List<String> neTunnelIds = new ArrayList<String>();
        for(SbiNeVxlanTunnel tunnel : sbiTunnels) {
            neTunnelIds.add(tunnel.getUuid());
        }
        LOGGER.info("=====Updating SbiNeVxlanTunnel status=====");
        new InventoryDaoUtil<SbiNeVxlanTunnel>().getInventoryDao().batchDelete(SbiNeVxlanTunnel.class, neTunnelIds);
    }

    public static void updateDeployStatus(List<NbiVxlanTunnel> vxlanTunnels, List<SbiNeVxlanInstance> sbiVxlans,
            boolean isDeployed) throws ServiceException {
        String newDeployStatus = isDeployed ? DeployStatus.DEPLOY.getName() : DeployStatus.UNDEPLOY.getName();
        LOGGER.info("=====Updating deploy status=====");
        for(NbiVxlanTunnel nbimodel : vxlanTunnels) {
            nbimodel.setDeployStatus(newDeployStatus);
        }
        new InventoryDaoUtil<NbiVxlanTunnel>().getInventoryDao().update(NbiVxlanTunnel.class, vxlanTunnels,
                "deployStatus");

        List<SbiNeVxlanInterface> interfaceList = new ArrayList<SbiNeVxlanInterface>();
        List<SbiNeVxlanTunnel> tunnelList = new ArrayList<SbiNeVxlanTunnel>();

        for(SbiNeVxlanInstance sbivxlan : sbiVxlans) {
            sbivxlan.setDeployStatus(newDeployStatus);

            for(SbiNeVxlanInterface vxlanIf : sbivxlan.getVxlanInterfaceList()) {
                vxlanIf.setDeployStatus(newDeployStatus);
                interfaceList.add(vxlanIf);
            }
            for(SbiNeVxlanTunnel vxlantunnel : sbivxlan.getVxlanTunnelList()) {
                vxlantunnel.setDeployStatus(newDeployStatus);
                tunnelList.add(vxlantunnel);
            }
        }
        LOGGER.info("=====Updating SbiNeVxlanInstance deploy status=====");
        new InventoryDaoUtil<SbiNeVxlanInstance>().getInventoryDao().update(SbiNeVxlanInstance.class, sbiVxlans,
                "deployStatus");
        LOGGER.info("=====Updating SbiNeVxlanInterface deploy status=====");
        new InventoryDaoUtil<SbiNeVxlanInterface>().getInventoryDao().update(SbiNeVxlanInterface.class, interfaceList,
                "deployStatus");
        LOGGER.info("=====Updating SbiNeVxlanTunnel deploy status=====");
        new InventoryDaoUtil<SbiNeVxlanTunnel>().getInventoryDao().update(SbiNeVxlanTunnel.class, tunnelList,
                "deployStatus");

    }
}
