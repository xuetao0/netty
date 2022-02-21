package org.xue.netty.base;

public class NettyCmd {
    private String desc;
    private int code;
    private String body;


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NettyCmd{" +
                "desc='" + desc + '\'' +
                ", code=" + code +
                ", body='" + body + '\'' +
                '}';
    }
}
