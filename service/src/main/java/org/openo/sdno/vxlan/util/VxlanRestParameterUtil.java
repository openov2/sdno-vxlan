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

package org.openo.sdno.vxlan.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.consts.CommConst;
import org.openo.sdno.overlayvpn.model.netmodel.vxlan.NeVxlanInstance;
import org.openo.sdno.overlayvpn.security.authentication.HttpContext;
import org.openo.sdno.overlayvpn.security.authentication.TokenDataHolder;

/**
 * Utility functions to fill rest parameters for different requests.<br>
 * 
 * @author
 * @version SDNO 0.5 03-Jun-2016
 */
public class VxlanRestParameterUtil {

    private VxlanRestParameterUtil() {

    }

    /**
     * Query VTEP parameter <br>
     * 
     * @param ctrlUuid Controller UUID
     * @return VTEP parameter in restful structure
     * @since SDNO 0.5
     */
    public static RestfulParametes getQueryVtepParam(String ctrlUuid) {
        RestfulParametes restfulParametes = new RestfulParametes();
        restfulParametes.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        restfulParametes.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlUuid);
        return restfulParametes;
    }

    /**
     * Get create VxLAN Instance parameter<br>
     * 
     * @param vxlanNeInstanceList List of NeVxlanInstance need to create
     * @param ctrlUuid Controller UUID
     * @return Restful structure with delete parameters filled
     * @throws ServiceException when operate failed
     * @since SDNO 0.5
     */
    public static RestfulParametes getCreateVxlanInstanceParam(List<NeVxlanInstance> vxlanNeInstanceList,
            String ctrlUuid) throws ServiceException {
        RestfulParametes restParametes = new RestfulParametes();
        String strJsonReq = JsonUtil.toJson(vxlanNeInstanceList);
        restParametes.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        restParametes.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlUuid);
        restParametes.setRawData(strJsonReq);
        return restParametes;
    }

    /**
     * Fill Delete Instance parameter in rest structure<br>
     * 
     * @param vxlanInstanceList List of NeVxlanInstance need to delete
     * @param ctrlUuid Controller UUID
     * @return Restful structure with delete parameters filled
     * @throws ServiceException - when operate failed
     * @since SDNO 0.5
     */
    public static RestfulParametes getDeleteInstanceParam(List<NeVxlanInstance> vxlanInstanceList, String ctrlUuid)
            throws ServiceException {
        RestfulParametes restParametes = new RestfulParametes();
        restParametes.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        restParametes.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlUuid);
        String strJsonReq = JsonUtil.toJson(vxlanInstanceList);
        restParametes.setRawData(strJsonReq);
        return restParametes;
    }

    /**
     * Fill Query Wan interface parameters in rest structure<br>
     * 
     * @param subInterUsedType - Sub interface type
     * @param ctrlUuid Controller UUID
     * @return Restful structure with delete parameters filled
     * @since SDNO 0.5
     */
    public static RestfulParametes getQueryWanInterfaceParam(String subInterUsedType, String ctrlUuid) {
        RestfulParametes restfulParametes = new RestfulParametes();
        restfulParametes.putHttpContextHeader(HttpContext.CONTENT_TYPE_HEADER, HttpContext.MEDIA_TYPE_JSON);
        restfulParametes.putHttpContextHeader("X-Driver-Parameter", "extSysID=" + ctrlUuid);
        TokenDataHolder.addToken2HttpRequest(restfulParametes);

        Map<String, String> queryParamMap = new ConcurrentHashMap<String, String>();
        queryParamMap.put(CommConst.DEVICE_WAN_SUB_INTERFACE_TYPE_PARAMETER, subInterUsedType);
        restfulParametes.setParamMap(queryParamMap);

        return restfulParametes;
    }

}
