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

package org.openo.sdno.vxlan.rest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.brs.model.ControllerMO;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.common.enums.TechnologyType;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVtep;
import org.openo.sdno.overlayvpn.model.port.WanSubInterface;
import org.openo.sdno.overlayvpn.model.servicemodel.Connection;
import org.openo.sdno.overlayvpn.model.servicemodel.OverlayVpn;
import org.openo.sdno.overlayvpn.model.tunnel.Tunnel;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.check.CheckStrUtil;
import org.openo.sdno.vxlan.service.inf.VxlanService;
import org.openo.sdno.vxlan.util.NeInterfaceUtil;
import org.openo.sdno.vxlan.util.WanSubInterfaceUtil;
import org.openo.sdno.vxlan.util.check.CheckOverlayVpnUtil;
import org.openo.sdno.vxlan.util.exception.ThrowVxlanExcpt;
import org.openo.sdno.vxlan.util.vxlanbuilder.ModelConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Restful Interface for VxLAN service (create, delete, get).<br>
 * 
 * @author
 * @version SDNO 0.5 03-June-2016
 */
@Service
@Path("/sdnovxlan/v1/vxlans")
public class VxlanSvcRoaResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxlanSvcRoaResource.class);

    @Resource
    private VxlanService vxlanService;

    public VxlanService getVxlanService() {
        return vxlanService;
    }

    public void setVxlanService(VxlanService vxlanService) {
        this.vxlanService = vxlanService;
    }

    /**
     * Query VxLan Tunnel information from database.<br>
     * 
     * @param request - HttpServletRequest Object
     * @param response - HttpServletResponse Object
     * @param connectionUuid - Connection UUID
     * @return - List of Tunnel Information consisting of source and destination information
     * @throws ServiceException - when query from database throws exception
     * @since SDNO 0.5
     */
    @GET
    @Path("/{connectionid}/vxlantunnels")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResultRsp<List<Tunnel>> queryTunnel(@Context HttpServletRequest request,
            @Context HttpServletResponse response, @PathParam("connectionid") String connectionUuid)
            throws ServiceException {
        long beginTime = System.currentTimeMillis();
        LOGGER.info("Query Vxlan tunnel begin.");

        // Validate UUID
        CheckStrUtil.checkUuidStr(connectionUuid);

        // Query Tunnel information from database
        ResultRsp<List<Tunnel>> queryResult = vxlanService.queryTunnel(connectionUuid, null);

        LOGGER.info("Query Vxlan tunnel end time = " + (System.currentTimeMillis() - beginTime));

        // Check result and throw exception if any error
        ThrowVxlanExcpt.checkRspThrowException(queryResult);
        return queryResult;
    }

    /**
     * Create VxLAN for the OverlayVpn.<br>
     * 
     * @param request - HttpServletRequest Object
     * @param response - HttpServletResponse Object
     * @param overlayVpn -OverlayVpn information with End point and Connection information
     * @return Create result with OverlayVPN Information
     * @throws ServiceException - when controller interface fails or input is invalid
     * @since SDNO 0.5
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResultRsp<OverlayVpn> create(@Context HttpServletRequest request, @Context HttpServletResponse response,
            OverlayVpn overlayVpn) throws ServiceException {
        long beginTime = System.currentTimeMillis();
        LOGGER.info("Enter Create vxlan begin time = " + beginTime);

        String tenantId = overlayVpn.getTenantId();

        if(!StringUtils.hasLength(tenantId)) {
            ThrowVxlanExcpt.throwTenantIdMissing(tenantId);
        }

        // Filter VxLAN Connections
        List<Connection> conectionList = overlayVpn.getVpnConnections();
        for(Connection curConnection : conectionList) {
            if(!TechnologyType.isVxlanRelated(curConnection.getTechnology())) {
                conectionList.remove(curConnection);
            }
        }

        // Validate the Input and get mapping from NEID and NE Information
        Map<String, NetworkElementMO> deviceIdToNeMap = new ConcurrentHashMap<String, NetworkElementMO>();
        CheckOverlayVpnUtil.check(overlayVpn, tenantId, deviceIdToNeMap);

        // Query Vtep information from controller
        Map<String, ControllerMO> deviceIdToCtrlMap = new ConcurrentHashMap<String, ControllerMO>();
        ResultRsp<Map<String, NeVtep>> queryDeviceIdToNeVtepRsp =
                NeInterfaceUtil.queryVtepForVxlan(deviceIdToNeMap, deviceIdToCtrlMap);
        if(!queryDeviceIdToNeVtepRsp.isSuccess()) {
            ThrowVxlanExcpt.checkRspThrowException(queryDeviceIdToNeVtepRsp);
        }

        Map<String, NeVtep> deviceIdToNeVtepMap = queryDeviceIdToNeVtepRsp.getData();

        // Query WAN interface information
        ResultRsp<Map<String, WanSubInterface>> queryDeviceIdToWansubInfRsp =
                WanSubInterfaceUtil.queryNeWanInfForVxlan(deviceIdToNeMap, deviceIdToCtrlMap);
        if(!queryDeviceIdToWansubInfRsp.isSuccess()) {
            ThrowVxlanExcpt.checkRspThrowException(queryDeviceIdToWansubInfRsp);
        }
        Map<String, WanSubInterface> deviceIdToWansubInfMap = queryDeviceIdToWansubInfRsp.getData();

        // Allocate VNI for Connection
        Map<String, String> connectionIdToVniMap = ModelConvertUtil.allocVniForConn(overlayVpn.getVpnConnections());

        ResultRsp<OverlayVpn> createResult = vxlanService.create(overlayVpn, tenantId, deviceIdToNeVtepMap,
                deviceIdToWansubInfMap, connectionIdToVniMap, deviceIdToCtrlMap);

        LOGGER.info("Create Vxlan end time = " + (System.currentTimeMillis() - beginTime));

        // Check for any error and throw exception
        ThrowVxlanExcpt.checkRspThrowException(createResult);

        response.setStatus(HttpCode.CREATE_OK);

        return createResult;
    }

    /**
     * Delete VxLan information from database <br>
     * 
     * @param request - HttpServletRequest Object
     * @param response - HttpServletResponse Object
     * @param connectionUuid - Connection UUID
     * @return - Operation result with connection information
     * @throws ServiceException - when input is invalid or database query returns failure
     * @since SDNO 0.5
     */
    @DELETE
    @Path("/{connectionid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResultRsp<String> delete(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @PathParam("connectionid") String connectionUuid) throws ServiceException {
        long infterEnterTime = System.currentTimeMillis();
        LOGGER.info("Enter delete method. begin time = " + infterEnterTime);

        // Validate UUID
        CheckStrUtil.checkUuidStr(connectionUuid);

        // UnDeploy VxLan Instance
        ResultRsp<String> unDeployResult = vxlanService.undeploy(connectionUuid, null);
        if(!unDeployResult.isSuccess()) {
            LOGGER.error("Undeploy VxLan failed");
            return new ResultRsp<String>(ErrorCode.OVERLAYVPN_FAILED);
        }

        // Delete VxLan from Database
        ResultRsp<Connection> resultRsp = vxlanService.delete(connectionUuid, null);
        LOGGER.info("Exit delete method. Cost time = " + (System.currentTimeMillis() - infterEnterTime));

        // Check for any error and throw exception if any
        ThrowVxlanExcpt.checkRspThrowException(resultRsp);
        return new ResultRsp<String>(ErrorCode.OVERLAYVPN_SUCCESS);
    }
}
