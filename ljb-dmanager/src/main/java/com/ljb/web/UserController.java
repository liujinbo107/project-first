package com.ljb.web;

import com.ljb.config.ResponseResult;
import com.ljb.dao.MenuDao;
import com.ljb.dao.RoleDao;
import com.ljb.dao.UserDao;
import com.ljb.dao.UserMapper;
import com.ljb.pojo.entity.MenuInfo;
import com.ljb.pojo.entity.RoleInfo;
import com.ljb.pojo.entity.UserInfo;
import com.ljb.utils.MD5;
import com.ljb.utils.UID;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author 刘进波
 * @create 2019-08-07 8:53
 */
@RestController
public class UserController {

    @Autowired
    @Qualifier("myEntityManager")
    private EntityManager myEntityManager;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private MenuDao menuDao;

    @Autowired(required = false)
    private UserMapper userMapper;

    private List<UserInfo> userList;

    /**
     * 获取用户列表
     * @param userInfoMap
     * @return
     */
    @RequestMapping("toUserList")
    public ResponseResult touserlist(@RequestBody Map<String,String> userInfoMap){

        StringBuffer stringBuffer = new StringBuffer("select * from base_user where 1 = 1");

        StringBuffer stringBufferCount = new StringBuffer("select count(1) from base_user where 1=1");

        if(userInfoMap.get("userName")!=""){
            //查询
            stringBuffer.append(" and userName like concat('%','"+userInfoMap.get("userName").toString()+"','%')");
            //总条数
            stringBufferCount.append(" and userName like concat('%','"+userInfoMap.get("userName").toString()+"','%')");
        }
        if(userInfoMap.get("sex")!=""){
            //查询性别
            stringBuffer.append(" and sex ="+userInfoMap.get("sex"));
            //总条数
            stringBufferCount.append(" and sex ="+userInfoMap.get("sex"));
        }
        if(userInfoMap.get("sta")!=""&&userInfoMap.get("sto")!=""&&userInfoMap.get("sta")!=null&&userInfoMap.get("sto")!=null){
            //区间查询
            stringBuffer.append(" and createTime between '"+userInfoMap.get("sta").toString()+"' and '"+userInfoMap.get("sto").toString()+"'");
            //总条数
            stringBufferCount.append(" and createTime between '"+userInfoMap.get("sta").toString()+"' and '"+userInfoMap.get("sto").toString()+"'");
        }
        if(userInfoMap.get("sta")!=""&&userInfoMap.get("sta")!=null&& userInfoMap.get("sto")==""){
            //区间查询
            stringBuffer.append(" and createTime >= '"+userInfoMap.get("sta").toString()+"'");
            //总条数
            stringBufferCount.append(" and createTime >= '"+userInfoMap.get("sta").toString()+"'");
        }
        if(userInfoMap.get("sto")!=""&&userInfoMap.get("sto")!=null&& userInfoMap.get("sta")==""){
            //区间查询
            stringBuffer.append(" and createTime <= '"+userInfoMap.get("sto").toString()+"'");
            //总条数
            stringBufferCount.append(" and createTime <= '"+userInfoMap.get("sto").toString()+"'");
        }

        //获取当前页
        int pageNo = Integer.parseInt(userInfoMap.get("pageNo").toString());

        //获取每页条数
        int pageSize = Integer.parseInt(userInfoMap.get("pageSize").toString());

        //分页
        stringBuffer.append(" ORDER BY createTime desc limit "+(pageNo-1)*pageSize+","+pageSize);

        System.out.println(userInfoMap.toString());
        //清空缓存
        myEntityManager.clear();

        //查列表
        Query nativeQuery = myEntityManager.createNativeQuery(stringBuffer.toString(), UserInfo.class);

        //总条数
        Query nativeQuery1 = myEntityManager.createNativeQuery(stringBufferCount.toString());

        List<UserInfo> resultList = nativeQuery.getResultList();

        userList = resultList;

        //查询用户的角色信息
        resultList.forEach(user->{
            RoleInfo roleInfo = roleDao.forRoleInfoByUserId(user.getId());
            user.setRoleInfo(roleInfo);
        });

        ResponseResult responseResult = ResponseResult.getResponseResult();

        Map<String, Object> map = new HashMap<>();

        //放入map集合
        map.put("list",resultList);

        map.put("total",nativeQuery1.getResultList().get(0));

        responseResult.setResult(map);

        System.out.println(map);

        return responseResult;

    }

