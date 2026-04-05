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
    private int upvotes;
    private int downvotes;
    private java.util.ArrayList<String> upvoterIds;
    private java.util.ArrayList<String> downvoterIds;

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
        this.upvotes = 0;
        this.downvotes = 0;
        this.upvoterIds = new java.util.ArrayList<>();
        this.downvoterIds = new java.util.ArrayList<>();
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

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public java.util.ArrayList<String> getUpvoterIds() {
        if (upvoterIds == null) {
            upvoterIds = new java.util.ArrayList<>();
        }
        return upvoterIds;
    }

    public void setUpvoterIds(java.util.ArrayList<String> upvoterIds) {
        this.upvoterIds = upvoterIds;
    }

    public java.util.ArrayList<String> getDownvoterIds() {
        if (downvoterIds == null) {
            downvoterIds = new java.util.ArrayList<>();
        }
        return downvoterIds;
    }

    public void setDownvoterIds(java.util.ArrayList<String> downvoterIds) {
        this.downvoterIds = downvoterIds;
    }
}
