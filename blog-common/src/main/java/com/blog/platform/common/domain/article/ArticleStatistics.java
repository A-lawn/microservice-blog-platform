package com.blog.platform.common.domain.article;

import com.blog.platform.common.domain.ValueObject;

import java.util.Objects;

public class ArticleStatistics extends ValueObject {
    
    private final long viewCount;
    private final long likeCount;
    private final long commentCount;
    private final long shareCount;
    private final long bookmarkCount;
    
    public ArticleStatistics(long viewCount, long likeCount, long commentCount, long shareCount) {
        this(viewCount, likeCount, commentCount, shareCount, 0L);
    }
    
    public ArticleStatistics(long viewCount, long likeCount, long commentCount, long shareCount, long bookmarkCount) {
        if (viewCount < 0 || likeCount < 0 || commentCount < 0 || shareCount < 0 || bookmarkCount < 0) {
            throw new IllegalArgumentException("统计数据不能为负数");
        }
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.shareCount = shareCount;
        this.bookmarkCount = bookmarkCount;
        validate();
    }
    
    public static ArticleStatistics empty() {
        return new ArticleStatistics(0L, 0L, 0L, 0L, 0L);
    }
    
    public static ArticleStatistics of(long viewCount, long likeCount, long commentCount, long shareCount) {
        return new ArticleStatistics(viewCount, likeCount, commentCount, shareCount, 0L);
    }
    
    public static ArticleStatistics of(long viewCount, long likeCount, long commentCount, long shareCount, long bookmarkCount) {
        return new ArticleStatistics(viewCount, likeCount, commentCount, shareCount, bookmarkCount);
    }
    
    public ArticleStatistics incrementViewCount() {
        return new ArticleStatistics(viewCount + 1, likeCount, commentCount, shareCount, bookmarkCount);
    }
    
    public ArticleStatistics incrementLikeCount() {
        return new ArticleStatistics(viewCount, likeCount + 1, commentCount, shareCount, bookmarkCount);
    }
    
    public ArticleStatistics decrementLikeCount() {
        return new ArticleStatistics(viewCount, Math.max(0, likeCount - 1), commentCount, shareCount, bookmarkCount);
    }
    
    public ArticleStatistics incrementCommentCount() {
        return new ArticleStatistics(viewCount, likeCount, commentCount + 1, shareCount, bookmarkCount);
    }
    
    public ArticleStatistics decrementCommentCount() {
        return new ArticleStatistics(viewCount, likeCount, Math.max(0, commentCount - 1), shareCount, bookmarkCount);
    }
    
    public ArticleStatistics incrementShareCount() {
        return new ArticleStatistics(viewCount, likeCount, commentCount, shareCount + 1, bookmarkCount);
    }
    
    public ArticleStatistics incrementBookmarkCount() {
        return new ArticleStatistics(viewCount, likeCount, commentCount, shareCount, bookmarkCount + 1);
    }
    
    public ArticleStatistics decrementBookmarkCount() {
        return new ArticleStatistics(viewCount, likeCount, commentCount, shareCount, Math.max(0, bookmarkCount - 1));
    }
    
    public long getViewCount() {
        return viewCount;
    }
    
    public long getLikeCount() {
        return likeCount;
    }
    
    public long getCommentCount() {
        return commentCount;
    }
    
    public long getShareCount() {
        return shareCount;
    }
    
    public long getBookmarkCount() {
        return bookmarkCount;
    }
    
    @Override
    protected void validate() {
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ArticleStatistics that = (ArticleStatistics) obj;
        return viewCount == that.viewCount &&
               likeCount == that.likeCount &&
               commentCount == that.commentCount &&
               shareCount == that.shareCount &&
               bookmarkCount == that.bookmarkCount;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(viewCount, likeCount, commentCount, shareCount, bookmarkCount);
    }
    
    @Override
    public String toString() {
        return String.format("ArticleStatistics{views=%d, likes=%d, comments=%d, shares=%d, bookmarks=%d}", 
                viewCount, likeCount, commentCount, shareCount, bookmarkCount);
    }
}
