import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.DemoApplication;
import com.dbms.bean.VulnerabilityPasswordBean;
import com.dbms.core.AjaxResult;
import com.dbms.core.ConfigurationCheck;
import com.dbms.core.SQLParserUtil;
import com.dbms.core.dbase.DataBaseUtilService;
import com.dbms.entity.AccessStrategyEntity;
import com.dbms.entity.DbaseAccountEntity;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.RiskOperationEntity;
import com.dbms.repository.DbopService;
import com.dbms.service.*;
import com.dbms.utils.CryptoUtil;
import com.dbms.utils.DesensitizationUtil;
import com.dbms.utils.StringUtils;
//import com.gbase.jdbc.GBaseConnection;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import com.dbms.utils.ScramSha256Utils;
import com.dbms.utils.Sha512Utils;
import com.dbms.utils.Sha1Hashing;
import com.dbms.utils.Sha256Hashing;
import com.dbms.utils.MD5Encryption;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@Transactional
public class SQLTest {


    @Autowired
    private RiskOperationService riskOperationService;

    @Autowired
    public DbaseService dbaseService;
    @Autowired
    public DbopService dbopService;
    @Autowired
    private DataBaseUtilService dataBaseUtilService;

    @Autowired
    private AccessStrategyService accessStrategyService;

    @Autowired
    private DbaseAccessService dbaseAccessService;

    @Autowired
    private DbaseAccountService dbaseAccountService;


