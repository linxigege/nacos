/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.auth.persist;

import com.alibaba.nacos.auth.configuration.ConditionOnEmbeddedStorage;
import com.alibaba.nacos.auth.model.Page;
import com.alibaba.nacos.auth.model.PermissionInfo;
import com.alibaba.nacos.auth.persist.repository.PaginationHelper;
import com.alibaba.nacos.auth.persist.repository.embedded.DatabaseOperate;
import com.alibaba.nacos.auth.persist.repository.embedded.AuthEmbeddedStoragePersistServiceImpl;
import com.alibaba.nacos.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * There is no self-augmented primary key.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@Conditional(value = ConditionOnEmbeddedStorage.class)
@Component
public class AuthEmbeddedPermissionPersistServiceImpl implements PermissionPersistService {
    
    public static final PermissionRowMapper PERMISSION_ROW_MAPPER = new PermissionRowMapper();
    
    @Autowired
    private DatabaseOperate databaseOperate;
    
    @Autowired
    private AuthEmbeddedStoragePersistServiceImpl persistService;
    
    @Override
    public Page<PermissionInfo> getPermissions(String role, int pageNo, int pageSize) {
        PaginationHelper<PermissionInfo> helper = persistService.createPaginationHelper();
        
        String sqlCountRows = "SELECT count(*) FROM permissions WHERE ";

        String sqlFetchRows = "SELECT role,resource,action FROM permissions WHERE ";
    
        String where = " role= ? ";
        List<String> params = new ArrayList<>();
        if (StringUtils.isNotBlank(role)) {
            params = Collections.singletonList(role);
        } else {
            where = " 1=1 ";
        }
        
        Page<PermissionInfo> pageInfo = helper
                .fetchPage(sqlCountRows + where, sqlFetchRows + where, params.toArray(), pageNo,
                        pageSize, PERMISSION_ROW_MAPPER);
        
        if (pageInfo == null) {
            pageInfo = new Page<>();
            pageInfo.setTotalCount(0);
            pageInfo.setPageItems(new ArrayList<>());
        }
        return pageInfo;
    }
    
    public static final class PermissionRowMapper implements RowMapper<PermissionInfo> {
        
        @Override
        public PermissionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            PermissionInfo info = new PermissionInfo();
            info.setResource(rs.getString("resource"));
            info.setAction(rs.getString("action"));
            info.setRole(rs.getString("role"));
            return info;
        }
    }
    
}