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

package org.openo.sdno.vxlan.util.exception;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.result.SvcExcptUtil;
import org.openo.sdno.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Utility function to handle all VxLAN service exceptions.<br>
 * 
 * @author
 * @version SDNO 0.5 03-June-2016
 */
public class ThrowVxlanExcpt {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThrowVxlanExcpt.class);

    private ThrowVxlanExcpt() {
    }

    /**
     * Check Response and throw this exception if error <br>
     * 
     * @param result - result data
     * @throws ServiceException - when the result is failure
     * @since SDNO 0.5
     */
    public static void checkRspThrowException(ResultRsp<?> result) throws ServiceException {
        if(result == null) {
            LOGGER.error("operation failed! ErrorCode = " + ErrorCode.OVERLAYVPN_FAILED);
            throw new ServiceException(ErrorCode.OVERLAYVPN_FAILED, HttpCode.ERR_FAILED);
        }
        if(!result.isSuccess()) {
            SvcExcptUtil.throwSvcExptionByResultRsp(result);
        }
    }

    /**
     * Resource do not exist, throw exception<br>
     * 
     * @param resName - Resource name
     * @param resDesc - resource exception
     * @throws ServiceException - when resource do not exist
     * @since SDNO 0.5
     */
    public static void throwResNotExistAsBadReq(String resName, String resDesc) throws ServiceException {
        String packName;
        if(!StringUtils.hasLength(resName)) {
            packName = "[Name]";
        } else {
            packName = "[" + resName + "]";
        }

        LOGGER.error("resource not exist, " + packName + resDesc);
        String desc = ResourceUtil.getMessage("resource.not_exist.desc");
        String message = packName + resDesc + " " + ResourceUtil.getMessage("resource.not_exist.reason");
        String advice = ResourceUtil.getMessage("resource.not_exist.advice");
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_RESOURCE_NOT_EXIST, desc, message, message,
                advice);
    }

    /**
     * Throw this exception when tenant ID is invalid<br>
     * 
     * @param exptTenantId - Expected tenantID
     * @param realTenantId - real tenant ID
     * @throws ServiceException - when tenant is invalid
     * @since SDNO 0.5
     */
    public static void throwTenantIdInvalid(String exptTenantId, String realTenantId) throws ServiceException {
        if(exptTenantId.equals(realTenantId)) {
            return;
        }

        LOGGER.error("tenantIds are not the same, expt = " + exptTenantId + ", real = " + realTenantId);
        String desc = ResourceUtil.getMessage("tenantid.invalid.desc");
        String message = ResourceUtil.getMessage("tenantid.invalid.reason");
        String advice = ResourceUtil.getMessage("tenantid.invalid.advice");
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_TENANT_INVALID, desc, message, message, advice);
    }

    /**
     * Throw when parameter is invalid<br>
     * 
     * @param paraName - parameter name
     * @param parmaterPath - parameter path
     * @throws ServiceException - when parameter is invalid
     * @since SDNO 0.5
     */
    public static void throwParmaterInvalid(String paraName, String parmaterPath) throws ServiceException {
        String packName;
        if(!StringUtils.hasLength(paraName)) {
            packName = "[Name]";
        } else {
            packName = "[" + paraName + "]";
        }

        String desc = ResourceUtil.getMessage("parameter.invalid.desc");
        String message = packName + parmaterPath + " " + ResourceUtil.getMessage("parameter.invalid.reason");
        String advice = ResourceUtil.getMessage("parameter.invalid.advice");
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_PARAMETER_INVALID, desc, message, message,
                advice);
    }

    /**
     * Throw when VXLAN mapping policy has IPsec policy<br>
     * 
     * @param mappingPolicyId - mapping policy ID
     * @throws ServiceException- when VXLAN mapping policy has IPsec policy
     * @since SDNO 0.5
     */
    public static void throwHavingIpsecAsParmaterInvalid(String mappingPolicyId) throws ServiceException {
        String desc = ResourceUtil.getMessage("vxlan.mappingpolicy.having.desc");
        String message = ResourceUtil.getMessage("vxlan.mappingpolicy.having.reason").replace("{mappingpolicyid}",
                mappingPolicyId);
        String advice = ResourceUtil.getMessage("vxlan.mappingpolicy.having.advice");
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_PARAMETER_INVALID, desc, message, message,
                advice);
    }

    /**
     * Mapping policy is IPSec but IPSec policy do not exist<br>
     * 
     * @param mappingPolicyId - mapping policy ID
     * @throws ServiceException - when policy is IPsec, but do not contain IPsec policy
     * @since SDNO 0.5
     */
    public static void throwNotHavingIpsecAsParmaterInvalid(String mappingPolicyId) throws ServiceException {
        String desc = ResourceUtil.getMessage("vxlan.mappingpolicy.nohaving.desc");
        String message = ResourceUtil.getMessage("vxlan.mappingpolicy.nohaving.reason").replace("{mappingpolicyid}",
                mappingPolicyId);
        String advice = ResourceUtil.getMessage("vxlan.mappingpolicy.nohaving.advice");
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_PARAMETER_INVALID, desc, message, message,
                advice);
    }

    /**
     * Exception thrown when tenant ID is missing<br>
     * 
     * @param exptTenantId - expected tenant ID
     * @throws ServiceException - when tenant Id is invalid
     * @since SDNO 0.5
     */
    public static void throwTenantIdMissing(String exptTenantId) throws ServiceException {

        LOGGER.error("tenantIds data missing in operation, expt = " + exptTenantId);
        String desc = ResourceUtil.getMessage("tenantid.data.missing.desc");
        String message = ResourceUtil.getMessage("tenantid.data.missing.reason");
        String advice = ResourceUtil.getMessage("tenantid.data.missing.advice");
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_TENANT_INVALID, desc, message, message, advice);
    }
}