    @Test
    public void testGetSchemas(){

        String sql = "select * from \"DBMS\".\"MENU\";";
        try {
            Set<String> schemas = SQLParserUtil.getSchemas(sql, SQLParserUtil.getSqlType(sql));
            System.out.println(JSON.toJSONString(schemas));
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetRisk(){
        RiskOperationEntity riskOperationEntity = riskOperationService.getById(1);
        System.out.println(JSON.toJSONString(riskOperationEntity));
        riskOperationEntity.stringToObject();
        System.out.println(JSON.toJSONString(riskOperationEntity));
    }


    @Test
    public void testSensitive(){
        // 身份证号脱敏
        String regCard = "(\\w{6})\\w*(\\w{3})";
        String cardParam = "$1*********$2";
        String card = "130321198712233329";
        System.out.println("身份证号::" + DesensitizationUtil.desensitizationData(card, regCard, cardParam));
        String phone = "(\\d{3})\\d{4}(\\d{4})";
        String phoneParam = "$1****$2";
        String phonenum= "15933501031";
        System.out.println("手机号::" + DesensitizationUtil.desensitizationData( phonenum, phone, phoneParam));
    }

    @Test
    public void testVulkingbase() throws Exception {
        Integer dbId = 13;
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        dbaseEntity.setPassword("123");
//        System.out.println("dbaseEntity:" + dbaseEntity.getPassword());
        List<Map<String, Object>> accounts = new ArrayList<>();
        String sql = "select rolname as username,rolpassword as password from pg_catalog.pg_authid WHERE rolpassword IS NOT NULL ;";
        try {
            accounts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Map<String, Object> account:accounts) {
            String username = (String) account.get("username");
            String passwordValue = (String) account.get("password");
//        String passwordValue = "SCRAM-SHA-256$4096:Q6IaEieIOMnFhsdE9rl75g==$z7YiMKMLX/06gKwwARmwEa97mJly23Mp+dUaL1gneWQ=:lKb5ljs+KsWfVBZ47/ucZ5cuFLJV6ehb2RioxlJTCiQ=";
            String[] parts = passwordValue.split("[:$]");
            int iterations = Integer.parseInt(parts[1]);   // 4096
            byte[] salt = Base64.getDecoder().decode(parts[2]);  // salt
            String truthStoredKeyBase64 = parts[3];  // stored key
            String passwordSeed = "123";
            String storedKeyBase64 = ScramSha256Utils.calculateStoredKey(passwordSeed.getBytes(), salt, iterations);
            System.out.println("Stored Key: " + storedKeyBase64);
            System.out.println("Real Key: " + parts[3]);
            if (storedKeyBase64.equals(truthStoredKeyBase64)) {
                System.out.println(username + "'s Password is correct");
            } else {
                System.out.println(username + "'s Password is incorrect");
            }
        }
    }

    public void setDbaseEntityInfo(DbaseEntity dbaseEntity, String schemaName){
        dbaseEntity.setSchemaName(schemaName);
        String pw = CryptoUtil.decode(dbaseEntity.getPassword());
        dbaseEntity.setPassword(pw);
        if (StringUtils.isNotEmpty(dbaseEntity.getSchemaName())){
//            String dir = "/";
//            if (dbaseEntity.getDbType().equals("DM") || dbaseEntity.getDbType().equals("Oracle")){
//                dir = ":";
//            } else if (dbaseEntity.getDbType().equals("MSSQL")){
//                dir = ";database=";
//            }
            if (dbaseEntity.getDbType().equals("GBase")||dbaseEntity.getDbType().equals("Oracle")||dbaseEntity.getDbType().equals("ElasticSearch")){

            } else {
                dbaseEntity.setUrl(dbaseEntity.getUrl() + dbaseEntity.getUrlDir() + dbaseEntity.getSchemaName());
            }
        }
    }

    public void setDbaseEntityInfo(DbaseEntity dbaseEntity){
        setDbaseEntityInfo(dbaseEntity, null);
    }

    @Test
    public void testVulDM() throws Exception {
        Integer dbId = 1;
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        setDbaseEntityInfo(dbaseEntity);
        List<Map<String, Object>> accounts = dbopService.getAccount(dbaseEntity);
        String sql = "select DU.USERNAME, SU.PASSWORD from SYSUSERS SU, DBA_USERS DU where SU.ID = DU.USER_ID;";
        try {
            accounts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Map<String, Object> account:accounts) {
            String username = (String) account.get("USERNAME");
            String passwordValue = (String) account.get("PASSWORD");
            String passwordSeed = "123";
            String storedKey = Sha512Utils.SHA512(passwordSeed);
            System.out.println("Stored Key: " + storedKey);
            System.out.println("Real Key: " + passwordValue);
            if (storedKey.equals(passwordValue)) {
                System.out.println(username + "'s Password is correct");
            } else {
                System.out.println(username + "'s Password is incorrect");
            }
        }
    }
    @Test
    public void testVulGbase8a() throws Exception {
        Integer dbId = 15;
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        setDbaseEntityInfo(dbaseEntity);
        List<Map<String, Object>> accounts = dbopService.getAccount(dbaseEntity);
        String sql = "select user, password from gbase.user;";
        try {
            accounts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> pwList = new ArrayList<String>();
        pwList = Arrays.asList("123456", "SYSDBA001", "sasuke", "SYSDBA", "root", "root123", "abc", "666", "888888", "123");
        List<String> encodedPsw = new ArrayList<>();

        for (String psw:pwList){
//            String encodeSql = "SELECT PASSWORD('" + psw + "') AS PASSWORD;";
//            encodedPsw.add(dataBaseUtilService.queryForList(dbaseEntity, encodeSql).get(0).get("PASSWORD").toString());
            String encodeP = Sha1Hashing.getSHA1(psw);
            encodedPsw.add(encodeP);
        }

        System.out.println(encodedPsw);
        for (Map<String, Object> account:accounts) {
            String username = (String) account.get("user");
            String passwordValue = (String) account.get("password");
            if (encodedPsw.contains(passwordValue)) {
                System.out.println(username + "'s Password is vulnerable");
            } else {
//                System.out.println(username + "'s Password is incorrect");
            }
        }
    }
    @Test
    public void testVulGbase8s() throws Exception {
        Integer dbId = 2;
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        setDbaseEntityInfo(dbaseEntity);
        List<Map<String, Object>> accounts = new ArrayList<>();
        List<Map<String, Object>> salts = new ArrayList<>();
        String sql = "SELECT username, hashed_password FROM sysuser:sysintauthusers;";
        try {
            accounts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sql = "SELECT hashed_password, salt FROM sysuser:sysintauthusers;";
        try {
            salts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> pwList = new ArrayList<String>();
        pwList = Arrays.asList("123456", "SYSDBA001", "sasuke", "SYSDBA", "root", "root123", "abc", "666", "888888", "123");
        List<String> encodedPsw = new ArrayList<>();
        for (Map<String, Object> account:accounts) {
            String username = (String) account.get("user");
            String passwordValue = (String) account.get("password");
            String salt = salts.get(0).get(passwordValue).toString();
            for (String psw:pwList){
//            String encodeSql = "SELECT PASSWORD('" + psw + "') AS PASSWORD;";
//            encodedPsw.add(dataBaseUtilService.queryForList(dbaseEntity, encodeSql).get(0).get("PASSWORD").toString());
                String encodeP = Sha256Hashing.gbase8sHash(psw, salt);
                encodedPsw.add(encodeP);
            }
            if (encodedPsw.contains(passwordValue)) {
                System.out.println(username + "'s Password is vulnerable");
            } else {
//                System.out.println(username + "'s Password is incorrect");
            }
        }
    }
    @Test
    public void testVulOscar() throws Exception {
        Integer dbId = 17;
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        setDbaseEntityInfo(dbaseEntity);
        List<Map<String, Object>> accounts = new ArrayList<>();
        String sql = "SELECT USENAME, PASSWD FROM SYS_SHADOW;";
        try {
            accounts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> pwList = new ArrayList<String>();
        pwList = Arrays.asList("123456", "SYSDBA001", "sasuke", "SYSDBA", "root", "root123", "abc", "666", "888888", "123");
        List<String> encodedPsw = new ArrayList<>();
        for (Map<String, Object> account:accounts) {
            String username = (String) account.get("USENAME");
            String passwordValue = (String) account.get("PASSWD");
            for (String psw:pwList){
                String encodeP = MD5Encryption.encrypt(username, psw);
                encodedPsw.add(encodeP);
            }
            if (encodedPsw.contains(passwordValue)) {
                System.out.println(username + "'s Password is vulnerable");
            } else {
//                System.out.println(username + "'s Password is incorrect");
            }
        }
    }
    @Test
    public void testConfigCheck() throws Exception {
        Integer dbId = 17;
        // DM 1 Gbase8a 15 Gbase8s 2 kingbase 13 Oscar 17
        ConfigurationCheck configurationCheck = new ConfigurationCheck();
//        List<Map<String, Object>> dbConfigChecks = new ArrayList<>();
//        Map<String, Object> dbConfigCheck = new HashMap<>();
        DbaseEntity dbaseEntity  = dbaseService.getById(dbId);
        setDbaseEntityInfo(dbaseEntity);
        String schemaname = dbaseEntity.getSchemaName();
        configurationCheck.setPortId(dbaseEntity.getPort());
        configurationCheck.setDatabaseVersion(dbaseEntity.getVersion());
        List<Map<String, Object>> res = null;
        String sql = "";
        if(dbaseEntity.getDbType().equals("DM")){
            // DM
            // 特殊字符
            sql = "SELECT username FROM sys.dba_users WHERE username LIKE '% %' OR username LIKE '%\"%' OR username LIKE '%,%' OR username LIKE '%--%' OR username LIKE '%'||chr(60)||'%'||chr(62)||'%' OR username LIKE '$%' OR username LIKE '!%' OR lower(username) LIKE '%identified %'; ";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            if (!res.isEmpty()){
                configurationCheck.setSpecialCharacters(res.toString());
            }
//            configurationCheck.setSpecialCharacters(res.toString());

//            确保缺省示例以及示例数据库删除
            sql = "SELECT USERNAME FROM ALL_USERS WHERE USERNAME IN ('BOOKSHOP', 'DMHR');";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            if (!res.isEmpty()) {
                configurationCheck.setEnsureDatabase(res.toString());
            }

//            启用客体重用功能
            sql = "SELECT * FROM V$PARAMETER WHERE NAME='ENABLE_OBJ_REUSE';";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            configurationCheck.setEnableFunction(res);
            //configurationCheck.setEnableFunctionFilevalue(res.get("File value"));
            for (Map<String, Object> map : res) {
                Object fileValue = map.get("FILE_VALUE");
                if (fileValue != null) {
                    configurationCheck.setEnableFunctionFilevalue(fileValue.toString());
                }
            }

//            确保PUBLIC对程序包执行权限最小化(建议删除以下包PBULIC权限)
            sql = "SELECT table_name FROM dba_tab_privs WHERE grantee='PUBLIC' AND privilege='EXECUTE' AND table_name IN ('UTL_FILE', 'UTL_SMTP', 'UTL_HTTP', 'DBMS_RANDOM', 'DBMS_LOB', 'DBMS_SQL', 'DBMS_SYS_SQL', 'DBMS_JOB', 'DBMS_BACKUP_RESTORE', 'DBMS_OBFUSCATION_TOOLKIT');";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            configurationCheck.setEnsurePublic(res);

//            限制PUBLIC对动态视图的查询权限
            sql = "SELECT * FROM dba_tab_privs WHERE GRANTEE='PUBLIC';";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            configurationCheck.setRestrictPublic(res);
            for (Map<String, Object> map : res) {
                String TABLENAME = map.get("TABLE_NAME").toString();
                if (TABLENAME.startsWith("V$")) {
                    configurationCheck.setJustrestrictPublic(false);
                    break;
                }
                else configurationCheck.setJustrestrictPublic(true);
            }

//            确保WITH　ADMIN　OPTION权限正确授予
//            sql = "SELECT grantee, granted_role FROM SYS.DBA_ROLE_PRIVS WHERE grantee NOT IN ('SYSDBA') AND admin_option='Y' AND grantee NOT IN (SELECT distinct FROM \"SYS\".\"DBA_OBJECTS\");";
//            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
//            configurationCheck.setMakeSure(res.toString());

//            确保DBA角色分配合理
//            sql = "SELECT grantee FROM DBA_ROLE_PRVS WHERE granted_role='DBA' AND grantee NOT IN ('SYSDBA', 'SYSAUDITOR', 'SYSSSO');";
//            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
//            configurationCheck.setEnsureDba(res.toString());

//            确保管理员特权分配合理
            sql = "SELECT grantee, privilege FROM \"SYS\".\"DBA_SYS_PRIVS\" WHERE grantee NOT IN (SELECT DISTINCT granted_role FROM \"SYS\".\"DBA_ROLE_PRIVS\") AND privilege <> 'UNLIMITED TABLESPACE' ORDER BY grantee;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            if (!res.isEmpty()){
                configurationCheck.setAppropriatelyDistribute(res);
            }

//            确保数据库系统表和其他的配置信息只限于DBA用户或角色
//            sql = "SELECT grantee, privilege, owner, table_name FROM :SYS\".\"DBA_TAB_PRIVS\" WHERE (owner='SYS' OR table_name LIKE 'DBA_%') AND privilege <> 'EXECUTE' AND grantee NOT IN ('PUBLIC', 'DBA', 'DB_AUDIT_PUBLIC', 'DB_AUDIT_ADMIN', 'DB_AUDIT_OPER', 'SYS_ADMIN', 'DB_POLICY_PUBLIC') AND grantee NOT IN (SELECT grantee FROM \"SYS\".\"DBA_ROLE_PRIVS\" WHERE granted_role='DBA') ORDER BY grantee;";
//            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
//            configurationCheck.setEnsureLimited(res.toString());

//            确保设置数据库对象审计策略
//            sql = "SELECT * FROM SYSAUDITOR.SYSAUDITRULES;";
//            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
//            configurationCheck.setEnsureAuditPolicy(res.toString());

//            确保开启审计开关
            sql = "SELECT * FROM V$PARAMETER WHERE NAME='ENABLE_AUDIT';";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            for (Map<String, Object> map : res) {
                Object fileValue = map.get("VALUE");
                if (fileValue != null) {
                    configurationCheck.setMksureAuditSwitch(fileValue.toString());
                }
            }
//            configurationCheck.setMksureAuditSwitch(res);

//            确保审计实时侵害检测功能开启
            sql = "SELECT * FROM V$PARAMETER WHERE NAME='ENABLE_AUDIT';";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            for (Map<String, Object> map : res) {
                Object fileValue = map.get("VALUE");
                if (fileValue != null) {
                    configurationCheck.setEnsureAuditIntrusion(fileValue.toString());
                }
            }
//            configurationCheck.setEnsureAuditIntrusion(res);

//            确保设置密码重用策略
            sql = "SELECT T2.USERNAME, T1.REUSE_MAX, T1.REUSE_TIME FROM \"SYS\".\"SYSUSER$\"T1, \"SYS\".\"ALL_USERS\"T2 WHERE T1.ID=T2.USER_ID AND (REUSE_MAX=0 OR REUSE_TIME=0) ORDER BY T2.USERNAME;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            configurationCheck.setEnsurePSWReuse(res);
            for (Map<String, Object> map : res) {
                Object REUSEMAX = map.get("REUSE_MAX");
                Object REUSETIME = map.get("REUSE_TIME");
                if (REUSEMAX.equals('0') || REUSETIME.equals('0')) {
                    configurationCheck.setReuseMax(REUSEMAX.toString());
                }
            }

//            确保设置DBMS账户锁定时间
            sql = "SELECT T2.USERNAME, T1.LOCK_TIME FROM \"SYS\".\"SYSUSER$\"T1, \"SYS\".\"ALL_USERS\"T2 WHERE T1.ID=T2.USER_ID AND T1.LOCK_TIME=0 ORDER BY T2.USERNAME;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            for (Map<String, Object> map : res) {
                Object lock_time = map.get("LOCK_TIME");
                if (lock_time.equals('0')) {
                    configurationCheck.setMksureLockoutTime(lock_time.toString());
                }
            }
//            configurationCheck.setMksureLockoutTime(res);

//            确保设置账户尝试登陆次数
            sql = "SELECT * FROM SYSUSERS WHERE failed_attemps>3;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            configurationCheck.setMksureLoginAttempts(res);

//            sql = "SELECT t2.username, t1.life_time FROM \"SYS\".\"SYSUSERS\"t1, \"SYS\">\"DBA_USERS\"t2 WHERE t2.user_id=t1.id AND t1.life_time>60;";
//            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
//            configurationCheck.setMksureValidityPeriod(res.toString());

//            确保系统默认口令策略设置合理
            sql = "SELECT * FROM V$PARAMETER WHERE NAME='PWD_POLICY';";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            for (Map<String, Object> map : res) {
                Object DefaultPSW = map.get("VALUE");
                if (DefaultPSW.equals('0')) {
                    configurationCheck.setEnsureDefaultPSW(DefaultPSW.toString());

                }
            }
//            configurationCheck.setEnsureDefaultPSW(res);

//            限制用户IP地址
            sql = "SELECT ALLOW_ADDR, NOT_ALLOW_ADDR FROM SYSUSERS;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            for (Map<String, Object> map : res) {
                if (map.get("ALLOW_ADDR") == null || map.get("NOT_ALLOW_ADDR") == null) {
                    configurationCheck.setUserIp("未设置");
                    break;
                }
            }
//            configurationCheck.setUserIp(res);

//            限制用户访问时间段
            sql = "SELECT ALLOW_DT, NOT_ALLOW_DT FROM SYSUSERS;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            for (Map<String, Object> map : res) {
                if (map.get("ALLOW_DT") == null || map.get("NOT_ALLOW_DT") == null) {
                    configurationCheck.setLimitAccessPeriods("未设置");
                    break;
                }
            }

//            确保数据库通讯加密
//            configurationCheck.setLimitAccessPeriods(res);


//            sql = "ELECT 'comm encry typr' AS Cont, case WHEN file_value='0' THEN 0 else 1 END AS Resu FROM \"SYS\".\"V$PARAMETER\" WHERE name='ENABLE_ENCRYPT'; SELECT * FROM V$PARAMETER WHERE NAME='COMM_ENCRYPT_NAME';";
//            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
//            configurationCheck.setEnsureCommunicationEncrypted(res.toString());
        }
        else if (dbaseEntity.getDbType().equals("GBase")){
            if (dbaseEntity.getDbDriver().equals("com.gbase.jdbc.Driver")){
                // gbase8a
                //                特殊字符
                sql = "SELECT user FROM gbase.user where user='sysdba'";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                if (!res.isEmpty()){
                    configurationCheck.setSpecialCharacters(res.toString());
                }
//                configurationCheck.setSpecialCharacters(res.toString());

//                确保DBA角色分配合理
                sql = "SELECT user FROM gbase.user where user='sysdba';";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                if (res.isEmpty())
                    configurationCheck.setEnsureDba(true);
                else
                    configurationCheck.setEnsureDba(false);

//                确保管理员特权分配合理
//                sql = "SELECT user, host FROM gbase.user;";
                sql = "SELECT * FROM gbase.user;";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                for (Map<String, Object> map : res) {
                    if (map.get("Select_priv").equals("Y")||map.get("Insert_priv").equals("Y")||map.get("Delete_priv").equals("Y")||map.get("Create_priv").equals("Y")||map.get("Drop_priv").equals("Y")||map.get("Update_priv").equals("Y")||map.get("Alter_priv").equals("Y")){
                        configurationCheck.setAppropriatelyDistribute(res);
                    }
                }
//                configurationCheck.setAppropriatelyDistribute(res);

//                确保设置密码重用策略
                sql = "SHOW VARIABLES LIKE 'old_passwords';";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                configurationCheck.setEnsurePSWReuse(res);
                configurationCheck.setReuseMax(res.get(0).get("Value").toString());


//                确保检查密码是否过于简单
                sql = "SELECT * FROM gbase.user WHERE user=''; ";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                if (!res.isEmpty()){
                    configurationCheck.setMksurePSWSimple(res.toString());
                }


//                限制用户IP地址
                sql = "SELECT user, host FROM gbase.user WHERE host = '%';";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                for (Map<String, Object> map : res) {
                    if (map.get("host").toString().contains("%")) {
                        configurationCheck.setUserIp("存在问题");
                        break;
                    }
                }
//                configurationCheck.setUserIp(res.toString());

//                确保数据文件最小权限
                sql = "show variables where variable_name='datadir';";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                configurationCheck.setEnsureDataLeastPrivilege(res);

//                确保日志文件最小权限
                sql = "show variables like 'general_log_file';";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                sql = "show global variables like 'log_warnings';";
                res.add(dataBaseUtilService.queryForList(dbaseEntity, sql).get(0));
                sql = "show variables like 'general_log';";
                res.add(dataBaseUtilService.queryForList(dbaseEntity, sql).get(0));
                configurationCheck.setEnsureLogfileLeastPrivilege(res);

//                确保文件最小权限
                sql = "show global variables like 'basedir';";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                configurationCheck.setEnsureLeastPrivilege(res);

//                确保开启SSL通讯保护
                sql = "SELECT user, host, ssl_type FROM gbase.user WHERE NOT HOST IN ('::1', '127.0.0.1', 'localhost');";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                sql = "SHOW variables WHERE variable_name = 'have_ssl';";
                res.add(dataBaseUtilService.queryForList(dbaseEntity, sql).get(0));
                configurationCheck.setMksureSSLProtection(res.toString());

//                确保sql_mode中含有NO_AUTO_CREATE_USER，是STRICT_ALL_TABLES模式
                sql = "SELECT @@session.sql_mode;";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                sql = " SHOW VARIABLES LIKE 'sql_mode';";
                res.add(dataBaseUtilService.queryForList(dbaseEntity, sql).get(0));
                if (res.get(0).get("@@session.sql_mode").toString().contains("NO_AUTO_CREATE_USER")){
                    configurationCheck.setMksureSQLMode(true);
                }


//                确保某些功能正常启用
                sql = "select * from information_schema.global_variables where variable_name='skip_show_database';";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                sql = "select * from information_schema.global_variables where variable_name='have_symlink';";
                res.add(dataBaseUtilService.queryForList(dbaseEntity, sql).get(0));
                sql = "show variables like 'have_symlink';";
                res.add(dataBaseUtilService.queryForList(dbaseEntity, sql).get(0));
                sql = "show variables where variable_name='local_infile';";
                res.add(dataBaseUtilService.queryForList(dbaseEntity, sql).get(0));
                for(int i = 0; i<res.size(); i++){
                    String s = res.get(i).get("VARIABLE_VALUE").toString();
                    if (s.contains("ON")||s.contains("1")||s.contains("OFF")){
                        configurationCheck.setMksureCertainFeatures(true);
                    }
                }
//                configurationCheck.setMksureCertainFeatures(res.toString());
            }else if (dbaseEntity.getDbDriver().equals("com.gbasedbt.jdbc.Driver")){
                // gbase8s
                //            确保缺省示例以及示例数据库删除
                sql = "select name from sysmaster:sysdatabases;";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                if (!res.isEmpty()) {
                    configurationCheck.setEnsureDatabase(res.toString());
                }

                //            确保PUBLIC对程序包执行权限最小化(建议删除以下包PBULIC权限)
                sql = "select username from sysusers where username = 'PUBLIC' AND (usertype = 'C' or usertype = 'R' or usertype = 'D');";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                sql = "select first 1 '', 0 from sysmaster:sysprocauth perm, sysmaster:sysprocedures proc\n" +
                        "where perm.procid = proc.procid and\n" +
                        " (proc.procname = \"lotofile\" or proc.procname = \"filetoclob\" or \n" +
                        "proc.procname = \"ifx_file_to_file\" ) and\n" +
                        " perm.grantee = \"public\";";
                res.add(dataBaseUtilService.queryForList(dbaseEntity, sql).get(0));
                configurationCheck.setEnsurePublic(res);

                //            限制PUBLIC对动态视图的查询权限
                sql = "select first 1 '', 0\n" +
                        "from sysmaster:sysprocauth perm, sysmaster:sysprocedures proc\n" +
                        "where perm.procid = proc.procid and\n" +
                        " (proc.procname = \"ifx_replace_module\" or proc.procname = \n" +
                        "\"ifx_load_internal\" or proc.procname = \"reload_module\" ) and\n" +
                        " perm.grantee = \"public\";\n";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                configurationCheck.setRestrictPublic(res);
                if(!res.isEmpty()){
                    configurationCheck.setJustrestrictPublic(false);
                }
                else{
                    configurationCheck.setJustrestrictPublic(true);
                }

//                禁用非 DBA 用户使用 C 或 JAVA 程序语言的权限
                sql = "select perm.grantee, r.langname from SYSLANGAUTH perm,sysroutinelangs r where perm.langid = r.langid and (r.langname = 'c' or r.langname = 'java') and perm.grantee <> 'DBA';";
                res = dataBaseUtilService.queryForList(dbaseEntity, sql);
                if (!res.isEmpty()) {
                    configurationCheck.setMksureNoDbaUser(res);
                }
            }
        }
        else if (dbaseEntity.getDbType().equals("KingBase")){
            // Kingbase
            dbaseEntity.setPassword("123");

//            确保缺省示例以及示例数据库删除
            sql = "select datname from sys_database where datname IN ('SAMPLES');";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            if (!res.isEmpty()) {
                configurationCheck.setEnsureDatabase(res.toString());
            }
//            configurationCheck.setEnsureDatabase(res.toString());

//            确保WITH　ADMIN　OPTION权限正确授予
////            错误: 关系 "sys_aothid" 不存在
//            sql = "select grantee, privilege from DBA_SYS_PRIVS where grantee not in ('SYSTEM', 'SYSSSO', 'SYSSAO') and admin_option='YES';";
//            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
//            configurationCheck.setMakeSure(res.toString());

//          确保DBA角色分配合理
            sql = "select rolname from sys_authid where rolcreaterole='true' and rolname not in ('SYSTEM', 'SYSSSO', 'SYSSAO');";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            if (res.isEmpty())
                configurationCheck.setEnsureDba(true);
            else
                configurationCheck.setEnsureDba(false);


//            确保管理员特权分配合理
////            错误: 关系 "sys_aothid" 不存在
//            sql = "select rolname from sys_aothid where rolcreatedb='true' and rolname!='SYSTEM';";
//            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
//            configurationCheck.setAppropriatelyDistribute(res);

//            确保开启审计开关
            sql = "select setting from sys_settings where name='audit_trail';";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            for (Map<String, Object> map : res) {
                Object fileValue = map.get("VALUE");
                if (fileValue != null) {
                    configurationCheck.setMksureAuditSwitch(fileValue.toString());
                }
            }
//            configurationCheck.setMksureAuditSwitch(res.toString());

////            超时
//            sql = "select setting from sys_settings where name='password_condition_simple';";
//            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
//            configurationCheck.setMksurePSWSimple(res.toString());
//            sql = "select setting from sys_settings where name='password_condition_user';";
//            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
//            configurationCheck.setMksurePSWSimple(configurationCheck.getMksurePSWSimple()+res.toString());

//            确保设置DBMS账户密码的有效期
            sql = "select setting from sys_settings where name='password_time';";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            if (!res.isEmpty()){
                configurationCheck.setMksureValidityPeriod(res.toString());
                for (Map<String, Object> map : res) {
                    String TABLENAME = map.get("name").toString();
                    if (TABLENAME.equals("password_time")) {
                        configurationCheck.setPassword_time(false);
                        break;
                    }
                    else configurationCheck.setPassword_time(true);
                }
            }

//            确保数据文件最小权限
            sql = "select setting from sys_settings where name='data_directory';";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            configurationCheck.setEnsureDataLeastPrivilege(res);

//            合理开启restricted_dba安全开关
            sql = "select setting from sys_settings where name='restricted_dba';";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            configurationCheck.setEnsureRestrictedDBA(res.toString());

//            确保开启SSL通讯保护
            sql = "select setting from sys_settings where name='ssl';";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            configurationCheck.setMksureSSLProtection(res.toString());

//            确保配置了非缺省的Unix socket权限
            sql = "select setting from sys_settings where name='unix_socket_permissions'; ";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            for (Map<String, Object> map : res) {
                if (map.get("setting") == "511"){
                    configurationCheck.setEnsureUnixSocket(res.toString());
                }
            }
//            configurationCheck.setEnsureUnixSocket(res.toString());

        }
        else if (dbaseEntity.getDbType().equals("Oscar")){
            // 检查数据库版本
            sql = "SHOW VERSION;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            if (!res.isEmpty()){
                configurationCheck.setDatabaseVersion(res.get(0).get("setting").toString());
            }

//            使用使用账号锁定
            sql = "SHOW CHECK_LOGIN_COUNT;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            if (!res.isEmpty()){
                configurationCheck.setUseAccountLockout(res.get(0).get("setting").toString());
            }


//          允许的最大失败登录次数
            sql = "SHOW MAX_LOGIN_COUNT;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            configurationCheck.setMksureLoginAttempts(res);

//         密码有效天数
            sql = "SHOW PASSWD_VALID_DAYS;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            if (!res.isEmpty()){
                configurationCheck.setMksureValidityPeriod(res.get(0).get("setting").toString());
            }
//          禁用历史口令
            sql = "SHOW CHECK_PWD_HISTORY;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            if (!res.isEmpty()){
                configurationCheck.setIsDisableHistoricalPsw(res.get(0).get("setting").toString());
            }
//          口令复杂度
            sql = "SHOW MIN_PASSWORD_LEN;";
            res = dataBaseUtilService.queryForList(dbaseEntity, sql);
            if (!res.isEmpty()){
                configurationCheck.setMksurePSWSimple(res.get(0).get("setting").toString());
            }

    }

    }
}
