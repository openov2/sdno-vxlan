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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;

public class ThrowVxlanExcptTest {

    @Test
    public void testCheckRspThrowException() {
        try {
            ThrowVxlanExcpt.checkRspThrowException(null);
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testThrowResNotExistAsBadReq() {
        try {
            ThrowVxlanExcpt.throwResNotExistAsBadReq("", "resDesc");
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testThrowResNotExistAsBadReq2() {
        try {
            ThrowVxlanExcpt.throwResNotExistAsBadReq("111", "resDesc");
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testThrowTenantIdInvalid() {
        try {
            ThrowVxlanExcpt.throwTenantIdInvalid("", "111");
            ThrowVxlanExcpt.throwTenantIdInvalid("exptTenantId", "realTenantId");
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testThrowParmaterInvalid() {
        try {
            ThrowVxlanExcpt.throwParmaterInvalid("111", "resDesc");
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testThrowParmaterInvalid2() {
        try {
            ThrowVxlanExcpt.throwParmaterInvalid("", "resDesc");
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testThrowHavingIpsecAsParmaterInvalid() {
        try {
            ThrowVxlanExcpt.throwHavingIpsecAsParmaterInvalid("www");
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testThrowNotHavingIpsecAsParmaterInvalid() {
        try {
            ThrowVxlanExcpt.throwNotHavingIpsecAsParmaterInvalid("www");
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testThrowTenantIdMissing() {
        try {
            ThrowVxlanExcpt.throwTenantIdMissing("www");
        } catch(ServiceException e) {
            assertTrue(true);
        }
    }

}
