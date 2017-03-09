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

package org.openo.sdno.vxlan.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.ParameterServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.model.v2.vxlan.ActionModel;
import org.openo.sdno.overlayvpn.model.v2.vxlan.Ip;
import org.openo.sdno.overlayvpn.model.v2.vxlan.NbiVxlanTunnel;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanInstance;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanInterface;
import org.openo.sdno.overlayvpn.model.v2.vxlan.SbiNeVxlanTunnel;
import org.openo.sdno.vxlan.constant.DeployStatus;
import org.openo.sdno.vxlan.util.builder.CreateVxlanHelper;
import org.openo.sdno.vxlan.util.builder.DataValidator;
import org.openo.sdno.vxlan.util.builder.DeployService;
import org.openo.sdno.vxlan.util.builder.NeControllerUtil;
import org.openo.sdno.vxlan.util.builder.PortUtil;
import org.openo.sdno.vxlan.util.builder.VxlanTunnelDbHelper;
import org.openo.sdno.vxlanservice.adapter.CallSbiApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Restful Interface for VxLAN service (create, delete, get).<br>
 *
 * @author
 * @version SDNO 0.5 03-June-2016
 */
@Service
@Path("/sdnovxlan/v1")
public class VxlanSvcResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxlanSvcResource.class);

    /**
     * health check interface.<br>
     * 
     * @param resp http servlet response object.
     * @throws ServiceException if inner error happens.
     * @since SDNO 0.5
     */
    @GET
    @Path("/healthcheck")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void healthCheck(@Context HttpServletResponse resp) throws ServiceException {
        resp.setStatus(HttpStatus.SC_OK);
    }

    /**
     * query vxlan nbi basic model by uuid.<br>
     * 
     * @param vxlanTunnelId uuid of the vxlan to query.
     * @return NbiVxlanTunnel model of the given uuid.
     * @throws ServiceException if inner error happens. if inner error happens.
     * @since SDNO 0.5
     */
    @GET
    @Path("/vxlans/{vxlanTunnelId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public NbiVxlanTunnel query(@PathParam("vxlanTunnelId") String vxlanTunnelId) throws ServiceException {
        NbiVxlanTunnel vxLanTunnel = VxlanTunnelDbHelper.getComplexNbiVxlanById(vxlanTunnelId);
        if(null == vxLanTunnel) {
            LOGGER.error("This Vxlan tunnel does not exist");
            throw new ParameterServiceException("This Vxlan tunnel does not exist");
        }
        return vxLanTunnel;
    }

    /**
     * batch query vxlan tunnel by list of uuid.<br>
     * 
     * @param vxlanTunnelIds list of uuids of the vxlan to query.
     * @return list of NbiVxlanTunnel of the given uuids.
     * @throws ServiceException if inner error happens.
     * @since SDNO 0.5
     */
    @POST
    @Path("/vxlans/batch-query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<NbiVxlanTunnel> batchquery(List<String> vxlanTunnelIds) throws ServiceException {
        return VxlanTunnelDbHelper.getNbiVxlanById(vxlanTunnelIds);
    }

    /**
     * batch create vxlans.<br>
     * 
     * @param vxlanTunnels vxlantunnel models to create
     * @param resp HttpServletResponse object
     * @return vxlantunnel models created.
     * @throws ServiceException if inner error happens.
     * @since SDNO 0.5
     */
    @POST
    @Path("/vxlans")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<NbiVxlanTunnel> create(@Context HttpServletResponse resp, List<NbiVxlanTunnel> vxlanTunnels)
            throws ServiceException {
        long beginTime = System.currentTimeMillis();
        LOGGER.info("Enter Create vxlan begin time = " + beginTime);
        // allocate uuid
        for(NbiVxlanTunnel tunnel : vxlanTunnels) {
            tunnel.allocateUuid();
        }
        Map<String, NetworkElementMO> neIdToNeMap = DataValidator.checkInputNe(vxlanTunnels);
        LOGGER.info("neIdToNeMap: " + JsonUtil.toJson(neIdToNeMap));
        DataValidator.checkVlanPortResource(vxlanTunnels);
        Map<NetworkElementMO, ControllerMO> neToCtrlMap = NeControllerUtil.getNeCtrlMap(neIdToNeMap);
        LOGGER.info("neToCtrlMap:" + JsonUtil.toJson(neToCtrlMap));
        Map<String, Ip> neIdToIpMap = PortUtil.buildPortIpMap(vxlanTunnels);
        CreateVxlanHelper.fillIpBackToTunnel(neIdToIpMap, vxlanTunnels);

        VxlanTunnelDbHelper.insertNbiVxlanTunnelList(vxlanTunnels);

        List<SbiNeVxlanInstance> sbiVxlans = CreateVxlanHelper.nbiToSbi(vxlanTunnels);
        for(SbiNeVxlanInstance sbivxlan : sbiVxlans) {
            sbivxlan.setDeployStatus(DeployStatus.UNDEPLOY.getName());
            ControllerMO controller = NeControllerUtil.findCtrlByNeId(neToCtrlMap, sbivxlan.getDeviceId());
            if(null == controller) {
                LOGGER.error("NE id =" + sbivxlan.getDeviceId() + "no controller.");
                throw new ParameterServiceException("find ne without controller.");
            }
            String ctrlId = controller.getId();
            sbivxlan.setControllerId(ctrlId);
            for(SbiNeVxlanInterface vxlanIf : sbivxlan.getVxlanInterfaceList()) {
                vxlanIf.setControllerId(ctrlId);
                vxlanIf.setDeployStatus(DeployStatus.UNDEPLOY.getName());
            }
            for(SbiNeVxlanTunnel vxlantunnel : sbivxlan.getVxlanTunnelList()) {
                vxlantunnel.setControllerId(ctrlId);
                vxlantunnel.setDeployStatus(DeployStatus.UNDEPLOY.getName());
            }
        }

        VxlanTunnelDbHelper.insertSbiVxlanInstanceList(sbiVxlans);

        for(SbiNeVxlanInstance sbivxlan : sbiVxlans) {
            CreateVxlanHelper.replaceNeIdWithDeviceId(sbivxlan, neToCtrlMap.keySet());
        }
        LOGGER.info("=====call adapter api=====");
        CallSbiApi.create(sbiVxlans).getData();
        boolean isDeployed = true;
        VxlanTunnelDbHelper.updateDeployStatus(vxlanTunnels, sbiVxlans, isDeployed);

        resp.setStatus(HttpCode.CREATE_OK);

        return vxlanTunnels;
    }

    /**
     * delete vxlan tunnel by uuid.<br>
     * 
     * @param vxlanTunnelId uuid of vxlantunnel to delete.
     * @return the uuid of deleted vxlan model.
     * @throws ServiceException if inner error happens.
     * @since SDNO 0.5
     */
    @DELETE
    @Path("/vxlans/{vxlanTunnelId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@PathParam("vxlanTunnelId") String vxlanTunnelId) throws ServiceException {
        long beginTime = System.currentTimeMillis();
        LOGGER.info("Enter delete vxlan begin time = " + beginTime);
        LOGGER.info("Enter delete vxlan uuid = " + vxlanTunnelId);
        NbiVxlanTunnel nbiModel = VxlanTunnelDbHelper.getComplexNbiVxlanById(vxlanTunnelId);
        if(nbiModel == null) {
            LOGGER.error("Enter delete vxlan does not exist.");
            throw new ParameterServiceException("can not get the vxlan model with the given uuid.");
        }

        if(!nbiModel.getDeployStatus().equals(DeployStatus.UNDEPLOY.getName())) {
            LOGGER.error("can not delete deployed vxlan, must undeploy first.");
            throw new ParameterServiceException("can not delete deployed vxlan, must undeploy first.");
        }
        List<String> uuids = new ArrayList<>();
        uuids.add(vxlanTunnelId);
        List<SbiNeVxlanInstance> sbiModels = VxlanTunnelDbHelper.getSbiVxlansByNbiModelId(uuids);
        if(sbiModels.isEmpty()) {
            LOGGER.error("can not find corresponding sbi model of the input nbi id.");
            return vxlanTunnelId;
        }

        VxlanTunnelDbHelper.deleteNbiAndSbi(nbiModel, sbiModels);
        long costTime = System.currentTimeMillis() - beginTime;
        LOGGER.info("finish delete vxlan uuid: = " + vxlanTunnelId + ",cost time = " + costTime);

        return vxlanTunnelId;

    }

    /**
     * deploy/undeploy interface of vxlan.<br>
     * 
     * @param actionModel action model, contain deploy/undeploy uuid list.
     * @return uuid list of the success deploy/undeploy models.
     * @throws ServiceException if inner error happens.
     * @since SDNO 0.5
     */
    @POST
    @Path("/vxlans/action")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> action(ActionModel actionModel) throws ServiceException {
        if(actionModel == null) {
            LOGGER.error("action model is empty.");
            throw new ParameterServiceException("action model is empty.");
        }

        if(CollectionUtils.isNotEmpty(actionModel.getDeploy())) {
            return deploy(actionModel.getDeploy());
        } else if(CollectionUtils.isNotEmpty(actionModel.getUndeploy())) {
            return undeploy(actionModel.getUndeploy());
        } else {
            LOGGER.error("adeploy and undeploy list are both empty.");
            throw new ParameterServiceException("adeploy and undeploy list are both empty.");
        }
    }

    private List<String> undeploy(List<String> undeploy) throws ServiceException {
        return DeployService.undeploy(undeploy);
    }

    private List<String> deploy(List<String> deploy) throws ServiceException {
        return DeployService.deploy(deploy);
    }
}
