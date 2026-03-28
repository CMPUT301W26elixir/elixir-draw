package com.example.allot.model.event;

import java.util.Date;

/**
 * Represents a comment or reply on an event.
 */
public class EventComment {
    private String commentId;
    private String authorId;
    private String authorName;
    private String text;
    private Date createdAt;
    private String parentId;

    /**
     * Creates an empty comment for Firestore deserialization.
     */
    public EventComment() {
    }

    /**
     * Creates a new comment or reply.
     */
    public EventComment(String commentId, String authorId, String authorName,
                        String text, Date createdAt, String parentId) {
        this.commentId = commentId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.createdAt = createdAt;
        this.parentId = parentId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
