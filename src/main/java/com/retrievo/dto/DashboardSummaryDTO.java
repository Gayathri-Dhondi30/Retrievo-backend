package com.retrievo.dto;

public class DashboardSummaryDTO {
    private long totalReports;
    private long foundCount;
    private long recoveredCount;
    private long activeUsers;

    private long userTotalReports;
    private long userActiveMatches;
    private long userPendingClaims;
    private long userRecoveredCount;

    public DashboardSummaryDTO() {}

    // Getters and Setters
    public long getTotalReports() { return totalReports; }
    public void setTotalReports(long totalReports) { this.totalReports = totalReports; }

    public long getFoundCount() { return foundCount; }
    public void setFoundCount(long foundCount) { this.foundCount = foundCount; }

    public long getRecoveredCount() { return recoveredCount; }
    public void setRecoveredCount(long recoveredCount) { this.recoveredCount = recoveredCount; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getUserTotalReports() { return userTotalReports; }
    public void setUserTotalReports(long userTotalReports) { this.userTotalReports = userTotalReports; }

    public long getUserActiveMatches() { return userActiveMatches; }
    public void setUserActiveMatches(long userActiveMatches) { this.userActiveMatches = userActiveMatches; }

    public long getUserPendingClaims() { return userPendingClaims; }
    public void setUserPendingClaims(long userPendingClaims) { this.userPendingClaims = userPendingClaims; }

    public long getUserRecoveredCount() { return userRecoveredCount; }
    public void setUserRecoveredCount(long userRecoveredCount) { this.userRecoveredCount = userRecoveredCount; }
}