    /**
     * 删除用户
     * @param map
     * @return
     */
    @RequestMapping("todeluser")
    public ResponseResult todeluser(@RequestBody Map<String,Object> map){

        //获取userid
        long id = Long.parseLong(map.get("id").toString());

        ResponseResult responseResult = ResponseResult.getResponseResult();
        try {
            userDao.deleteById(id);

            responseResult.setSuccess("删除成功");

            responseResult.setCode(200);

        }catch (RuntimeException e){

            responseResult.setSuccess("删除失败");

            responseResult.setCode(500);
        }
        //
        return responseResult;

    }

    /**
     * 上传图片
     * @param file
     * @throws IOException
     */
    @RequestMapping("touploaduser")
    public void touploaduser(@Param("file") MultipartFile file) throws IOException {

        file.transferTo(new File("E:\\img\\"+file.getOriginalFilename()));
    }

    /**
     * 添加用户
     * @param userInfo
     * @return
     */
    @RequestMapping("toadduser")
    public ResponseResult toadduser(@RequestBody UserInfo userInfo){

        ResponseResult responseResult = ResponseResult.getResponseResult();

        System.out.println(userInfo+"用户信息******");
        try {

            //判断登录名称是否唯一
            if(userDao.findAllByLoginName(userInfo.getLoginName())!=null){

                responseResult.setCode(203);

                responseResult.setSuccess("登陆名称已存在");

                return responseResult;
            }
            //算法的id
            userInfo.setId(UID.next());
            //密码加密
            String lcg = MD5.encryptPassword(userInfo.getPassword(), "lcg");
            //重新设置密码
            userInfo.setPassword(lcg);

            userDao.save(userInfo);

            userMapper.adduserrole(userInfo.getId(),1908131117520000L);

            responseResult.setSuccess("添加成功");

            responseResult.setCode(200);
        }catch (Exception e){
            responseResult.setSuccess("添加失败");

            responseResult.setCode(500);
        }

        return responseResult;
    }

    /**
     * 修改用户
     * @param userInfo
     * @return
     */
    @RequestMapping("toupdateuser")
    public ResponseResult toupdateuser(@RequestBody UserInfo userInfo){

        ResponseResult responseResult = ResponseResult.getResponseResult();

        System.out.println(userInfo+"用户信息******");
        try {

            //判断登录名称是否唯一
            UserInfo allByLoginName = userDao.findAllByLoginName(userInfo.getLoginName());

            if(allByLoginName!=null){
                //判断是否是修改对象
                if(!userInfo.getId().toString().equals(allByLoginName.getId().toString())){
                    responseResult.setCode(203);

                    responseResult.setSuccess("登陆名称已存在");

                    return responseResult;
                }
            }
            //密码加密
            String lcg = MD5.encryptPassword(userInfo.getPassword(), "lcg");
            //重新设置密码
            userInfo.setPassword(lcg);

            userDao.saveAndFlush(userInfo);

            responseResult.setSuccess("修改成功ss");

            responseResult.setCode(200);
        }catch (Exception e){
            responseResult.setSuccess("修改失败");

            responseResult.setCode(500);
        }

        return responseResult;
    }

    /**
     * 解除用户绑定的角色
     * @param map
     * @return
     */
    @RequestMapping("todelbdrole")
    public ResponseResult delbdrole(@RequestBody Map<String,String> map){

        ResponseResult responseResult = ResponseResult.getResponseResult();
        try{
            //获取用户id
            long id = Long.parseLong(map.get("id"));
            //解除用户之前的角色
            userMapper.deluserrole(id);

            //给用户绑定一个最基本的普通用户
            userMapper.adduserrole(id,1908131117520000L);

            responseResult.setCode(200);
        }catch (Exception e){
            responseResult.setCode(500);
        }

        return responseResult;
    }

    /**
     * 角色列表
     * @return
     */
    @RequestMapping("tofindallrole")
    public ResponseResult findallrole(@RequestBody Map<String,String> map){
        //获取操作用户角色的等级
        Integer leval = Integer.parseInt(map.get("leval"));

        //获取该用户角色以下或等于的角色列表
        List<RoleInfo> all = roleDao.findAllByLevel(leval);

        ResponseResult responseResult = ResponseResult.getResponseResult();

        responseResult.setResult(all);

        return responseResult;

    }

    /**
     * 用户绑定角色
     * @param map
     * @return
     */
    @RequestMapping("tobdrole")
    public ResponseResult tobdrole(@RequestBody Map<String,String> map){

        //获取用户id
        long userId = Long.parseLong(map.get("userId"));

        //删除用户之前绑定的角色
        userMapper.deluserrole(userId);

        //获取绑定的角色id
        long roleId = Long.parseLong(map.get("roleId"));

        ResponseResult responseResult = ResponseResult.getResponseResult();

        try {
            //角色绑定
            userMapper.adduserrole(userId,roleId);

            responseResult.setSuccess("绑定成功");

            responseResult.setCode(200);
        }catch (Exception e){

            responseResult.setSuccess("绑定失败");

            responseResult.setCode(500);
        }
        return responseResult;
    }

