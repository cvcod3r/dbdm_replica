package com.dbms.enums;


public enum SqlTypeEnum {
    ALTER("ALTER",1),
    CREATEINDEX("CREATEINDEX",2),
    CREATETABLE("CREATETABLE",3),
    CREATEVIEW("CREATEVIEW",4),

    DELETE("DELETE",5),
    DROP("DROP",6),
    EXECUTE("EXECUTE",7),
    INSERT("INSERT",8),
    MERGE("MERGE",9),
    REPLACE("REPLACE",10),
    SELECT("SELECT",11),
    TRUNCATE("TRUNCATE",12),
    UPDATE("UPDATE",13),
    UPSERT("UPSERT",14),
    NONE("NONE",15),
    SHOW("SHOW", 16),
    DESCRIBE("DESCRIBE", 17),
    DECLARE("DECLARE", 18),
    RENAME("RENAME", 19),
    EXPLAIN("EXPLAIN", 20),
    COMMENT("COMMENT", 21),
    // DCL
    GRANT("GRANT", 35),
    REVOKE("REVOKE", 36),

    // TCL
    COMMIT("COMMIT", 37),
    ROLLBACK("ROLLBACK", 38),

    CREATESCHEMA("CREATESCHEMA", 39),

    CREATEFUNCTION("CREATEFUNCTION", 40),

    CREATEPROCEDURE("CREATEPROCEDURE", 41),

    //HBASE
    LIST_NAMESPACE("LIST_NAMESPACE",100),

    LIST_NAMESPACE_TABLES("LIST_NAMESPACE_TABLES",101),

    DESCRIBE_NAMESPACE("DESCRIBE_NAMESPACE",102),

    DROP_NAMESPACE("DROP_NAMESPACE",103),

    CREATE("CREATE",104),

    LIST("LIST",105),

    PUT("PUT",106),

    GET("GET",107),

    SCAN("SCAN",108),

    DROP_ALL("DROP_ALL",109),

    DELETEALL("DELETEALL", 110),

    DESC("DESCRIBE", 111),

    CREATE_NAMESPACE("CREATE_NAMESPACE",112),

    DISABLE("DISABLE",113 ),

    EXIST("EXIST", 114),

    ENABLE("ENABLE", 115),
    // HBASE END

    // HIVE
    CREATEDATABASE("CREATEDATABASE", 200),

    CREATEROLE("CREATEROLE",201),

    DROPTABLE("DROPTABLE",202),

    DROPROLE("DROPROLE",203),

    DROPVIEW("DROPVIEW",204),

    LOADDATA("LOADDATA",205);




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private String name;
    private int index;

    SqlTypeEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }
}
