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

package org.openo.sdno.vxlan.mocoserver;

import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.moco.responsehandler.MocoResponseHandler;

public class DeleteVxlanResponseHandler extends MocoResponseHandler {

    @Override
    public void processRequestandResponse(HttpRquestResponse httpObject) {

        HttpResponse httpResponse = httpObject.getResponse();

        ResultRsp<String> newResult = new ResultRsp<String>(ErrorCode.OVERLAYVPN_SUCCESS);

        httpResponse.setData(JsonUtil.toJson(newResult));
    }
}
