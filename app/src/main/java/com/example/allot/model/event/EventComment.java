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

    /**
     * Returns whether g.et Comment Id
     */
    public String getCommentId() {
        return commentId;
    }

    /**
     * Updates comment id.
     */
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    /**
     * Returns whether g.et Author Id
     */
    public String getAuthorId() {
        return authorId;
    }

    /**
     * Updates author id.
     */
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    /**
     * Returns whether g.et Author Name
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * Updates author name.
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Returns whether g.et Text
     */
    public String getText() {
        return text;
    }

    /**
     * Updates text.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns whether g.et Created At
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Updates created at.
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns whether g.et Parent Id
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Updates parent id.
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * Returns whether g.et Upvotes
     */
    public int getUpvotes() {
        return upvotes;
    }

    /**
     * Updates upvotes.
     */
    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    /**
     * Returns whether g.et Downvotes
     */
    public int getDownvotes() {
        return downvotes;
    }

    /**
     * Updates downvotes.
     */
    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    /**
     * Returns whether g.et Upvoter Ids
     */
    public java.util.ArrayList<String> getUpvoterIds() {
        if (upvoterIds == null) {
            upvoterIds = new java.util.ArrayList<>();
        }
        return upvoterIds;
    }

    /**
     * Updates upvoter ids.
     */
    public void setUpvoterIds(java.util.ArrayList<String> upvoterIds) {
        this.upvoterIds = upvoterIds;
    }

    /**
     * Returns whether g.et Downvoter Ids
     */
    public java.util.ArrayList<String> getDownvoterIds() {
        if (downvoterIds == null) {
            downvoterIds = new java.util.ArrayList<>();
        }
        return downvoterIds;
    }

    /**
     * Updates downvoter ids.
     */
    public void setDownvoterIds(java.util.ArrayList<String> downvoterIds) {
        this.downvoterIds = downvoterIds;
    }
}
