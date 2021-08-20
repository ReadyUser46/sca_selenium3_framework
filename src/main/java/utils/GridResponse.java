package utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GridResponse {

  /**
   * Model for deserialize the response body with Gson.
   */
  @SerializedName("inactivityTime")
  @Expose
  private int inactivityTime;
  @SerializedName("internalKey")
  @Expose
  private String internalKey;
  @SerializedName("msg")
  @Expose
  private String msg;
  @SerializedName("proxyId")
  @Expose
  private String proxyId;
  @SerializedName("session")
  @Expose
  private String session;
  @SerializedName("success")
  @Expose
  private boolean success;

  public int getInactivityTime() {
    return inactivityTime;
  }

  public void setInactivityTime(int inactivityTime) {
    this.inactivityTime = inactivityTime;
  }

  public String getInternalKey() {
    return internalKey;
  }

  public void setInternalKey(String internalKey) {
    this.internalKey = internalKey;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getProxyId() {
    return proxyId;
  }

  public void setProxyId(String proxyId) {
    this.proxyId = proxyId;
  }

  public String getSession() {
    return session;
  }

  public void setSession(String session) {
    this.session = session;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }
}
