package com.tinnkm.rpc.common;

/**
 * 封装rpc响应
 * Created by tinnkm on 2017/11/14.
 */
public class RpcResponse {
    private String requestId;
    private Throwable error;
    private Object result;

    public boolean isError(){
        return error != null;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
