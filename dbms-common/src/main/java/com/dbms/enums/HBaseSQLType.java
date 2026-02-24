package com.dbms.enums;

public enum HBaseSQLType {


    LIST_NAMESPACE("LIST_NAMESPACE",1),

    LIST_NAMESPACE_TABLES("LIST_NAMESPACE_TABLES",2),

    CREATE_NAMESPACE("CREATE_NAMESPACE",3),

    DESCRIBE_NAMESPACE("DESCRIBE_NAMESPACE",4),

    DROP_NAMESPACE("DROP_NAMESPACE",5),

    CREATE("CREATE",6),

    LIST("LIST",7),

    DESCRIBE("DESCRIBE",8),

    ALTER("ALTER",9),

    PUT("PUT",10),

    GET("GET",11),

    SCAN("SCAN",12),

    DROP("DROP",13),

    DROP_ALL("DROP_ALL",14),

    DELETE("DELETE",15),

    DELETEALL("DELETEALL", 16),

    DESCRIBWE("DESCRIBE", 17),

    TRUNCATE("TRUNCATE", 18);

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

    HBaseSQLType(String name, int index) {
        this.name = name;
        this.index = index;
    }
}