    /**
     * 批量添加用户
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping("toaddusers")
    public ResponseResult toaddusers (@Param("file")MultipartFile file) throws IOException{

        ArrayList<UserInfo> userInfos = new ArrayList<>();

        //打开HSSFWorkbook对象

        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());

        XSSFSheet sheet = workbook.getSheetAt(0);

        int physicalNumberOfRows = sheet.getPhysicalNumberOfRows(); //获取表单所有行

        UID uid = new UID();

        //向数据库导入数据
        for (int i = 1; i < physicalNumberOfRows; i++){

            XSSFRow row = sheet.getRow(i);
            UserInfo userInfo = new UserInfo();

            //XSSFCell c0 = row.getCell(0);
            //long id = new Double(c0.getNumericCellValue()).longValue();
            userInfo.setId(uid.next());

            XSSFCell c1 = row.getCell(1);
            userInfo.setUserName(c1.getStringCellValue());

            XSSFCell c2 = row.getCell(2);
            userInfo.setLoginName(c2.getStringCellValue());

            XSSFCell c3 = row.getCell(3);
            userInfo.setParentId(new Double(c3.getNumericCellValue()).longValue());

            XSSFCell c4 = row.getCell(4);
            userInfo.setPassword(c4.getStringCellValue());

            XSSFCell c5 = row.getCell(5);
            userInfo.setSex((int)c5.getNumericCellValue());

            XSSFCell c6 = row.getCell(6);
            userInfo.setTel(c6.getStringCellValue());

            XSSFCell c7 = row.getCell(7);
            userInfo.setTouxiang(c7.getStringCellValue());

            userInfos.add(userInfo);

            //给每个用户绑定普通用户的角色
            userMapper.adduserrole(userInfo.getId(),1908131117520000L);

        }
        ResponseResult responseResult = ResponseResult.getResponseResult();
        try{
            userInfos.forEach(userInfo -> {
                userDao.saveAndFlush(userInfo);
            });
            responseResult.setCode(200);
        }catch (Exception e){
            responseResult.setCode(500);
        }

        return responseResult;
    }

    /**
     * 导出数据到Excel文件
     * @return
     */
    @RequestMapping("toexport")
    public ResponseResult toexport() throws IOException{

        XSSFWorkbook workbook = new XSSFWorkbook();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String fileName = "用户信息表"+simpleDateFormat.format(new Date())+".xlsx";
        String sheetName = "用户信息表";
        String[] titile ={"id","用户名","登录名","parentId","性别","电话","头像","创建时间","修改时间"};

        //在文档中添加表单
        XSSFSheet sheet = workbook.createSheet(sheetName);

        //创建单元格格式，并设置居中
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);

        //创建第一行用于填充标题
        XSSFRow titleRow = sheet.createRow(0);

        for (int i=0 ; i<titile.length ; i++) {
            //创建单元格
            XSSFCell cell = titleRow.createCell(i);
            //设置单元格内容
            cell.setCellValue(titile[i]);
            //设置单元格样式
            cell.setCellStyle(style);
        }

        System.out.println(userList);
        Row row = null;


        ResponseResult responseResult = ResponseResult.getResponseResult();
        try {
            for(int i = 0; i < userList.size();i++){
                //创建list.siza()行数据
                row = sheet.createRow(i + 1);
                //把值写进单元格
                row.createCell(0).setCellValue(userList.get(i).getId());
                row.createCell(1).setCellValue(userList.get(i).getUserName());
                row.createCell(2).setCellValue(userList.get(i).getLoginName());
                row.createCell(3).setCellValue(userList.get(i).getPassword());
                row.createCell(4).setCellValue(userList.get(i).getSex());
                row.createCell(5).setCellValue(userList.get(i).getTel());
                row.createCell(6).setCellValue(userList.get(i).getTouxiang());
                row.createCell(7).setCellValue(sdf.format(userList.get(i).getCreateTime()));
                row.createCell(8).setCellValue(sdf.format(userList.get(i).getUpdateTime()));

            }

            //获取配置文件中保存对应excel文件的路径  文件目录
            File file = new File("E:\\biao");
            if(!file.exists()){
                file.mkdirs();
            }

            String savePath = "E:\\biao\\"+fileName;
            FileOutputStream fileOut = new FileOutputStream(savePath);
            workbook.write(fileOut);

            fileOut.close();

            responseResult.setCode(200);
        }catch (Exception e){

            responseResult.setCode(500);
        }

        return responseResult;
    }

}
