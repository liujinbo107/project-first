package com.sso.dao;

import com.ljb.pojo.entity.MenuInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 作者: LCG
 * 日期: 2019/8/5 00:32
 * 描述:
 */
public interface MenuDao extends JpaRepository<MenuInfo,Long> {
    /**
     * 获取角色的菜单信息
     * @return
     */
    @Query(value = "select bm.* from base_role_menu brm INNER JOIN base_menu bm ON brm.menuId=bm.id where brm.roleId=?1 and bm.leval=?2 and bm.parentId=?3",nativeQuery = true)
    public List<MenuInfo> getFirstMenuInfo(Long roleId, Integer leval,Long parentId);
}
