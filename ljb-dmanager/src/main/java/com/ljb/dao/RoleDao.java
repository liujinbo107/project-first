package com.ljb.dao;

import com.ljb.pojo.entity.RoleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoleDao extends JpaRepository<RoleInfo,Long> {

    /**
     * 根据用户ID获取角色信息
     * @param userId
     * @return
     */
    @Query(value = "select br.* from base_user_role bur INNER JOIN base_role br ON bur.roleId=br.id where bur.userId=?1",nativeQuery = true)
    public RoleInfo forRoleInfoByUserId(Long userId);


    /**
     * 判断roleName的唯一性
     * @param roleName
     * @return
     */
    public RoleInfo findAllByRoleName(String roleName);

    @Query(value = "select br.* from base_role br where level >= ?1",nativeQuery = true)
    public List<RoleInfo> findAllByLevel(Integer level);

}
