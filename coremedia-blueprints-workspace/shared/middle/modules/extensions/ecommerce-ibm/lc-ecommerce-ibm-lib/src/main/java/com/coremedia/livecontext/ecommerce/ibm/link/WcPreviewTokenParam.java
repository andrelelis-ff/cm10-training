package com.coremedia.livecontext.ecommerce.ibm.link;

import com.coremedia.livecontext.ecommerce.ibm.login.LoginServiceImpl;

/**
 * Parameter model for REST-PreviewToken
 * @see LoginServiceImpl
 */
public class WcPreviewTokenParam {

  private String workspaceId;
  private String start;
  private String timeZoneId;
  private String includedMemberGroupIds;
  private String tokenLife;
  // instruct the wcs to assume that the time is fix and must not elapse in that preview session
  private String status = "true";

  public WcPreviewTokenParam(String workspaceId, String start, String timeZoneId, String status, String includedMemberGroupIds, String tokenLife) {
    this.workspaceId = workspaceId;
    this.start = start;
    this.timeZoneId = timeZoneId;
    this.includedMemberGroupIds = includedMemberGroupIds;
    this.tokenLife = tokenLife;
    this.status = status;
  }

  public String getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public String getStart() {
    return start;
  }

  public void setStart(String start) {
    this.start = start;
  }

  public String getTimeZoneId() {
    return timeZoneId;
  }

  public void setTimeZoneId(String timeZoneId) {
    this.timeZoneId = timeZoneId;
  }

  public String getIncludedMemberGroupIds() {
    return includedMemberGroupIds;
  }

  public void setIncludedMemberGroupIds(String includedMemberGroupIds) {
    this.includedMemberGroupIds = includedMemberGroupIds;
  }

  public String getTokenLife() {
    return tokenLife;
  }

  public void setTokenLife(String tokenLife) {
    this.tokenLife = tokenLife;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public int hashCode() {
    int result = workspaceId != null ? workspaceId.hashCode() : 0;
    result = 31 * result + (start != null ? start.hashCode() : 0);
    result = 31 * result + (timeZoneId != null ? timeZoneId.hashCode() : 0);
    result = 31 * result + (includedMemberGroupIds != null ? includedMemberGroupIds.hashCode() : 0);
    result = 31 * result + (tokenLife != null ? tokenLife.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    WcPreviewTokenParam that = (WcPreviewTokenParam) o;

    if (includedMemberGroupIds != null ? !includedMemberGroupIds.equals(that.includedMemberGroupIds) : that.includedMemberGroupIds != null) {
      return false;
    }
    if (start != null ? !start.equals(that.start) : that.start != null) {
      return false;
    }
    if (timeZoneId != null ? !timeZoneId.equals(that.timeZoneId) : that.timeZoneId != null) {
      return false;
    }
    if (tokenLife != null ? !tokenLife.equals(that.tokenLife) : that.tokenLife != null) {
      return false;
    }
    if (workspaceId != null ? !workspaceId.equals(that.workspaceId) : that.workspaceId != null) {
      return false;
    }
    if (status != null ? !status.equals(that.status) : that.status != null) {
      return false;
    }

    return true;
  }
}
