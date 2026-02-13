package com.blog.platform.article.application.dto;

public class ArticleStatisticsDto {
    
    private long viewCount;
    private long likeCount;
    private long commentCount;
    private long shareCount;
    private long bookmarkCount;
    
    public ArticleStatisticsDto() {}
    
    public ArticleStatisticsDto(long viewCount, long likeCount, long commentCount, long shareCount) {
        this(viewCount, likeCount, commentCount, shareCount, 0L);
    }
    
    public ArticleStatisticsDto(long viewCount, long likeCount, long commentCount, long shareCount, long bookmarkCount) {
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.shareCount = shareCount;
        this.bookmarkCount = bookmarkCount;
    }
    
    public long getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }
    
    public long getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }
    
    public long getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }
    
    public long getShareCount() {
        return shareCount;
    }
    
    public void setShareCount(long shareCount) {
        this.shareCount = shareCount;
    }
    
    public long getBookmarkCount() {
        return bookmarkCount;
    }
    
    public void setBookmarkCount(long bookmarkCount) {
        this.bookmarkCount = bookmarkCount;
    }
}
